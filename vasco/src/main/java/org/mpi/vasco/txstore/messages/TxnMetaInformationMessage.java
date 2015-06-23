package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.StorageList;
import org.mpi.vasco.txstore.util.WriteSet;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.TimeStamp;

import org.mpi.vasco.util.UnsignedTypes;

/**
   Sent from the coordinator to the proxy and the storage when a transaction must be aborted.
 **/

public class TxnMetaInformationMessage extends MessageBase{

    protected ProxyTxnId proxyTxnId;
    StorageList storageList;
    WriteSet writeSet;
    LogicalClock logicalClock;
    //    Dependencies dependencies;
    TimeStamp timeStamp;


    public TxnMetaInformationMessage(ProxyTxnId txid, StorageList slist,
				     WriteSet wset, LogicalClock lclock,
				     //Dependencies dep,
				     TimeStamp ts){
	super(MessageTags.TXNMETAINFORMATION, computeByteSize(txid, slist, wset,
							      lclock, //dep,
							      ts));
	proxyTxnId = txid;
	storageList = slist;
	writeSet = wset;
	logicalClock = lclock;
	//	dependencies = dep;
	timeStamp = ts;
	

	int offset = getOffset();
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	storageList.getBytes(getBytes(), offset);
	offset += storageList.getByteSize();
	logicalClock.getBytes(getBytes(), offset);
	offset += logicalClock.getByteSize();
	//dependencies.getBytes(getBytes(), offset);
	//offset += dependencies.getByteSize();
	writeSet.getBytes(getBytes(), offset);
	offset += writeSet.getByteSize();
	timeStamp.getBytes(getBytes(), offset);
	offset += timeStamp.getByteSize();

	if (offset != getBytes().length)
	    throw new RuntimeException("did not fill up the byte array!");
    }

    public TxnMetaInformationMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.TXNMETAINFORMATION)
	    throw new RuntimeException("Invalid message tag.  looking for "+
				       MessageTags.TXNMETAINFORMATION+ " found "+getTag());
	int offset = getOffset();
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	storageList = new StorageList(b, offset);
	offset += storageList.getByteSize();
	logicalClock = new LogicalClock(b, offset);
	offset += logicalClock.getByteSize();
	//dependencies = new Dependencies(b, offset);
	//offset += dependencies.getByteSize();
	writeSet = new WriteSet(b, offset);
	offset += writeSet.getByteSize();
	timeStamp = new TimeStamp(b, offset);
	offset += timeStamp.getByteSize();
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array!");
    }

    public ProxyTxnId getTxnId(){
	return proxyTxnId;
    }


    public StorageList getStorageList(){
	return storageList;
    }

    public WriteSet getWriteSet(){
	return writeSet;
    }

    //    public Dependencies getDependencies(){
    //	return dependencies;
    //}

    public LogicalClock getLogicalClock(){
	return logicalClock;
    }

    public TimeStamp getTimeStamp(){
	return timeStamp;
    }



    static int computeByteSize(ProxyTxnId proxyTxnId, StorageList slist, 
			       WriteSet wset, LogicalClock lclock,// Dependencies dep, 
			       TimeStamp ts){
	return proxyTxnId.getByteSize() + slist.getByteSize()+ wset.getByteSize() +
	    lclock.getByteSize() + //dep.getByteSize() +
	    ts.getByteSize();
    }

    public String toString(){
	return "<"+getTagString()+", "+proxyTxnId+", "+storageList +", "+writeSet+", "
	    +logicalClock+", "//+dependencies
	    +", "+timeStamp+">";
    }


}