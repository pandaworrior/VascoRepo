package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.txstore.util.ProxyTxnId;

import org.mpi.vasco.util.UnsignedTypes;

/**
   Sent from the storage to PROXY AFTER THE PROXY has finished commitiing a transaction
 **/

public class StorageCommitTxnMessage extends MessageBase{

    protected ProxyTxnId proxyTxnId;
    int storageId;

    public StorageCommitTxnMessage(ProxyTxnId txid, int sid){
	super(MessageTags.STORAGECOMMITTXN, computeByteSize(txid));
	proxyTxnId = txid;
	storageId = sid;
	
	int offset = getOffset();
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	UnsignedTypes.intToBytes(sid, getBytes(), offset);
	offset += UnsignedTypes.uint16Size;
	if (offset != getBytes().length)
	    throw new RuntimeException("did not fill up the byte array!");
    }

    public StorageCommitTxnMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.STORAGECOMMITTXN)
	    throw new RuntimeException("Invalid message tag.  looking for "+
				       MessageTags.STORAGECOMMITTXN+ " found "+getTag());
	int offset = getOffset();
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	storageId = UnsignedTypes.bytesToInt(getBytes(),offset);
	offset+= UnsignedTypes.uint16Size;
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array!");
    }

    public ProxyTxnId getTxnId(){
	return proxyTxnId;
    }


    public int getStorageId(){
	return storageId;
    }

    static int computeByteSize(ProxyTxnId proxyTxnId){
	return proxyTxnId.getByteSize() + UnsignedTypes.uint16Size;
    }

    public String toString(){
	return "<"+getTagString()+", "+proxyTxnId+">";
    }


}