package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.txstore.util.WriteSet;

public class FakedRemoteShadowOpMessage extends MessageBase{
	ProxyTxnId txnId;
    WriteSet wset;
    String opName;

    public FakedRemoteShadowOpMessage(ProxyTxnId txnId, WriteSet wset, String opName){
	super(MessageTags.FAKEDREMOTESHADOW, computeByteSize(txnId, wset, opName));
	this.txnId = txnId;
	this.wset = wset;
	this.opName = opName;

	int offset = getOffset();
	byte bytes[] = getBytes();
	
	txnId.getBytes(getBytes(), offset);
	offset += txnId.getByteSize();
	wset.getBytes(getBytes(), offset);
	offset += wset.getByteSize();
	byte[] opNameArr = opName.getBytes();
	System.arraycopy(opNameArr, 0, getBytes(), offset, opNameArr.length);
	offset += opNameArr.length;
	if (bytes.length != offset)
	    throw new RuntimeException("failed to consume entire byte array");
	
    }
    
    public void encodeMessage(ProxyTxnId txnId, WriteSet wS, String opN){
    	this.txnId = txnId;
    	wset = wS;
    	this.opName = opN;
    	this.config(MessageTags.FAKEDREMOTESHADOW, computeByteSize(this.txnId, wset, opName));
    	int offset = getOffset();

    	byte bytes[] = getBytes();
    	txnId.getBytes(getBytes(), offset);
    	offset += txnId.getByteSize();
    	wset.getBytes(getBytes(), offset);
    	offset += wset.getByteSize();
    	byte[] opNameArr = opName.getBytes();
    	System.arraycopy(opNameArr, 0, getBytes(), offset, opNameArr.length);
    	offset += opNameArr.length;
    	if (bytes.length != offset)
    	    throw new RuntimeException("failed to consume entire byte array");
    }

    public FakedRemoteShadowOpMessage(byte[] b){
		super(b);
		if (getTag() != MessageTags.FAKEDREMOTESHADOW)
		    throw new RuntimeException("invalid message tag.  Found: "+getTag()+" expected: "+
					       MessageTags.FAKEDREMOTESHADOW);
		
		int offset = getOffset();
		txnId = new ProxyTxnId(b, offset);
		offset += txnId.getByteSize();
		wset = new WriteSet(b, offset);
		offset += wset.getByteSize();
		opName = new String(b, offset, b.length - offset);
		offset += b.length - offset;
		if (offset != getBytes().length)
		    throw new RuntimeException("did not consume entire byte array");
    }
    
    
    public void decodeMessage(byte[] b){
    	this.decodeMessage(b, 0);
    	if (getTag() != MessageTags.FAKEDREMOTESHADOW)
    	    throw new RuntimeException("invalid message tag.  Found: "+getTag()+" expected: "+
    				       MessageTags.FAKEDREMOTESHADOW);
    	
    	int offset = getOffset();
    	txnId = new ProxyTxnId(b, offset);
		offset += txnId.getByteSize();
    	wset = new WriteSet(b, offset);
    	offset += wset.getByteSize();
    	opName = new String(b, offset, b.length - offset);
    	offset += b.length - offset;
    	if (offset != getBytes().length)
    	    throw new RuntimeException("did not consume entire byte array");
    }
    
    public void reset(){
    	txnId = null;
        wset = null;
        opName = null;
    }
    
    public ProxyTxnId getTxnId(){
    	return this.txnId;
    }
    
    public WriteSet getWriteset(){
    	return wset;
    }
    
    public String getOpName(){
    	return this.opName;
    }

    static int computeByteSize(ProxyTxnId txnId, WriteSet ws, String opName){
    	return txnId.getByteSize() + ws.getByteSize() + opName.getBytes().length;
    }


    public String toString(){
    	return "<"+ txnId + "," + wset + "," + opName + ">";
    }

	@Override
	public String getTagString() {
		return MessageTags.getString(this.getTag());
	}

}
