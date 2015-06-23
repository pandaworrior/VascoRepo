package org.mpi.vasco.txstore.messages;


import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.ReadWriteSet;


import org.mpi.vasco.util.UnsignedTypes;

/**
   Sent from storage to coordinator when a transaction is finished
 **/
public class ReadWriteSetMessage extends MessageBase{


    ReadWriteSet rwset;
    ProxyTxnId proxyTxnId;
    int storageId;

    public ReadWriteSetMessage(ProxyTxnId proxyTxnId, ReadWriteSet rwset, int storageId){
	super(MessageTags.READWRITESET, computeByteSize(proxyTxnId, rwset));
	this.proxyTxnId = proxyTxnId;
	this.rwset = rwset;
	this.storageId = storageId;
	int offset = getOffset();

	UnsignedTypes.intToBytes(storageId, getBytes(), offset);
	offset += UnsignedTypes.uint16Size;
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	rwset.getBytes(getBytes(), offset);
	offset += rwset.getByteSize();
	if (offset!=getBytes().length)
	    throw new RuntimeException("invalid offset!");
    }

    public ReadWriteSetMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.READWRITESET)
	    throw new RuntimeException("Invalid message tag.  looking for"+
				       MessageTags.READWRITESET+" found "+getTag());
	int offset = getOffset();
	storageId = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	rwset = new ReadWriteSet(b, offset);
	offset += rwset.getByteSize();
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array "+offset+" of "+b.length);
    }

    public ProxyTxnId getTxnId(){
	return proxyTxnId;
    }

    public ReadWriteSet getReadWriteSet(){
	return rwset;
    }

    public int getStorageId(){
	return storageId;
    }

    static int computeByteSize(ProxyTxnId tx, ReadWriteSet rwset){
	return tx.getByteSize() + rwset.getByteSize() + UnsignedTypes.uint16Size;
    }

    public String toString(){
	return "<"+getTagString()+", "+proxyTxnId+", "+rwset+">";
    }
			    
}