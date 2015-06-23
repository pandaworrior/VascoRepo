package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.util.UnsignedTypes;

public class AckCommitTxnMessage extends MessageBase {
	protected ProxyTxnId proxyTxnId;
    protected int success;
    
    public AckCommitTxnMessage(ProxyTxnId txid, int outcome){
	super(MessageTags.ACKCOMMIT, computeByteSize(txid, outcome));
	proxyTxnId = txid;
	this.success = outcome;
	
	int offset = getOffset();
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	UnsignedTypes.intToBytes(success, getBytes(), offset);
	offset += UnsignedTypes.uint16Size;
	if (offset != getBytes().length)
	    throw new RuntimeException("did not consume the entire byte array!");

    }
    
    public void encodeMessage(	ProxyTxnId id, int s){
    	proxyTxnId = id;
    	success = s;
    	this.config(MessageTags.ACKCOMMIT, computeByteSize(proxyTxnId, success));
    	int offset = getOffset();
    	proxyTxnId.getBytes(getBytes(), offset);
    	offset += proxyTxnId.getByteSize();
    	UnsignedTypes.intToBytes(success, getBytes(), offset);
    	offset += UnsignedTypes.uint16Size;
    	if (offset != getBytes().length)
    	    throw new RuntimeException("did not consume the entire byte array!");
    }

    public AckCommitTxnMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.ACKCOMMIT)
	    throw new RuntimeException("Invalid message tag.  looking for "+
				       MessageTags.ACKCOMMIT+ " found "+getTag());
	int offset = getOffset();
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	success = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array!");
    }
    
    public void decodeMessage(byte[] b){
    	this.decodeMessage(b, 0);
    	if (getTag() != MessageTags.ACKCOMMIT)
    	    throw new RuntimeException("Invalid message tag.  looking for "+
    				       MessageTags.ACKCOMMIT+ " found "+getTag());
    	int offset = getOffset();
    	proxyTxnId = new ProxyTxnId(b, offset);
    	offset += proxyTxnId.getByteSize();
    	success = UnsignedTypes.bytesToInt(b, offset);
    	offset += UnsignedTypes.uint16Size;
    	if (offset != b.length)
    	    throw new RuntimeException("did not consume the entire byte array!");
    }
    
    public void reset(){
    	proxyTxnId = null;
        success = 0;
    }

    public ProxyTxnId getTxnId(){
	return proxyTxnId;
    }

    public boolean getOutcome(){
    	if(success == 1){
    		return true;
    	}
    	return false;
    }

    static int computeByteSize(ProxyTxnId proxyTxnId, int outcome){
	return proxyTxnId.getByteSize()+ UnsignedTypes.uint16Size;
    }

    public String toString(){
	return "<"+getTagString()+", "+proxyTxnId+", "+success+">";
    }

}
