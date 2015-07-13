package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.ProxyTxnId;

import org.mpi.vasco.util.UnsignedTypes;

/**
   Sent from the storage to the coordinator when a remote transaction is ready for completion
 **/

public class TxnReadyMessage extends MessageBase{

    protected ProxyTxnId proxyTxnId;
    int storageId;

    public TxnReadyMessage(ProxyTxnId txid, int sid){
	super(MessageTags.TXNREADY, computeByteSize(txid));
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

    public TxnReadyMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.TXNREADY)
	    throw new RuntimeException("Invalid message tag.  looking for "+
				       MessageTags.TXNREADY+ " found "+getTag());
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

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTagString() {
		return MessageTags.getString(this.getTag());
	}


}