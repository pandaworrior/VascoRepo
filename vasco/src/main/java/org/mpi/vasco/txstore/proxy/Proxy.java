package org.mpi.vasco.txstore.proxy;

import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.BaseNode;
import org.mpi.vasco.txstore.membership.Role;

import org.mpi.vasco.txstore.messages.MessageTags;

// sending messages
import org.mpi.vasco.txstore.messages.BeginTxnMessage;
import org.mpi.vasco.txstore.messages.FinishTxnMessage;
import org.mpi.vasco.txstore.messages.OperationMessage;

// received messages
import org.mpi.vasco.txstore.messages.AckTxnMessage;
import org.mpi.vasco.txstore.messages.MessageFactory;
import org.mpi.vasco.txstore.messages.ResultMessage;
import org.mpi.vasco.txstore.messages.CommitTxnMessage;
import org.mpi.vasco.txstore.messages.AbortTxnMessage;
import org.mpi.vasco.txstore.messages.StorageCommitTxnMessage;

import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.txstore.storageshim.StorageShim;
import org.mpi.vasco.txstore.util.ProxyTxnIdFactory;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.StorageList;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.Result;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.Hashtable;

//for logging
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Proxy extends BaseNode implements ClosedLoopProxyInterface {

	MessageFactory mf;
	ProxyTxnIdFactory txnFac;
	Hashtable<ProxyTxnId, TransactionInfo> transactions;

	int commit = 0;

	ApplicationInterface app;

	/*long baseTime = 0;

	private static Logger proxyLogger = Logger.getLogger(Proxy.class.getName());
	private static FileHandler fh;*/

	public Proxy(String file, int dc, int id, ApplicationInterface ap) {
		super(file, dc, Role.PROXY, id);
		Debug.println("Proxy " + id + " @ dc " + dc);
		Debug.println(getMembership().getMe());
		this.mf = new MessageFactory();
		txnFac = new ProxyTxnIdFactory(dc, id);
		transactions = new Hashtable<ProxyTxnId, TransactionInfo>();
		app = ap;

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
		//long start_time = System.nanoTime() - baseTime;
		BeginTxnMessage msg = new BeginTxnMessage(txnFac.nextTxnId());
		TransactionInfo info = new TransactionInfo(msg.getTxnId());
		transactions.put(msg.getTxnId(), info);
		sendToCoordinator(msg);
		// wait for the begin ack to be received
		info.waitForTimeStamp();
		/*long end_time = System.nanoTime() - baseTime;
		String tmpStr = "txnId|" + msg.getTxnId().toString()
				+ "|START BEGIN TXN|" + Long.toString(start_time);
		proxyLogger.log(Level.INFO, tmpStr);
		tmpStr = "txnId|" + msg.getTxnId().toString() + "|END BEGIN TXN|"
				+ Long.toString(end_time);
		proxyLogger.log(Level.INFO, tmpStr);*/
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
		OperationMessage opMsg = new OperationMessage(pr, op, opId);
		info.setOperation(opMsg.getOperationId(), opMsg.getOperation(), sid);
		// sendToStorage(opMsg, sId);
		sendToStorage(opMsg, sid);
		// return transactions.get(pr).waitForResult(opId);
		Result rs = transactions.get(pr).waitForResult(opId);
		/*long end_time = System.nanoTime() - baseTime;

		String tmpStr = "txnId|" + pr.toString() + "|START EXEC OP|"
				+ Long.toString(start_time);
		proxyLogger.log(Level.INFO, tmpStr);
		tmpStr = "txnId|" + pr.toString() + "|END EXEC OP|"
				+ Long.toString(end_time);
		proxyLogger.log(Level.INFO, tmpStr);*/
		return rs;
	}

	/**
	 * Aborts transaction txn
	 **/
	public void abort(ProxyTxnId txn) {
		//long start_time = System.nanoTime() - baseTime;
		AbortTxnMessage abort = new AbortTxnMessage(txn);
		sendToCoordinator(abort);
		StorageList slist = transactions.get(txn).getStorageList();
		for (int i = 0; i < slist.getStorageCount(); i++)
			sendToStorage(abort, slist.getStorage(i));
		transactions.remove(txn);
		/*long end_time = System.nanoTime() - baseTime;
		String tmpStr = "txnId|" + txn.toString() + "|START ABORT TXN|"
				+ Long.toString(start_time);
		proxyLogger.log(Level.INFO, tmpStr);
		tmpStr = "txnId|" + txn.toString() + "|END ABORT TXN|"
				+ Long.toString(end_time);
		proxyLogger.log(Level.INFO, tmpStr);*/
	}

	/**
	 * Attempt to commit the transaction. Returns true if the transaction
	 * commits, false otherwise
	 **/
	public boolean commit(ProxyTxnId txn) {
		//long start_time = System.nanoTime() - baseTime;
		StorageList slist = transactions.get(txn).getStorageList();
		FinishTxnMessage ftm = new FinishTxnMessage(txn, slist);
		sendToCoordinator(ftm);
		for (int i = 0; i < slist.getStorageCount(); i++)
			sendToStorage(ftm, slist.getStorage(i));
		boolean com = transactions.get(txn).waitForCommit();
		transactions.remove(txn);
		/*long end_time = System.nanoTime() - baseTime;
		String tmpStr = "txnId|" + txn.toString() + "|START COMMIT TXN|"
				+ Long.toString(start_time);
		proxyLogger.log(Level.INFO, tmpStr);
		tmpStr = "txnId|" + txn.toString() + "|END COMMIT TXN|"
				+ Long.toString(end_time);
		proxyLogger.log(Level.INFO, tmpStr);*/
		return com;
	}

	/***
	 * handle incoming messages. implements ByteHandler
	 ***/
	public void handle(byte[] b) {
		MessageBase msg = mf.fromBytes(b);
//		System.out.println(msg);
		if (msg == null)
			throw new RuntimeException("Should never receive a null message");
		switch (msg.getTag()) {
		case MessageTags.ACKTXN:
			process((AckTxnMessage) msg);
			return;
		case MessageTags.RESULT:
			process((ResultMessage) msg);
			return;
		case MessageTags.COMMITTXN:
			process((CommitTxnMessage) msg);
			return;
                case MessageTags.STORAGECOMMITTXN:
                        process((StorageCommitTxnMessage) msg);
                        return;
		case MessageTags.ABORTTXN:
			process((AbortTxnMessage) msg);
			return;
		default:
			throw new RuntimeException("invalid message tag: " + msg.getTag()
					+ " " + MessageTags.RESULT);
		}

	}

	public void process(AckTxnMessage ack) {
		transactions.get(ack.getTxnId()).setTimeStamp(ack.getTimeStamp());
	}

	public void process(ResultMessage res) {
		transactions.get(res.getTxnId()).setResult(res.getResult(),
				res.getOperationId());
	}

	public void process(AbortTxnMessage abort) {
		transactions.get(abort.getTxnId()).setAbortMessage(abort);
              
	}

	public void process(CommitTxnMessage com) {
		transactions.get(com.getTxnId()).setCommitMessage(com);
	}
        
        public void process(StorageCommitTxnMessage com){
            transactions.get(com.getTxnId()).setStorageCommitTxnMessage(com);
            
        }

		public boolean commit(ProxyTxnId txId, DBShadowOperation op, int color) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public ResultSet executeOrig(Operation op, ProxyTxnId txnid) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ResultSet executeOrig(Operation op, ProxyTxnId pr, int sid) {
			// TODO Auto-generated method stub
			return null;
		}

}