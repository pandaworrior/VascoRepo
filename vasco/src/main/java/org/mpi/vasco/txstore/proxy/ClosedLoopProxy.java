package org.mpi.vasco.txstore.proxy;

import org.mpi.vasco.util.ObjectPool;
import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.BaseNode;
import org.mpi.vasco.txstore.coordinator.TransactionRecord;
import org.mpi.vasco.txstore.membership.Role;

import org.mpi.vasco.txstore.messages.MessageTags;

// sending messages
import org.mpi.vasco.txstore.messages.BeginTxnMessage;
import org.mpi.vasco.txstore.messages.FinishTxnMessage;
import org.mpi.vasco.txstore.messages.OperationMessage;

// received messages
import org.mpi.vasco.txstore.messages.AckCommitTxnMessage;
import org.mpi.vasco.txstore.messages.AckTxnMessage;
import org.mpi.vasco.txstore.messages.MessageFactory;
import org.mpi.vasco.txstore.messages.ProxyCommitMessage;
import org.mpi.vasco.txstore.messages.ResultMessage;
import org.mpi.vasco.txstore.messages.CommitTxnMessage;
import org.mpi.vasco.txstore.messages.AbortTxnMessage;
import org.mpi.vasco.txstore.messages.StorageCommitTxnMessage;

import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.ScratchpadFactory;
import org.mpi.vasco.txstore.scratchpad.ScratchpadInterface;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.txstore.storageshim.StorageShim;
import org.mpi.vasco.txstore.util.ProxyTxnIdFactory;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.ReadWriteSet;
import org.mpi.vasco.txstore.util.StorageList;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.Result;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

//for logging
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ClosedLoopProxy extends BaseNode implements ClosedLoopProxyInterface {

	MessageFactory mf;
	ProxyTxnIdFactory txnFac;
	Hashtable<ProxyTxnId, TransactionInfo> transactions;
	long latency = 0;
	long txnNum = 0;

	public long startmi=0;//statistics
    public long endmi=0;//statistics
    public AtomicInteger bluetnxcounter= new AtomicInteger(0) ;//statistics
    public AtomicInteger redtnxcounter= new AtomicInteger(0) ;//statistics
    public AtomicInteger aborttnxcounter= new AtomicInteger(0) ;//statistics
	
	ApplicationInterface app;
	
	Hashtable<ProxyTxnId, ScratchpadInterface> scratchpads;
	ScratchpadFactory spFactory;
	//objectPool
	ObjectPool<TransactionInfo> txnPool;
	
	/*long baseTime = 0;

	private static Logger proxyLogger = Logger.getLogger(Proxy.class.getName());
	private static FileHandler fh;*/

	public ClosedLoopProxy(String file, int dc, int id, ApplicationInterface ap, ScratchpadFactory spF) {
		super(file, dc, Role.PROXY, id);
		Debug.println("Proxy " + id + " @ dc " + dc);
		Debug.println(getMembership().getMe());
		this.mf = new MessageFactory();
		txnFac = new ProxyTxnIdFactory(dc, id);
		transactions = new Hashtable<ProxyTxnId, TransactionInfo>();
		scratchpads = new Hashtable<ProxyTxnId, ScratchpadInterface>();
		app = ap;
		spFactory = spF;
		
		txnPool = new ObjectPool<TransactionInfo>();

		/*String logFileName = "Proxy" + Integer.toString(id) + "-.log";
		try {
			fh = new FileHandler(logFileName, true);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		proxyLogger.setLevel(Level.INFO);
		SimpleFormatter formatter = new SimpleFormatter();
		fh.setFormatter(formatter);
		proxyLogger.addHandler(fh);

		baseTime = System.nanoTime();*/

	}

	// /// the following implement ProxyInterface

	/**
	 * Begin the transaction. Return only when the ack has been received from
	 * the coordinator
	 **/
	public ProxyTxnId beginTxn() {
		BeginTxnMessage msg = mf.borrowBeginTxnMessage();
		if(msg == null){
			msg = new BeginTxnMessage(txnFac.nextTxnId());
		}else{
			msg.encode(txnFac.nextTxnId());
		}
		
		TransactionInfo info  = txnPool.borrowObject();
		if(info == null){
			info = new TransactionInfo(msg.getTxnId());
		}else{
			info.setTxnId(msg.getTxnId());
		}
		info.setBeginTxnMessage(msg);
		info.setStartTime();
		ScratchpadInterface tmp = spFactory.createScratchPad(msg.getTxnId());
		scratchpads.put(msg.getTxnId(), tmp);
		transactions.put(msg.getTxnId(), info);
		sendToCoordinator(msg);
		tmp.beginTransaction(msg.getTxnId());
		info.waitForTimeStamp();
		return msg.getTxnId();
	}

	/**
	 * Execute the specified operation as part of transaction proxyTxnId
	 **/
	public byte[] execute(byte[] op, ProxyTxnId proxyTxnId) {
		return execute(op, proxyTxnId, app.selectStorageServer(op));
	}

	public Result execute(Operation op, ProxyTxnId txnid) {
		return execute(op, txnid, app.selectStorageServer(op.getOperation()));
	}
	
	public ResultSet executeOrig(Operation op, ProxyTxnId txnid) {
		return executeOrig(op, txnid, app.selectStorageServer(op.getOperation()));
	}

	/**
	 * Execute the specified operation as part of transaction proxyTxnId on
	 * storageservrice sid
	 **/
	public byte[] execute(byte[] op, ProxyTxnId proxyTxnId, int sId) {
		// TransactionInfo info = transactions.get(proxyTxnId);
		// int opId = info.nextOperationId();
		// OperationMessage opMsg = new OperationMessage(proxyTxnId, new
		// Operation(op), opId);
		// info.setOperation(opMsg.getOperationId(), opMsg.getOperation(), sId);
		// // sendToStorage(opMsg, sId);
		// sendToStorage(opMsg, sId);
		return execute(new Operation(op), proxyTxnId, sId).getResult();
	}

	public Result execute(Operation op, ProxyTxnId pr, int sid) {
		//long start_time = System.nanoTime() - baseTime;
		TransactionInfo info = transactions.get(pr);
		int opId = info.nextOperationId();
		OperationMessage opMsg = mf.borrowOperationMessage();
		if(opMsg == null)
			opMsg =	new OperationMessage(pr, op, opId);
		else
			opMsg.encodeMessage(pr, op, opId);
		info.setOperation(opMsg.getOperationId(), opMsg.getOperation(), sid);
		Result rs = null;
		try {
			rs = scratchpads.get(pr).execute(op);
		} catch (ScratchpadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mf.returnOperationMessage(opMsg);
		return rs;
	}
	
	public ResultSet executeOrig(Operation op, ProxyTxnId pr, int sid) {
		//long start_time = System.nanoTime() - baseTime;
		TransactionInfo info = transactions.get(pr);
		int opId = info.nextOperationId();
		OperationMessage opMsg = mf.borrowOperationMessage();
		if(opMsg == null)
			opMsg =	new OperationMessage(pr, op, opId);
		else
			opMsg.encodeMessage(pr, op, opId);
		info.setOperation(opMsg.getOperationId(), opMsg.getOperation(), sid);
		ResultSet rs = null;
		try {
			rs = scratchpads.get(pr).executeOrig(op);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mf.returnOperationMessage(opMsg);
		return rs;
	}

	/**
	 * Aborts transaction txn
	 **/
	public void abort(ProxyTxnId txn) {
		//long start_time = System.nanoTime() - baseTime;
		AbortTxnMessage abort = new AbortTxnMessage(txn);
		Debug.println("abort transaction " + txn + " " +abort);
		sendToCoordinator(abort);
		TransactionInfo info = transactions.get(txn);
		ScratchpadInterface tmp = scratchpads.get(txn);
		try {
			tmp.abort();
		} catch (ScratchpadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		////////
		boolean com = info.waitForCommit();
		
    	updateMessageCounter(info,com); // just do statistics
		transactions.remove(txn);
		spFactory.releaseScratchpad(tmp, txn);
		scratchpads.remove(txn);
		mf.returnBeginTxnMessage(info.bMsg);
		mf.returnAckCommitTxnMessage(info.ackMsg);
		info.reset();
		txnPool.returnObject(info);
	}
	
	/**
	 * commit a transaction, send shadowop to coordinator
	 */
	
	public boolean commit(ProxyTxnId txn) {
		throw new RuntimeException("commit in ClosedLoopProxy has not been implemented");
	}
	
	Object txnNumObj = new Object();
	public boolean commit(ProxyTxnId txn, DBShadowOperation op, String opName) {
		TransactionInfo info = transactions.get(txn);
		
		info.setShadowOp(op);
		
		Debug.println("proxy commit " + txn + " " + op  + " count " + info.count);
		ScratchpadInterface tmp = scratchpads.get(txn);
		ReadWriteSet rws = tmp.complete();
		Debug.println("rws " + rws);
		ProxyCommitMessage pcm = mf.borrowProxyCommitMessage();
		if(pcm == null)
			pcm = new ProxyCommitMessage(opName, txn, rws, op);
		else
			pcm.encodeMessage(opName, txn, rws, op);
		info.setProxyCommitMessage(pcm);
		//Debug.println("commit a transaction " + txn + " " + pcm + "\n");
		sendToCoordinator(pcm);
		try {
			tmp.abort();
		} catch (ScratchpadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		///////////
		synchronized(txnNumObj){
			txnNum++;
			latency += transactions.get(txn).getLatency();
			if(txnNum %5000 == 0){
				System.out.println(	"txnNum " + txnNum + " latency    (ms/req) " + latency + " "
						+ (double) ((double) latency
								/ (double) 5000 / (double) 1000000));
				latency = 0;
			}
		}
		//////////
		
		boolean com = info.waitForCommit();
		
    	updateMessageCounter(info,com); // just do statistics
		transactions.remove(txn);
		scratchpads.remove(txn);
		spFactory.releaseScratchpad(tmp, txn);
		mf.returnBeginTxnMessage(info.bMsg);
		mf.returnAckCommitTxnMessage(info.ackMsg);
		mf.returnProxyCommitMessage(info.pcMsg);
		info.reset();
		txnPool.returnObject(info);
		return com;
	}

	/***
	 * handle incoming messages. implements ByteHandler
	 ***/
	public void handle(byte[] b) {
		MessageBase msg = mf.fromBytes(b);
		Debug.println(msg);
		if (msg == null)
			throw new RuntimeException("Should never receive a null message");
		switch (msg.getTag()) {
		case MessageTags.ACKTXN:
			process((AckTxnMessage) msg);
			return;
		case MessageTags.ACKCOMMIT:
			process((AckCommitTxnMessage) msg);
			return;
		case MessageTags.ABORTTXN:
			process((AbortTxnMessage) msg);
			return;
		default:
			throw new RuntimeException("invalid message tag: " + msg.getTag()
					+ " " + MessageTags.RESULT);
		}

	}

	private void process(AckCommitTxnMessage msg) {
		// TODO Auto-generated method stub
		transactions.get(msg.getTxnId()).setAckCommitMessage(msg);
	}

	public void process(AckTxnMessage ack) {
		transactions.get(ack.getTxnId()).setTimeStamp(ack.getTimeStamp());
		mf.returnAckTxnMessage(ack);
	}

	public void process(AbortTxnMessage abort) {
		transactions.get(abort.getTxnId()).setAbortMessage(abort);
              
	}
        
    private void updateMessageCounter(TransactionInfo info,boolean isCommited){
    	long time = System.currentTimeMillis();
		if( time > startmi && time < endmi ){
			if(isCommited){
				//no blue and no red anymore, but keep both for output, the number should be equal to each other
				//which is the total num of committed txns
				bluetnxcounter.incrementAndGet();
				redtnxcounter.incrementAndGet();
			}//commit
			else{
				aborttnxcounter.incrementAndGet();
			}//abort
    	}//within measurement interval
     }

}