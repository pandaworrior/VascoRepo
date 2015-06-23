package org.mpi.vasco.txstore.messages;


public class MessageTags {

	// /// PROCESSING LOCAL TRANSACTIONS

	public final static int BEGINTXN = 1; // proxy to coordinator to
	// start
	public final static int ACKTXN = 2; // coordinator to proxy with
	// initial TS
	public final static int FINISHTXN = 3; // proxy to
	// coordinator/storage when
	// done
	public final static int ABORTTXN = 4; // coordinator to
	// proxy/storage on abort
	public final static int COMMITTXN = 5; // coordinator to
	// proxy/storage on commit
	public final static int OPERATION = 6; // proxy to storage with an
	// operation
	public final static int RESULT = 7; // storage to proxy with a
	// result
	public final static int READWRITESET = 8; // storage to
	// coordinator with a
	// read/write set

	// /// PROCESSING REMOTE TRANSACTIONS

	public final static int OPERATIONENCODING = 9; // storage to
	// remote storage
	public final static int TXNREADY = 10; // storage to coordinator
	// when a remote
	// transaction is ready to
	// commit
	public final static int FINISHREMOTE = 11; // coordiantor to
	// storage when it is
	// time to make a
	// remote transaction
	// visible
	public final static int TXNMETAINFORMATION = 12; // sent between
	// coordinators
	// to announce
	// remote
	// transactions

	public final static int GIMMETHEBLUE = 13; // sent between
	// coordinators when
	// one coordinator is
	// ready for the blue
	// token but does not hold it
	public final static int STORAGECOMMITTXN = 14;

	public final static int BLUETOKENGRANT = 15;
	
	public final static int BLUETOKENGRANTACK = 16;
	
	public final static int PROXYCOMMIT = 17;
	
	public final static int COMMITSHADOW = 18;
	
	public final static int ACKCOMMIT = 19;
	
	public final static int REMOTESHADOW = 20;

	public final static String getString(int i) {
		switch (i) {
		case MessageTags.BEGINTXN:
			return "BEGINTXN";
		case MessageTags.ACKTXN:
			return "ACKTXN";
		case MessageTags.RESULT:
			return " Result";
		case MessageTags.FINISHTXN:
			return " FinishTxn";
		case MessageTags.OPERATION:
			return " Operation";
		case MessageTags.READWRITESET:
			return " ReadWriteSet";
		case MessageTags.COMMITTXN:
			return " CommitTxn";
		case MessageTags.ABORTTXN:
			return " AbortTxn";
		case MessageTags.OPERATIONENCODING:
			return " OperationEncoding";
		case MessageTags.TXNREADY:
			return " TxnReady";
		case MessageTags.TXNMETAINFORMATION:
			return " TxnMetaInformation";
		case MessageTags.FINISHREMOTE:
			return " FinishRemote";
		case MessageTags.GIMMETHEBLUE:
			return " GimmeTheBlue";
		case MessageTags.STORAGECOMMITTXN:
			return " StorageCommitTxn";
		case MessageTags.BLUETOKENGRANT:
			return "BlueTokenGrant";
		case MessageTags.BLUETOKENGRANTACK:
			return "BlueTokenGrantAck";
		case MessageTags.COMMITSHADOW:
			return "CommitShadowOp";
		case MessageTags.PROXYCOMMIT:
			return "ProxyCommit";
		case MessageTags.ACKCOMMIT:
			return "AckCommit";
		case MessageTags.REMOTESHADOW:
			return "RemoteShadow";
		default:

			throw new RuntimeException("Invalid message tag:  " + i);
		}
	}

}