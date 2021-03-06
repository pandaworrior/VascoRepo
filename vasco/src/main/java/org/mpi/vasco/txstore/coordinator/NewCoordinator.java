package org.mpi.vasco.txstore.coordinator;

import org.mpi.vasco.txstore.BaseNode;
import org.mpi.vasco.txstore.membership.Role;

import org.mpi.vasco.txstore.messages.MessageTags;

// receiving messages
import org.mpi.vasco.txstore.messages.AckCommitTxnMessage;
import org.mpi.vasco.txstore.messages.BeginTxnMessage;
import org.mpi.vasco.txstore.messages.CommitShadowOpMessage;
import org.mpi.vasco.txstore.messages.FakedRemoteShadowOpMessage;
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

import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSifterEmptyShd;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSifterShd;
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

import org.mpi.vasco.coordination.VascoServiceAgent;
import org.mpi.vasco.coordination.VascoServiceAgentFactory;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
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

public class NewCoordinator extends BaseNode {
	
	private static final long serialVersionUID = 1L;
	MessageFactory mf;
	long lastTxnCount = 0;
	
	ObjectPool<TransactionRecord> txnPool;
	Hashtable<ProxyTxnId, TransactionRecord> records;
	LogicalClock lastCommitted;
	LogicalClock lastVisible;
	UpdateTable objectUpdates;
	
	//debug
	long checkTxnNum = 0;
	long checkLatency = 0;
	long ackTxnNum = 0;
	long ackLatency = 0;

	long timeToCommit = 0;
	long commitCount = 0;
	long manualAbort = 0;
	long nonConflictOpReadAbort = 0;
	long conflictOpReadAbort = 0;
	long conflictOpWriteAbort = 0;
	long redoCount = 0;

	Object lastCommittedSynObj = new Object(); // syn object for lastCommitted
	
	//objects for vasco coordination service
	VascoServiceAgent vascoAgent;

	public NewCoordinator(String file, int dc, int id, String vascoMemFile) {
		super(file, dc, Role.COORDINATOR, id);
		this.mf = new MessageFactory();
		records = new Hashtable<ProxyTxnId, TransactionRecord>();
		long[] dclist = new long[getMembership().getDatacenterCount()];
		lastCommitted = new LogicalClock(dclist);
		lastVisible = new LogicalClock(dclist);
		objectUpdates = new UpdateTable(100000);
		
		//initiate the txnPool
		txnPool = new ObjectPool<TransactionRecord>();
		for(int i = 0; i < 100; i++){
			TransactionRecord txn = new TransactionRecord();
			txnPool.addObject(txn);
		}
		this.setVascoAgent(VascoServiceAgentFactory.createVascoServiceAgent(vascoMemFile, dc));
		System.out.println("Coordinator finished initialization and starts");
	}

	AtomicInteger messageCount = new AtomicInteger(0);
	int messages[] = new int[7];
	long msgstart = 0;
	long msgend = 0;

	/***
	 * handle incoming messages. implements ByteHandler
	 ***/
	public void handle(byte[] b) {
		MessageBase msg = mf.fromBytes(b);
		if (msg == null) {
			throw new RuntimeException("Should never receive a null message");
		}
		
		 if (messageCount.incrementAndGet() % 5000 == 0) { 
			 messageCount.set(0);
			 System.out.println("beginTxn  |  gimetheblue |  abortxn | bluetokengrant | proxycommit | ackcommit | remoteshadow ");
			 for (int i = 0; i < messages.length; i++) {
				 System.out.print(messages[i] + "\t"); messages[i] = 0; 
			 }
			 System.out.println();
		 }
		switch (msg.getTag()) {
		case MessageTags.BEGINTXN:
			messages[0]++;
			process((BeginTxnMessage) msg);
			return;
		case MessageTags.ABORTTXN:
			messages[2]++;
			process((AbortTxnMessage) msg);
			return;
		case MessageTags.PROXYCOMMIT:
			messages[4]++;
			process((ProxyCommitMessage) msg);
			return;
		case MessageTags.ACKCOMMIT:
			messages[5]++;
			process((AckCommitTxnMessage) msg);
			return;
		default:
			throw new RuntimeException("invalid message tag: " + msg.getTag());
		}

	}

	/**
	 * 
	 * @param receive a notification from the storage part and notify the proxy and update objectTable
	 */
	private void process(AckCommitTxnMessage msg) {
		Debug.println("receive ack commit " + msg);
		
		TransactionRecord tmpRec = records.get(msg.getTxnId());
		updateLastVisibleLogicalClock(tmpRec.getMergeClock());
		// insure that one dc doesnt "always win" because another is underloaded
		setLocalTxn(tmpRec.getFinishTime().getCount());
		
		updateObjectTable(tmpRec.getWriteSet().getWriteSet(), tmpRec.getMergeClock(), 
			tmpRec.getFinishTime(), tmpRec.getTxnId());
		//if this txn is conflicting, then we need to clean up
		
		if(tmpRec.isConflicting()){
			Debug.println("Let's clean up this conflicting txn");
			this.getVascoAgent().cleanUpLocalOperation(tmpRec.getTxnId(), tmpRec.getLcRequest());
		}
		statisticOutput(tmpRec);
		records.remove(tmpRec.getTxnId());
		Debug.println("reply to proxy " + msg);
		sendToProxy(msg, tmpRec.getTxnId().getProxyId());
		mf.returnProxyCommitMessage(tmpRec.msg);
		mf.returnBeginTxnMessage(tmpRec.bMsg);
		
		//clean datastructure
		mf.returnRemoteShadowOpMessage(tmpRec.rOpMsg);
		mf.returnCommitShadowOpMessage(tmpRec.cSMsg);
		tmpRec.reset();
		txnPool.returnObject(tmpRec);
		mf.returnAckCommitTxnMessage(msg);
	}

	private void process(ProxyCommitMessage msg) {
		Debug.println("proxy commit message " + msg);
		TransactionRecord txn = records.get(msg.getTxnId());
		txn.setReadWriteSet(msg.getRwset(), 0);
		txn.setShadowOp(msg.getShadowOperation());
		txn.addStorage(0);
		txn.setProxyCommitMessage(msg);
		
		if(!txn.isReadonly()){
			//generate lock request and isConflicting flag
			LockRequest lcRequest = this.getVascoAgent().generateLockRequestFromWriteSet(
					txn.getOpName(), txn.getTxnId(), txn.getWriteSet());
			txn.setLcRequest(lcRequest);
			txn.setConflicting(!(lcRequest == null));
		}
		
		finishTransaction(txn);
		/*synchronized(checkObj){
			checkLatency += System.nanoTime() - startTime;
			checkTxnNum++;
			if(checkTxnNum % 2000 == 0){
					System.out.println(	"txnNum " + checkTxnNum + " proxy commit latency    (ms/req) " + checkLatency + " "
							+ (double) ((double) checkLatency
									/ (double) 2000 / (double) 1000000)+ " object table size " + objectUpdates.size()) ;
					checkLatency = 0;
			}
		}*/
		
	}


	/**
	 * Process the begin transaction message to start a transaction. Creates a
	 * transaction record with appropriate information.
	 **/
	public void process(BeginTxnMessage msg) {
		Debug.println("receive a begin txn message " + msg);
		TimeStamp ts = currentTimeStamp();// TODO: need to be accuracy?
		// the following synchronization block is needed to protect startset
		//LogicalClock lc = currentLogicalClock();
		LogicalClock lc = getLastVisibleLogicalClock();
		AckTxnMessage ack = mf.borrowAckTxnMessage();
		if(ack == null){
			ack = new AckTxnMessage(msg.getTxnId(), ts);
		}else{
			ack.encodeMessage(msg.getTxnId(), ts);
		}
		TransactionRecord tmprec = txnPool.borrowObject();
		if(tmprec == null){
			tmprec = new TransactionRecord(msg.getTxnId(), ts, lc);
		}else{
			tmprec.setTxnId(msg.getTxnId());
			tmprec.setTimestampLc(ts, lc);
		}
		tmprec.setBeginTxnMessage(msg);
		tmprec.setAckTxnMessage(ack);
		records.put(msg.getTxnId(), tmprec);
		sendToProxy(ack, msg.getTxnId().getProxyId());
	}

	
	public void process(AbortTxnMessage msg) {
		Debug.println("receive a abort txn " + msg);
		manualAbort++;
		TransactionRecord tmpRec = records.get(msg.getTxnId());
		if (tmpRec != null){
			abortTxn(records.get(msg.getTxnId()));
		}
		else {
			System.out.println("tried to abort a non-existent transaction! "
					+ msg);
			System.exit(-1);
		}
	}

	long INTERVAL = 10000;
	long remoteCount = 0;
	long remoteStartTime = System.nanoTime();
	long totalthroughputCount = 0;
	long totalthroughputTime = System.nanoTime();
	long skipCount = 0;

	private float rate(long f, long timeNow, long baseTime) {
		return (float) ((float) f / (float) (timeNow - baseTime))
				* (float) (1000) * (float) (1000000);
	}

	int NOTAPROXY = 13897;

	/**
	 * The primary driver for finishing a transaction. (1) set the snapshot
	 * clock (i.e. execution time) (2) identify the current blue epoch (3) check
	 * for read coherence; abort if not coherent (4) check for write coherence;
	 * abort if not coherent (5) attempt to finalize the transaction, if
	 * finalization fails then try again
	 * check whether you need to have synchronized keyword here
	 **/
	private void finishTransaction(TransactionRecord tmpRec) {
		if(!tmpRec.isReadonly()){
			//acquire the permission here
			this.getVascoAgent().getPemissions(tmpRec.getTxnId(), tmpRec.getLcRequest());
			Debug.println("get lock permissions done!");
			//wait for the permission is matching
			this.getVascoAgent().waitForBeExecuted(tmpRec.getTxnId(), tmpRec.getLcRequest());
		}
		Debug.println("start to finalize the transaction + readonly " + tmpRec.isReadonly() );
		this.finalizeTransaction(tmpRec);
	}
	
	/**
	 * Finalize the transaction. this function sets the logical clock for the
	 * merge time. it always completes for red transactions.. Blue transactions
	 * can only complete after acquiring permission to consume the next blue
	 * epoch.
	 * 
	 * a transaction that can be finalized is applied and then commited. if the
	 * transaction cannot be finalized, then return false.
	 **/
	
	private void finalizeTransaction(TransactionRecord tmpRec){
		if (checkReadWriteCoherence(tmpRec)){
			if(!tmpRec.isConflicting()){
				this.finalizeNonConflictingTransaction(tmpRec);
			}else{
				this.finalizeConflictingTransaction(tmpRec);
			}
		}
	}

	private boolean checkReadWriteCoherence(TransactionRecord tmpRec) {
		
		if(!tmpRec.isConflicting()){
			if(!checkReadCoherence(tmpRec)){
				abortTxn(tmpRec);
				nonConflictOpReadAbort++;
				return false;
			}
		}else{
			/*if(!checkReadCoherence(tmpRec)){
				//TODO: needed or not?
				abortTxn(tmpRec);
				conflictOpReadAbort++;
				return false;
			}else{*/
				//lock all write sets plus
				if(!checkReadWriteConflicts(tmpRec)){
					this.unlockObjectsForAbortedConflictTxn(tmpRec);
					abortTxn(tmpRec);
					conflictOpWriteAbort++;
					return false;
				}
			//}
		}
		
		Debug.println("passed coherence check " + tmpRec.getTxnId());
		return true;
	}

	private void finalizeNonConflictingTransaction(TransactionRecord tmpRec) {
		//System.out.println("\t\t\t\tfinalizenonconflictingtransaction start");
        if(tmpRec.isReadonly()){
            //send ackcommit back to proxy
            AckCommitTxnMessage acm = mf.borrowAckCommitTxnMessage();
            if(acm == null){
            	acm = new AckCommitTxnMessage( tmpRec.getTxnId(), 1);
            }else{
            	acm.encodeMessage(tmpRec.getTxnId(), 1);
            }
            Debug.println("commit a readonly txn id " + tmpRec.getTxnId());
            sendToProxy(acm, tmpRec.getTxnId().getProxyId());
            statisticOutput(tmpRec);
            records.remove(tmpRec.getTxnId());
            mf.returnAckCommitTxnMessage(acm);
            tmpRec.reset();
            txnPool.returnObject(tmpRec);
            return;
        }
		commitTxn(tmpRec);
		return;
	}
	
	private void finalizeConflictingTransaction(TransactionRecord tmpRec) {
		//System.out.println("\t\t\t\tfinalizenonconflictingtransaction start");
		commitTxn(tmpRec);
		return;

	}

	/**
	 * Check read coherence of the transaction. ensure that there are no reads
	 * that should have returned a value updated (or merged) between the time of
	 * the read value and the snapshot time itself. read most recent version before the begin timestamp
	 * 
	 * this checks that we have read a coherent snapshot
	 **/
	private boolean checkReadCoherence(TransactionRecord tmpRec) {
		Debug.println(" enter read coherence check " + tmpRec.getTxnId());
		ReadSet rs = tmpRec.getReadSet();
		synchronized (objectUpdates) {
			for (int i = 0; i < rs.getReadSet().length; i++) {
				ReadSetEntry rse = rs.getReadSet()[i];
				updateEntry u = objectUpdates.getUpdates(rse
						.getObjectId());
				if( u != null && (u.isDeleted() == false)){
					if(!rse.getLogicalClock().precedes(tmpRec.getStartClock())){
						//read a value modified after the begin timestamp
						System.out.println("txn" +tmpRec.getTxnId()+" object id: " +
								 rse.getObjectId());
						System.out.println("read entry: " + rse.getLogicalClock().toString() + " not precedes beginTime :" +
								 tmpRec.getStartClock().toString());
						return false;
					}else{
						if (u.lc != null && u.lc.precedes(tmpRec.getStartClock())
								&& ( !u.lc.precedes(rse.getLogicalClock()))){
							//read a value, it has been modified between its timestamp and the begin time
							//this means that it is not from the most recent version
							//TODO: needed or not?
							System.out.println("txn" +tmpRec.getTxnId()+" object id: " +
									 rse.getObjectId());
							System.out.println("update to: " + u.lc.toString() + " precedes beginTime :" +
									 tmpRec.getStartClock().toString());
							System.out.println("but not precede the read entry: " + rse.getLogicalClock().toString());
							return false;
						}
					}
				}
			}
			return true;
		}
	}
	
	/**
	 * Check read conflicts of the transaction. ensure that there are no reads
	 * that should have returned a value updated (or merged) between the time of
	 * the read value and the end time itself.
	 * 
	 * this checks that we have read a coherent snapshot
	 **/
	private boolean checkReadWriteConflicts(TransactionRecord tmpRec) {
		Debug.println(" enter read conflicts check " + tmpRec.getTxnId());
		ReadSetEntry[] rsEntries = tmpRec.getReadSet().getReadSet();
		WriteSetEntry[] wsEntries = tmpRec.getWriteSet().getWriteSet();
		int numOfReadEntries = rsEntries.length;
		int numOfWriteEntries = wsEntries.length;
		synchronized (objectUpdates) {
			//first lock all write entries
			for(int i = 0; i < numOfWriteEntries; i++){
				WriteSetEntry wsE = wsEntries[i];
				updateEntry u = objectUpdates.getUpdates(wsE
						.getObjectId());
				if(u == null){
					Debug.println("Object not exists, add its record " + wsE.getObjectId());
					u = new updateEntry(null, null, false);
					this.objectUpdates.addUpdateTime(wsE.getObjectId(), u);
				}
				u.lock(tmpRec.getTxnId());
			}
			
			//validate if read sets not conflicting with other concurrent transactions
			for(int i = 0; i < numOfReadEntries; i++){
				ReadSetEntry rsE = rsEntries[i];
				updateEntry u = objectUpdates.getUpdates(rsE
						.getObjectId());
				if(u != null){
					if(u.isLockedByOtherTransaction(tmpRec.getTxnId())){
						System.out.println("txn" +tmpRec.getTxnId()+" object id: " +
								 rsE.getObjectId() + " get locked");
						return false;
					}else if(u.lc != null && (!u.lc.precedes(rsE.getLogicalClock()))){
						System.out.println("txn" +tmpRec.getTxnId()+" object id: " +
								 rsE.getObjectId()); 
						System.out.println("update to the object: " + u.lc +" " + u.ts.toLong());
						System.out.println("not precedes RE: " + rsE.getLogicalClock());
						return false;
					}
					/*if((u.isLockedByOtherTransaction(tmpRec.getTxnId())) || 
							(u.lc != null && (!u.lc.precedes(rsE.getLogicalClock())))){
						 System.out.println("txn" +tmpRec.getTxnId()+" object id: " +
								 rsE.getObjectId()); 
						 System.out.println("update to the object: " + u.lc +" " + u.ts.toLong());
						 System.out.println("not precedes RE: " + rsE.getLogicalClock());
						return false;
					}*/
				}
			}
			
			return true;
		}
	}

	/**
	 * abort the transaction. notify proxy and relevant storage servers of the
	 * abort
	 **/
	private void abortTxn(TransactionRecord tmpRec) {
		Debug.println("abort Txn " + tmpRec.getTxnId());
		AckCommitTxnMessage msg2 = mf.borrowAckCommitTxnMessage();
		if(msg2 == null){
			msg2 = new AckCommitTxnMessage(tmpRec.getTxnId(), 0);
		}else{
			msg2.encodeMessage(tmpRec.getTxnId(), 0);
		}
		sendToProxy(msg2, tmpRec.getTxnId().getProxyId());
		if(tmpRec.isConflicting()){
			//faked a commit message to remote
			this.commitConflictingFakedTxn(tmpRec);
		}
		
		records.remove(tmpRec.getTxnId());
		//clean datastructure
		if(tmpRec.msg != null)
			mf.returnProxyCommitMessage(tmpRec.msg);
		mf.returnBeginTxnMessage(tmpRec.bMsg);
		mf.returnAckTxnMessage(tmpRec.aMsg);
		mf.returnAckCommitTxnMessage(msg2);
		tmpRec.reset();
		txnPool.returnObject(tmpRec);
	}

	/**
	 * compute statistic data after each transaction commit and output at certain time
	 **/
	
	public void statisticOutput(TransactionRecord tmpRec){
		long timeNow = System.nanoTime();
		timeToCommit += (timeNow - tmpRec.realTimeStart());
		totalthroughputCount++;
		if(tmpRec.isLocal())
			commitCount++;
		else
			remoteCount++;
		
		if (totalthroughputCount % INTERVAL == 0) {
			System.out
					.println("local thpt (txn/s) "
							+ (float) ((float) commitCount / (float) (timeNow - commitStartTime))
							* (float) (1000) * (float) (1000000));
			commitCount = 0;
			commitStartTime = timeNow;
			System.out
					.println("remote thpt (txn/s) "
							+ (float) ((float) remoteCount / (float) (timeNow - remoteStartTime))
							* (float) (1000) * (float) (1000000));
			remoteCount = 0;
			remoteStartTime = timeNow;

			System.out.println("non-conflict op read abort (txn) "+ nonConflictOpReadAbort);
			System.out.println("conflict op read abort (txn) "+ nonConflictOpReadAbort);
			System.out.println("conflict op write abort (txn) "+nonConflictOpReadAbort);
			System.out.println("manual abort (txn) "+ manualAbort);
			System.out.println("redo (txn) " + redoCount);
			redoCount = 0;
			manualAbort = 0;
			nonConflictOpReadAbort = 0;
			conflictOpReadAbort = 0;
			conflictOpWriteAbort = 0;
			System.out
					.println("global thpt (txn/s) "
							+ (float) ((float) totalthroughputCount / (float) (timeNow - totalthroughputTime))
							* (float) (1000) * (float) (1000000));
			totalthroughputCount = 0;
			totalthroughputTime = timeNow;

			long total = timeNow - msgstart;
			msgstart = timeNow;
			System.out.println("msgs / second: " + (float) (messageCount.get())
					/ (float) (total) * (float) 1000000 * (float) 1000);
			messageCount.set(0);
			System.out.println("msgs         : " + messages[0] + " "
					+ messages[1] + " " + messages[2] + " tot: "
					+ ((float) total / (float) 1000000 / (float) 1000));
			messages[0] = 0;
			messages[1] = 0;
			messages[2] = 0;
		}
	}
	
	/**
	 * 
	 * @param the transaction can be committed, then commit to data writer as well as the remote part
	 */
	Object commitTxnSynObj = new Object();
	private void commitTxn(TransactionRecord tmpRec) {
		synchronized(commitTxnSynObj){
			tmpRec.setMergeClock(this.getNextLogicalClock());
			TimeStamp ns = new TimeStamp(getMyDatacenterId(), nextLocalTxn());
			tmpRec.setFinishTime(ns);
		}
		//commit to data writer
		CommitShadowOpMessage csm = mf.borrowCommitShadowOpMessage();
		if(csm == null){
			csm = new CommitShadowOpMessage(tmpRec.getTxnId(), 
					tmpRec.getShadowOp(), tmpRec.getFinishTime(), tmpRec.getMergeClock());
		}else{
			csm.encodeMessage(tmpRec.getTxnId(), 
					tmpRec.getShadowOp(), tmpRec.getFinishTime(), tmpRec.getMergeClock());
		}
		tmpRec.setCommitShadowOpMessage(csm);
		
		RemoteShadowOpMessage rom = mf.borrowRemoteShadowOpMessage();
		if(rom == null){
			rom = new RemoteShadowOpMessage(tmpRec.getTxnId(), 
				tmpRec.getShadowOp(), tmpRec.getFinishTime(), tmpRec.getMergeClock(), tmpRec.getWriteSet(),
				tmpRec.getOpName());
		}else{
			rom.encodeMessage(tmpRec.getTxnId(), 
					tmpRec.getShadowOp(), tmpRec.getFinishTime(), tmpRec.getMergeClock(), tmpRec.getWriteSet(),
					tmpRec.getOpName());
		}
		tmpRec.setRemoteShadowOpMessage(rom);
		Debug.println("commit to data writer" + csm);
		
		sendToStorage(csm, 0);
		//send to remote site
		
		Debug.println("send to remote data centers " + rom);
		sendToOtherRemoteCoordinator(rom);
	}
	
	/**
	 * This is required to unlock all objects this aborted conflicting txn wants
	 * to modify and clean up the protocol it uses.
	 * @param tmpRec
	 */
	private void unlockObjectsForAbortedConflictTxn(TransactionRecord tmpRec){
		Debug.println("Unlock all objects that locked by " + tmpRec.getTxnId());
		//clean up locked items in the update tables
		WriteSetEntry[] wsEntries = tmpRec.getWriteSet().getWriteSet();
		int numOfWriteEntries = wsEntries.length;
		synchronized (objectUpdates) {
			// first lock all write entries
			for (int i = 0; i < numOfWriteEntries; i++) {
				WriteSetEntry wsE = wsEntries[i];
				updateEntry u = objectUpdates.getUpdates(wsE.getObjectId());
				u.unlock(tmpRec.getTxnId());
			}
		}
		Debug.println("Let's clean up for this aborted conflicting txn " + tmpRec.getTxnId());
		this.getVascoAgent().cleanUpLocalOperation(tmpRec.getTxnId(), tmpRec.getLcRequest());
	}
	
	/**
	 * When a conflicting transaction is about to abort, we cannot simply ignore it
	 * since it already acquires locks, then I need to mimic its commit but without
	 * actually executing it.
	 * @param tmpRec
	 */
	private void commitConflictingFakedTxn(TransactionRecord tmpRec) {
		Debug.println("Commit a conflicting abort transaction");
		
		//send a faked message to remote, and ask remote coordinator to updates the sym/asym meta data
		FakedRemoteShadowOpMessage fShMsg = new FakedRemoteShadowOpMessage(tmpRec.getTxnId(),
				tmpRec.getWriteSet(), tmpRec.getOpName());
		
		Debug.println("send ConflictingFaked to remote data centers " + fShMsg);
		sendToOtherRemoteCoordinator(fShMsg);
	}

	long commitStartTime = System.nanoTime();

	// //////////// maintaining timestamps and counts
	Object txnCountLock = new String();

	private long nextLocalTxn() {
		synchronized (txnCountLock) {
			return ++lastTxnCount;
		}
	}

	public void setLocalTxn(long nv) {
		synchronized (txnCountLock) {
			if (nv > lastTxnCount) {
				lastTxnCount = nv;
			}
		}
	}

	private TimeStamp currentTimeStamp() {
		return new TimeStamp(getMyDatacenterId(), lastTxnCount);
	}

	private LogicalClock currentLogicalClock() {
		synchronized (lastCommittedSynObj) {
			return lastCommitted;
		}
	}
	
	public void updateLastCommittedLogicalClock(LogicalClock lc){
		synchronized(lastCommittedSynObj){
			LogicalClock tmpLc = lastCommitted.maxClock(lc);
			lastCommitted = tmpLc;
		}
	}
	
	private LogicalClock getNextLogicalClock(){
		synchronized (lastCommittedSynObj) {
			lastCommitted.increment(getMyDatacenterId());
			return lastCommitted.copy();
		}
	}
	
	public void updateLastVisibleLogicalClock(LogicalClock lc){
		synchronized(lastVisible){
			lastVisible = lastVisible.maxClock(lc);
		}
	}
	
	private LogicalClock getLastVisibleLogicalClock(){
		synchronized(lastVisible){
			return lastVisible.copy();
		}
	}
	
	//check this
	public void updateObjectTable(WriteSetEntry wse[], LogicalClock lc, TimeStamp ts, 
			ProxyTxnId txnId){
		synchronized (objectUpdates) {
			for (int j = 0; j < wse.length; j++) {
				updateEntry newVersion = null;
				if(wse[j].isDeleted()){
					newVersion = objectUpdates.addUpdateTime(wse[j].getObjectId(),
							new updateEntry(lc, ts, true), txnId);
				}else{
					newVersion = objectUpdates.addUpdateTime(wse[j].getObjectId(),
							new updateEntry(lc, ts, false), txnId);
				}
				
				Debug.println("update object id: " +
						 wse[j].getObjectId() + " by txnid " + txnId);
				Debug.println("new clock : " + newVersion.toString());
			} 
		}
	}
	
	public VascoServiceAgent getVascoAgent() {
		return vascoAgent;
	}


	public void setVascoAgent(VascoServiceAgent vascoAgent) {
		this.vascoAgent = vascoAgent;
	}

	public static void main(String arg[]) {
		if (arg.length != 6) {
			System.out
					.println("usage: Coordinator config.xml dcId coordinatorId threadCount writeRate vascoMemFile");
			System.exit(0);
		}

		NewCoordinator imp = new NewCoordinator(arg[0],
				Integer.parseInt(arg[1]), Integer.parseInt(arg[2]), arg[5]);

		// set up the replicationlayer.core.network.ng for outgoing messages
		NettyTCPSender sendNet = new NettyTCPSender();
		imp.setSender(sendNet);
		sendNet.setTCPNoDelay(false);
		sendNet.setKeepAlive(true);
		// set up the replicationlayer.core.network.ng for incoming messages
		// first, create the pipe from the replicationlayer.core.network.to the coordinator
		int threadcount = Integer.parseInt(arg[3]);
		double writeRate = Double.parseDouble(arg[4]);
		int dcCount = imp.getDatacenterCount();
		int localThreadCount = 0;
		int remoteThreadCount = 0;
		if (dcCount == 1)
			localThreadCount = threadcount;
		else{
			double remoteRatio = (1-1*1.0/dcCount)*writeRate;
			remoteThreadCount = (int) (threadcount*remoteRatio) + 1;
			localThreadCount = threadcount - remoteThreadCount;
		}
		System.out.println("local thread count " + localThreadCount + " remote thread count " + remoteThreadCount);
		ParallelPassThroughNetworkQueue ptnq = new ParallelPassThroughNetworkQueue(
				imp, localThreadCount);

		NettyTCPReceiver rcv = new NettyTCPReceiver(imp.getMembership().getMe()
				.getInetSocketAddress(), ptnq, localThreadCount);
		
		if(imp.getMembership().getDatacenterCount()>1){
			RemoteCoordinator rCoord = new RemoteCoordinator(arg[0],
					Integer.parseInt(arg[1]), Integer.parseInt(arg[2]));
			// set up the replicationlayer.core.network.ng for outgoing messages
			NettyTCPSender sendNet1 = new NettyTCPSender();
			rCoord.setSender(sendNet1);
			sendNet1.setTCPNoDelay(false);
			sendNet1.setKeepAlive(true);

			ParallelPassThroughNetworkQueue ptnq1 = new ParallelPassThroughNetworkQueue(
					rCoord, remoteThreadCount);
			// then create the actual network
			// PassThroughNetworkQueue ptnq = new PassThroughNetworkQueue(imp);

			NettyTCPReceiver rcv1 = new NettyTCPReceiver(rCoord.getMembership().getMe()
					.getInetSocketAddress(), ptnq1, remoteThreadCount);
			rCoord.setCoordinator(imp);
		}

	}
}

