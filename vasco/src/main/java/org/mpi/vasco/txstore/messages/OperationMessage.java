package org.mpi.vasco.txstore.messages;


import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.Operation;


import org.mpi.vasco.util.UnsignedTypes;

public class OperationMessage extends MessageBase{


    Operation op;
    ProxyTxnId proxyTxnId;
    int opId;

    public OperationMessage(ProxyTxnId proxyTxnId, Operation op, int opId){
	super(MessageTags.OPERATION, computeByteSize(proxyTxnId, op));
	this.proxyTxnId = proxyTxnId;
	this.op = op;
	this.opId = opId;
	int offset = getOffset();
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	op.getBytes(getBytes(), offset);
	offset += op.getByteSize();
	UnsignedTypes.intToBytes(opId, getBytes(), offset);
	offset += UnsignedTypes.uint16Size;
	if (offset!=getBytes().length)
	    throw new RuntimeException("invalid offset!");
    }
    
    public void encodeMessage(ProxyTxnId proxyTxnId, Operation op, int opId){
    	this.config(MessageTags.OPERATION, computeByteSize(proxyTxnId, op));
    	this.proxyTxnId = proxyTxnId;
    	this.op = op;
    	this.opId = opId;
    	int offset = getOffset();
    	proxyTxnId.getBytes(getBytes(), offset);
    	offset += proxyTxnId.getByteSize();
    	op.getBytes(getBytes(), offset);
    	offset += op.getByteSize();
    	UnsignedTypes.intToBytes(opId, getBytes(), offset);
    	offset += UnsignedTypes.uint16Size;
    	if (offset!=getBytes().length)
    	    throw new RuntimeException("invalid offset!");
    }

    public OperationMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.OPERATION)
	    throw new RuntimeException("Invalid message tag.  looking for"+
				       MessageTags.OPERATION+" found "+getTag());
	int offset = getOffset();
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	op = new Operation(b, offset);
	offset += op.getByteSize();
	opId = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array");
    }
    
    public void decodeMessage(byte[] b){
    	this.decodeMessage(b, 0);
    	if (getTag() != MessageTags.OPERATION)
    	    throw new RuntimeException("Invalid message tag.  looking for"+
    				       MessageTags.OPERATION+" found "+getTag());
    	int offset = getOffset();
    	proxyTxnId = new ProxyTxnId(b, offset);
    	offset += proxyTxnId.getByteSize();
    	op = new Operation(b, offset);
    	offset += op.getByteSize();
    	opId = UnsignedTypes.bytesToInt(b, offset);
    	offset += UnsignedTypes.uint16Size;
    	if (offset != b.length)
    	    throw new RuntimeException("did not consume the entire byte array");
    }
    
    public void reset(){
        op = null;
        proxyTxnId = null;
        opId = 0;
    }

    public ProxyTxnId getTxnId(){
	return proxyTxnId;
    }

    public Operation getOperation(){
	return op;
    }

    public int getOperationId(){
	return opId;
    }

    static int computeByteSize(ProxyTxnId tx, Operation op){
	return tx.getByteSize() + op.getByteSize() + UnsignedTypes.uint16Size;
    }

    public String toString(){
	return "<"+getTagString()+", "+proxyTxnId+", "+op+">";
    }
			    
}