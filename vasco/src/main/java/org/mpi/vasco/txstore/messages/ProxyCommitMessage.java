package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.ReadWriteSet;
import org.mpi.vasco.util.debug.Debug;

public class ProxyCommitMessage extends MessageBase {

	String opName;
    ProxyTxnId txnId;
    ReadWriteSet rwset;
    Operation op;

    public ProxyCommitMessage(String opName, ProxyTxnId txnId, ReadWriteSet rwset,  Operation op){
	super(MessageTags.PROXYCOMMIT, computeByteSize(opName, txnId, rwset, op));
	
	Debug.println("computed total size is : " + this.getPayloadSize());
	this.opName = opName;
	this.txnId = txnId;
	this.rwset = rwset;
	this.op = op;

	int offset = getOffset();
	byte bytes[] = getBytes();

	txnId.getBytes(getBytes(), offset);
	offset += txnId.getByteSize();
	op.getBytes(getBytes(), offset);
	offset += op.getByteSize();
	rwset.getBytes(getBytes(), offset);
	offset += rwset.getByteSize();
	byte[] opNameArr = opName.getBytes();
	System.arraycopy(opNameArr, 0, bytes, offset, opNameArr.length);
	offset += opNameArr.length;
	if (bytes.length != offset)
	    throw new RuntimeException("failed to consume entire byte array");
	
    }
    
    public void encodeMessage(String opName, ProxyTxnId txnId, ReadWriteSet rwset,  Operation op){
    	this.config(MessageTags.PROXYCOMMIT, computeByteSize(opName, txnId, rwset, op));
    	this.opName = opName;
    	this.txnId = txnId;
    	this.rwset = rwset;
    	this.op = op;

    	int offset = getOffset();
    	byte bytes[] = getBytes();

    	txnId.getBytes(getBytes(), offset);
    	offset += txnId.getByteSize();
    	op.getBytes(getBytes(), offset);
    	offset += op.getByteSize();
    	rwset.getBytes(getBytes(), offset);
    	offset += rwset.getByteSize();
    	byte[] opNameArr = opName.getBytes();
    	System.arraycopy(opNameArr, 0, bytes, offset, opNameArr.length);
    	offset += opNameArr.length;
    	if (bytes.length != offset)
    	    throw new RuntimeException("failed to consume entire byte array");
    	
    }

    public ProxyCommitMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.PROXYCOMMIT)
	    throw new RuntimeException("invalid message tag.  Found: "+getTag()+" expected: "+
				       MessageTags.PROXYCOMMIT);
	
	txnId = new ProxyTxnId(b, getOffset());
	int offset = getOffset() + txnId.getByteSize();
	op = new Operation(b, offset);
	offset +=op.getByteSize();
	rwset = new ReadWriteSet(b, offset);
	offset += rwset.getByteSize();
	opName = new String(b, offset, b.length - offset);
	offset += b.length - offset;
	if (offset != getBytes().length)
	    throw new RuntimeException("did not consume entire byte array");
    }
    
    public void decodeMessage(byte[] b){
    	this.decodeMessage(b, 0);
    	if (getTag() != MessageTags.PROXYCOMMIT)
    	    throw new RuntimeException("invalid message tag.  Found: "+getTag()+" expected: "+
    				       MessageTags.PROXYCOMMIT);
    	
    	txnId = new ProxyTxnId(b, getOffset());
    	int offset = getOffset() + txnId.getByteSize();
    	op = new Operation(b, offset);
    	offset +=op.getByteSize();
    	rwset = new ReadWriteSet(b, offset);
    	offset += rwset.getByteSize();
    	opName = new String(b, offset, b.length - offset);
    	offset += b.length - offset;
    	if (offset != getBytes().length)
    	    throw new RuntimeException("did not consume entire byte array");
    }
    
    public void reset(){
        txnId = null;
        rwset = null;
        op = null;
        opName = null;
    }
    
    public Operation getShadowOperation(){
    	return op;
    }

    public ProxyTxnId getTxnId(){
	return txnId;
    }
    
    public ReadWriteSet getRwset(){
    	return rwset;
    }

    static int computeByteSize(String opName, ProxyTxnId tx, ReadWriteSet rws, Operation op0){
    	
    	Debug.println("txnId size : " +tx.getByteSize());
    	Debug.println("readwrite set size: " + rws.getByteSize());
    	Debug.println("op size : " + op0.getByteSize());
    	Debug.println("op name size : "+ opName.getBytes().length);
    	return  tx.getByteSize() + rws.getByteSize() + op0.getByteSize() + opName.getBytes().length;
    }


    public String toString(){
	return "<"+getTagString()+", "+txnId+", "+rwset + "," + op +"," + opName + ">";
    }

	@Override
	public String getTagString() {
		return MessageTags.getString(this.getTag());
	}

	public String getOpName() {
		return opName;
	}

	public void setOpName(String opName) {
		this.opName = opName;
	}
}
