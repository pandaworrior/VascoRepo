package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.ReadWriteSet;
import org.mpi.vasco.util.UnsignedTypes;
import org.mpi.vasco.util.debug.Debug;

public class ProxyCommitMessage extends MessageBase {

    ProxyTxnId txnId;
    ReadWriteSet rwset;
    Operation op;
    int color;

    public ProxyCommitMessage(ProxyTxnId txnId, ReadWriteSet rwset,  Operation op, int c){
	super(MessageTags.PROXYCOMMIT, computeByteSize(txnId, rwset, op,c));
	this.txnId = txnId;
	this.rwset = rwset;
	this.op = op;
	this.color = c;

	int offset = getOffset();
	byte bytes[] = getBytes();

	txnId.getBytes(getBytes(), offset);
	offset += txnId.getByteSize();
	op.getBytes(getBytes(), offset);
	offset += op.getByteSize();
	UnsignedTypes.intToBytes(color, getBytes(), offset);
	offset += UnsignedTypes.uint16Size;
	rwset.getBytes(getBytes(), offset);
	offset += rwset.getByteSize();
	if (bytes.length != offset)
	    throw new RuntimeException("failed to consume entire byte array");
	
    }
    
    public void encodeMessage(ProxyTxnId txnId, ReadWriteSet rwset,  Operation op, int c){
    	this.config(MessageTags.PROXYCOMMIT, computeByteSize(txnId, rwset, op,c));
    	this.txnId = txnId;
    	this.rwset = rwset;
    	this.op = op;
    	this.color = c;

    	int offset = getOffset();
    	byte bytes[] = getBytes();

    	txnId.getBytes(getBytes(), offset);
    	offset += txnId.getByteSize();
    	op.getBytes(getBytes(), offset);
    	offset += op.getByteSize();
    	UnsignedTypes.intToBytes(color, getBytes(), offset);
    	offset += UnsignedTypes.uint16Size;
    	rwset.getBytes(getBytes(), offset);
    	offset += rwset.getByteSize();
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
	color = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	rwset = new ReadWriteSet(b, offset);
	offset += rwset.getByteSize();
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
    	color = UnsignedTypes.bytesToInt(b, offset);
    	offset += UnsignedTypes.uint16Size;
    	rwset = new ReadWriteSet(b, offset);
    	offset += rwset.getByteSize();
    	if (offset != getBytes().length)
    	    throw new RuntimeException("did not consume entire byte array");
    }
    
    public void reset(){
        txnId = null;
        rwset = null;
        op = null;
        color = 0;
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
    
    public boolean isBlue(){
    	if(color == 1){
    		return true;
    	}
    	return false;
    }
    
    public int getColor(){
    	return color;
    }

    static int computeByteSize(ProxyTxnId tx, ReadWriteSet rws, Operation op0, int c){
    	Debug.println(tx.getByteSize() + rws.getByteSize() + op0.getByteSize() + UnsignedTypes.uint16Size);
	return  tx.getByteSize() + rws.getByteSize() + op0.getByteSize() + UnsignedTypes.uint16Size;
    }


    public String toString(){
	return "<"+getTagString()+", "+txnId+", "+rwset + "," + op+ "," + color+">";
    }

	@Override
	public String getTagString() {
		return MessageTags.getString(this.getTag());
	}
}
