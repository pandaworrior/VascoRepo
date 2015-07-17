package org.mpi.vasco.txstore.coordinator;

import org.mpi.vasco.txstore.BaseNode;
import org.mpi.vasco.txstore.membership.Role;

import org.mpi.vasco.txstore.messages.MessageTags;

// receiving messages
import org.mpi.vasco.txstore.messages.AckCommitTxnMessage;
import org.mpi.vasco.txstore.messages.BeginTxnMessage;
import org.mpi.vasco.txstore.messages.BlueTokenGrantMessage;
import org.mpi.vasco.txstore.messages.CommitShadowOpMessage;
import org.mpi.vasco.txstore.messages.FinishTxnMessage;
import org.mpi.vasco.txstore.messages.MessageFactory;
import org.mpi.vasco.txstore.messages.OperationMessage;
import org.mpi.vasco.txstore.messages.ProxyCommitMessage;
import org.mpi.vasco.txstore.messages.ReadWriteSetMessage;
import org.mpi.vasco.txstore.messages.RemoteShadowOpMessage;
import org.mpi.vasco.txstore.messages.TxnReadyMessage;
import org.mpi.vasco.txstore.messages.TxnMetaInformationMessage;

// sending messages
import org.mpi.vasco.txstore.messages.AckTxnMessage;
import org.mpi.vasco.txstore.messages.CommitTxnMessage;
import org.mpi.vasco.txstore.messages.AbortTxnMessage;
import org.mpi.vasco.txstore.messages.FinishRemoteMessage;
import org.mpi.vasco.txstore.messages.GimmeTheBlueMessage;

import org.mpi.vasco.txstore.storageshim.StorageShim;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.ReadWriteSet;
import org.mpi.vasco.txstore.util.StorageList;
import org.mpi.vasco.txstore.util.ReadSetEntry;
import org.mpi.vasco.txstore.util.ReadSet;
import org.mpi.vasco.txstore.util.WriteSet;
import org.mpi.vasco.txstore.util.WriteSetEntry;

import org.mpi.vasco.util.Counter;
import org.mpi.vasco.util.ObjectPool;
import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.network.netty.NettyTCPSender;
import org.mpi.vasco.network.netty.NettyTCPReceiver;
import org.mpi.vasco.network.ParallelPassThroughNetworkQueue;
import org.mpi.vasco.network.PassThroughNetworkQueue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import java.util.Vector;


public class RemoteCoordinator extends BaseNode {

	MessageFactory mf;
	NewCoordinator coord; 
	
	ObjectPool<TransactionRecord> txnPool;
	Hashtable<ProxyTxnId, TransactionRecord> records;

	public RemoteCoordinator(String file, int dc, int id) {
		super(file, dc, Role.REMOTECOORDINATOR, id);
		System.out.println("start remote coordinator acceptor");
		this.mf = new MessageFactory();
		records = new Hashtable<ProxyTxnId, TransactionRecord>();
		
		//initiate the txnPool
		txnPool = new ObjectPool<TransactionRecord>();
		for(int i = 0; i < 100; i++){
			TransactionRecord txn = new TransactionRecord();
			txnPool.addObject(txn);
		}
		Debug.println("RemoteCoordinator acceptor finished initialization and starts");
	}
	
	public void setCoordinator(NewCoordinator c){
		coord = c;
	}

	/***
	 * handle incoming messages. implements ByteHandler
	 ***/
	public void handle(byte[] b) {
		MessageBase msg = mf.fromBytes(b);
		if (msg == null) {
			throw new RuntimeException("Should never receive a null message");
		}
		
		 if (coord.messageCount.incrementAndGet() % 5000 == 0) { 
			 coord.messageCount.set(0);
			 System.out.println("beginTxn  |  gimetheblue |  abortxn | bluetokengrant | proxycommit | ackcommit | remoteshadow ");
			 for (int i = 0; i < coord.messages.length; i++) {
				 System.out.print(coord.messages[i] + "\t"); coord.messages[i] = 0; 
			 }
			 System.out.println();
		 }
		switch (msg.getTag()) {
		case MessageTags.ACKCOMMIT:
			coord.messages[5]++;
			process((AckCommitTxnMessage) msg);
			return;
		case MessageTags.REMOTESHADOW:
			coord.messages[6]++;
			process((RemoteShadowOpMessage) msg);
			return;
		default:
			throw new RuntimeException("invalid message tag: " + msg.getTag());
		}

	}

	private void process(AckCommitTxnMessage msg) {
		// TODO Auto-generated method stub
		Debug.println("receive ack commit " + msg);
		
		TransactionRecord tmpRec = records.get(msg.getTxnId());
		coord.updateLastCommittedLogicalClock(tmpRec.getMergeClock(), tmpRec.isBlue());
		coord.updateLastVisibleLogicalClock(tmpRec.getMergeClock());
		// insure that one dc doesnt "always win" because another is underloaded
		coord.setLocalTxn(tmpRec.getFinishTime().getCount());
		
		coord.updateObjectTable(tmpRec.getWriteSet().getWriteSet(), tmpRec.getMergeClock(), 
				tmpRec.getFinishTime(), tmpRec.getTxnId());
		
		coord.statisticOutput(tmpRec);
		records.remove(tmpRec.getTxnId());
		
		//clean datastructure
		mf.returnRemoteShadowOpMessage(tmpRec.rOpMsg);
		mf.returnCommitShadowOpMessage(tmpRec.cSMsg);
		tmpRec.reset();
		txnPool.returnObject(tmpRec);
		mf.returnAckCommitTxnMessage(msg);
	}

	private void process(RemoteShadowOpMessage msg) {
		// TODO Auto-generated method stub
		Debug.println("receive remote shadow " + msg);
		TransactionRecord txn  = txnPool.borrowObject();
		if(txn == null){
			txn = new TransactionRecord(msg.getTxnId(), msg.getTimeStamp(), msg.getLogicalClock());
		}else{
			txn.setTxnId(msg.getTxnId());
		}
		records.put(msg.getTxnId(), txn);
		txn.setWriteSet(msg.getWset());
		txn.setShadowOp(msg.getShadowOperation());
		txn.setColor(msg.getColor());
		txn.setMergeClock(msg.getLogicalClock());
		txn.setFinishTime(msg.getTimeStamp());
		txn.setRemote();
		txn.addStorage(0);
		txn.setRemoteShadowOpMessage(msg);
		
		CommitShadowOpMessage csm = mf.borrowCommitShadowOpMessage();
		if(csm == null){
			csm = new CommitShadowOpMessage(txn.getTxnId(), 
				txn.getShadowOp(), msg.getTimeStamp(), txn.getMergeClock());
		}else{
			csm.encodeMessage(txn.getTxnId(), 
				txn.getShadowOp(), msg.getTimeStamp(), txn.getMergeClock());
		}
		txn.setCommitShadowOpMessage(csm);
		Debug.println("commit remote to data writer" + csm);
		sendToStorage(csm, 0); //TODO: fix to more generic
	}


}

