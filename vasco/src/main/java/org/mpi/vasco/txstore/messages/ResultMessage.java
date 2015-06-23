package org.mpi.vasco.txstore.messages;


import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.Result;

import org.mpi.vasco.util.UnsignedTypes;

/**
   Sent from Storage to Proxy with the result of executing an operation
 **/
public class ResultMessage extends MessageBase{


    Result op;
    ProxyTxnId proxyTxnId;
    int opId;

    public ResultMessage(ProxyTxnId proxyTxnId, Result op, int opId){
	super(MessageTags.RESULT, computeByteSize(proxyTxnId, op));
	this.proxyTxnId = proxyTxnId;
	this.op = op;
	int offset = getOffset();
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	op.getBytes(getBytes(), offset);
	offset += op.getByteSize();
	this.opId = opId;
	UnsignedTypes.intToBytes(opId, getBytes(), offset);
	offset += UnsignedTypes.uint16Size;
	if (offset!=getBytes().length)
	    throw new RuntimeException("invalid offset!");
    }

    public ResultMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.RESULT)
	    throw new RuntimeException("Invalid message tag.  looking for"+
				       MessageTags.RESULT+" found "+getTag());
	int offset = getOffset();
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	op = new Result(b, offset);
	offset += op.getByteSize();
	opId = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array");
    }

    public ProxyTxnId getTxnId(){
	return proxyTxnId;
    }

    public Result getResult(){
	return op;
    }

    public int getOperationId(){
	return opId;
    }

    static int computeByteSize(ProxyTxnId tx, Result op){
	return tx.getByteSize() + op.getByteSize() + UnsignedTypes.uint16Size;
    }

    public String toString(){
	return "<"+getTagString()+", "+proxyTxnId+", "+op+">";
    }
			    
}