package org.mpi.vasco.txstore.storageshim;

import org.mpi.vasco.util.ObjectPool;
import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.txstore.BaseNode;
import org.mpi.vasco.txstore.membership.Role;

import org.mpi.vasco.txstore.messages.MessageTags;

// receiving messages
import org.mpi.vasco.txstore.messages.AckCommitTxnMessage;
import org.mpi.vasco.txstore.messages.CommitShadowOpMessage;
import org.mpi.vasco.txstore.messages.MessageFactory;
import org.mpi.vasco.txstore.proxy.TransactionInfo;

import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.Result;
import org.mpi.vasco.txstore.util.ReadWriteSet;
import org.mpi.vasco.txstore.util.OperationLog;

import org.mpi.vasco.txstore.scratchpad.ScratchpadInterface;
import org.mpi.vasco.txstore.scratchpad.ScratchpadFactory;
import org.mpi.vasco.txstore.scratchpad.ScratchpadException;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.network.netty.NettyTCPSender;
import org.mpi.vasco.network.netty.NettyTCPReceiver;
import org.mpi.vasco.network.PassThroughNetworkQueue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

public class StorageShim extends BaseNode {

	MessageFactory mf;
	long lastTxnCount = 0;
	LogicalClock lastCommitted;

	Hashtable<ProxyTxnId, ScratchpadInterface> scratchpads;
	ScratchpadFactory spFactory;
	Hashtable<ProxyTxnId, TransactionInfo> txnTable;

	ArrayList<ArrayList<CommitShadowOpMessage>> checkQueueList;
	ArrayList<CommitShadowOpMessage> commitWaitingQueue;
	
	public long txnNum = 0;
	public int disOrderNum = 0;
	public long latency = 0;
	public long startTime = 0;
	int dcCount;
	
	//objectPool
	ObjectPool<TransactionInfo> txnPool;

	public StorageShim(String file, int dc, int id,
			ScratchpadFactory spadFactory) {
		super(file, dc, Role.STORAGE, id);
		dcCount = getDatacenterCount();
		checkQueueList = new ArrayList<ArrayList<CommitShadowOpMessage>>();
		for(int i = 0 ; i < dcCount ; i++){
			ArrayList<CommitShadowOpMessage> checkQueue = new ArrayList<CommitShadowOpMessage>();
			checkQueueList.add(checkQueue);
		}
		this.mf = new MessageFactory();
		scratchpads = new Hashtable<ProxyTxnId, ScratchpadInterface>();
		this.spFactory = spadFactory;
		lastCommitted = new LogicalClock(getMembership().getDatacenterCount());
		commitWaitingQueue = new ArrayList<CommitShadowOpMessage>();
	    txnTable = new Hashtable<ProxyTxnId, TransactionInfo>();
	    startTime = System.nanoTime();
	    txnPool = new ObjectPool<TransactionInfo>();
	}

	int messageCount = 0;
	int messages[] = new int[7];
	long msgstart = 0;
	long msgend = 0;

	/***
	 * handle incoming messages. implements ByteHandler
	 ***/
	public void handle(byte[] b) {
		MessageBase msg = mf.fromBytes(b);
		if (msg == null)
			throw new RuntimeException("Should never receive a null message");
		messageCount++;
		if (messageCount % 2000 == 0) {
			messageCount = 0;
			msgend = System.nanoTime();
			long total = msgend - msgstart;
			msgstart = msgend;
			System.out.println("msgs / second: " + (float) (100000)
					/ (float) (total) * (float) 1000000 * (float) 1000);
		}

		switch (msg.getTag()) {
			
		case MessageTags.COMMITSHADOW:
			process((CommitShadowOpMessage) msg);
			Debug.println("end of commitshadowop " + ((CommitShadowOpMessage) msg).toString());
			return;
		default:
			throw new RuntimeException("invalid message tag: " + msg.getTag());
		}

	}
	
	private synchronized boolean casualityCheck(LogicalClock lc){
		if (!lastCommitted.lessThanByAtMostOne(lc)){
			return false;
		}
		updateLastCommitted(lc);
		return true;
	}
	
	
	
	private void process(CommitShadowOpMessage msg) {
		// TODO Auto-generated method stub
		Debug.println("receive a commitshadow op " + msg);
		
		TransactionInfo info  = txnPool.borrowObject();
		if(info == null){
			info = new TransactionInfo(msg.getTxnId());
		}else{
			info.setTxnId(msg.getTxnId());
		}
		info.setStartTime();
		txnTable.put(msg.getTxnId(), info);
		if(casualityCheck(msg.getLogicalClock())){
			//Debug.println("passed casuality check " + msg);
			Debug.println("passed casuality check " + msg);
			addToCommitQueue(msg);
		}else{
			Debug.println("casuality check failed, need to wait " + msg);
			addToCheckQueue(msg);
			disOrderNum++;
		}
		while(true){
			CommitShadowOpMessage tmpMsg = getNextCommitMessage();
			if (tmpMsg != null){
				try {
					commitTxn(tmpMsg);
				} catch (ScratchpadException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else
				return;
		}
	}
	
	protected void addToCheckQueue(CommitShadowOpMessage msg){
		
		int dcId = msg.getTxnId().getDcId();
		ArrayList<CommitShadowOpMessage> checkWaitingQueue = checkQueueList.get(dcId);
		
		synchronized(checkWaitingQueue){
			binaryInsert(checkWaitingQueue, msg, dcId);
		}
	}
	
	protected void addToCommitQueue(CommitShadowOpMessage msg){
		synchronized(commitWaitingQueue){
			commitWaitingQueue.add(msg);
		}
	}
	
	protected CommitShadowOpMessage getNextCommitMessage(){
		
		CommitShadowOpMessage msg = null;
		
		synchronized(commitWaitingQueue){
			if(commitWaitingQueue.size() > 0){
				Debug.println("get a commit msg from commit queue " + msg);
				msg = commitWaitingQueue.remove(0);
				return msg;
			}
		}
		
		boolean isCommitExist = false;
		for(int i = 0; i < dcCount; i++){
			ArrayList<CommitShadowOpMessage> checkWaitingQueue = checkQueueList.get(i);
			synchronized(checkWaitingQueue){
				if(checkWaitingQueue.size() > 0){
					if(casualityCheck(checkWaitingQueue.get(0).getLogicalClock())){
						Debug.println("get a commit msg from check queue " + msg);
						msg = checkWaitingQueue.remove(0);
						if(msg !=null){
							addToCommitQueue(msg);
							isCommitExist = true;
						}
					}
				}
			}
		}
		
		if(isCommitExist){
			synchronized(commitWaitingQueue){
				if(commitWaitingQueue.size() > 0){
					Debug.println("get a commit msg from commit queue " + msg);
					msg = commitWaitingQueue.remove(0);
					return msg;
				}
			}
		}
		return null;
	}

	Object txnNumObj = new Object();
	
	//TODO: need to fix later on, queue stuff
	public void commitTxn(CommitShadowOpMessage msg) throws ScratchpadException {
		
		Debug.println("Commit shadow operation against database " + msg);
		
		ScratchpadInterface tmp = spFactory.createScratchPad(msg.getTxnId());
		tmp.beginTransaction(msg.getTxnId());
		try {
			tmp.commitShadowOP(msg.getOperation(), msg.getLogicalClock(), msg.getTimeStamp());
			Debug.println("finish commit shadow operation " + msg.getTxnId() + msg + " lc " + msg.getLogicalClock() );
		} catch(Exception f){
			f.printStackTrace();
			System.out.println("unacceptable exception");
			System.exit(-1);
		}
		
		
		AckCommitTxnMessage acm = mf.borrowAckCommitTxnMessage();
		if(acm == null)
			acm = new AckCommitTxnMessage(msg.getTxnId(), 1);
		else
			acm.encodeMessage(msg.getTxnId(), 1);
		Debug.println("send ack commit back to coordinator " + acm);
		if(msg.getTxnId().getDatacenterId() == getMyDatacenterId())
			sendToCoordinator(acm);
		else
			sendToRemoteCoordinator(acm);
		
		releaseScratchpad(tmp, msg.getTxnId());
		synchronized(txnNumObj){
			txnNum++;
			latency += txnTable.get(msg.getTxnId()).getLatency();
			if(txnNum %2000 == 0){
				System.out.println("txnNum " + txnNum + " disOrder " + disOrderNum);
				disOrderNum = 0;
				System.out.println(	"txnNum " + txnNum + " throughput "
						+ (double) ((double) 2000 / (double) (System.nanoTime()-startTime)
								* (double) 1000000 * (double) 1000)
						+ " latency    (ms/req) " + latency + " "
						+ (double) ((double) latency
								/ (double) 2000 / (double) 1000000));
				latency = 0;
				startTime = System.nanoTime();
			}
		}
		TransactionInfo txn = txnTable.get(msg.getTxnId());
		txnTable.remove(msg.getTxnId());
		//clean datastructure
		mf.returnCommitShadowOpMessage(msg);
		mf.returnAckCommitTxnMessage(acm);
		txn.reset();
		txnPool.returnObject(txn);
	}
	

	private void updateLastCommitted(LogicalClock lc) {
		LogicalClock tmpLc = lastCommitted.maxClock(lc);
		lastCommitted = tmpLc;
		Debug.println("current logical clock: " + lastCommitted);
	}

	/*
	 * Functions added by Cheng to help scratchpad factory to maintain
	 * scratchpad pool
	 */

	protected void releaseScratchpad(ScratchpadInterface sp, ProxyTxnId txnId) {
		spFactory.releaseScratchpad(sp, txnId);
	}
	
	/*
	 * binary search the correct position to put one element
	 */
	
	void binaryInsert(ArrayList<CommitShadowOpMessage> array, CommitShadowOpMessage msg, int dcId){
		
		if(array.isEmpty()){
			array.add(msg);
			return;
		}
		if(array.size() == 1){
			CommitShadowOpMessage tmp = array.get(0);
			if(isGE(msg, tmp, dcId)){
				array.add(msg);
			}else{
				array.add(0, msg);
			}
			return;
		}
		
		int left = 0;
		int right = array.size() - 1;
		while(left < right){
			int middle = (left + right)/2;
			CommitShadowOpMessage tmp = array.get(middle);
			if(isGE(msg, tmp, dcId))
				left = middle + 1;
			else
				right = middle;
		}
		CommitShadowOpMessage tmp = array.get(left);
		if(isGE(msg,tmp,dcId)){
			array.add(left+1, msg);
		}else{
			array.add(left, msg);
		}
		
	}
	
	boolean isGE(CommitShadowOpMessage msg1, CommitShadowOpMessage msg2, int dcId){
		LogicalClock lc1 = msg1.getLogicalClock();
		LogicalClock lc2 = msg2.getLogicalClock();
		if(lc1.getDcEntry(dcId) == lc2.getDcEntry(dcId)){
			System.out.println("bad two entries has the same count " + lc1 + " " + lc2);
			System.exit(0);
		}
		if(lc1.getDcEntry(dcId) > lc2.getDcEntry(dcId)){
			return true;
		}
		return false;
	}
	
	

}


