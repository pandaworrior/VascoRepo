package org.mpi.vasco.txstore.messages;


import org.mpi.vasco.txstore.util.ProxyTxnId;


import org.mpi.vasco.util.UnsignedTypes;

/**
   SENT BETWEEn coordinators when one coordinator wants the token but does not yet have it
 **/
public class GimmeTheBlueMessage extends MessageBase{


    ProxyTxnId proxyTxnId;
    long opId;

    public GimmeTheBlueMessage(ProxyTxnId proxyTxnId, long opId){
	super(MessageTags.GIMMETHEBLUE, computeByteSize(proxyTxnId));
	this.proxyTxnId = proxyTxnId;
	int offset = getOffset();
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	this.opId = opId;
	UnsignedTypes.longlongToBytes(opId, getBytes(), offset);
	offset += UnsignedTypes.uint64Size;
	if (offset!=getBytes().length)
	    throw new RuntimeException("invalid offset!");
    }

    public GimmeTheBlueMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.GIMMETHEBLUE)
	    throw new RuntimeException("Invalid message tag.  looking for"+
				       MessageTags.GIMMETHEBLUE+" found "+getTag());
	int offset = getOffset();
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	opId = UnsignedTypes.bytesToLongLong(b, offset);
	offset += UnsignedTypes.uint64Size;
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array");
    }

    public ProxyTxnId getTxnId(){
	return proxyTxnId;
    }

    public long getBlueEpoch(){
	return opId;
    }

    static int computeByteSize(ProxyTxnId tx){
	return tx.getByteSize() + UnsignedTypes.uint64Size;
    }

    public String toString(){
	return "<"+getTagString()+", "+proxyTxnId+", "+getBlueEpoch()+">";
    }
			    
}