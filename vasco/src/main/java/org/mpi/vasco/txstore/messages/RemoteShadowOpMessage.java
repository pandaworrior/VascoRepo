package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.txstore.util.WriteSet;

public class RemoteShadowOpMessage extends MessageBase{
    ProxyTxnId txnId;
    Operation op;
    protected TimeStamp timestamp;
    protected LogicalClock logicalClock;
    WriteSet wset;
    String opName;

    public RemoteShadowOpMessage(ProxyTxnId txnId,  Operation op, TimeStamp ts, LogicalClock lc, WriteSet wset,
    		String opName){
	super(MessageTags.REMOTESHADOW, computeByteSize(txnId, op, ts, lc, wset, opName));
	this.txnId = txnId;
	this.op = op;
	this.timestamp = ts;
	this.logicalClock = lc;
	this.wset = wset;
	this.opName = opName;

	int offset = getOffset();

	byte bytes[] = getBytes();

	txnId.getBytes(getBytes(), offset);
	offset += txnId.getByteSize();
	op.getBytes(getBytes(), offset);
	offset += op.getByteSize();
	timestamp.getBytes(getBytes(), offset);
	offset += timestamp.getByteSize();
	logicalClock.getBytes(getBytes(), offset);
	offset += logicalClock.getByteSize();
	wset.getBytes(getBytes(), offset);
	offset += wset.getByteSize();
	byte[] opNameArr = opName.getBytes();
	System.arraycopy(opNameArr, 0, getBytes(), offset, opNameArr.length);
	offset += opNameArr.length;
	if (bytes.length != offset)
	    throw new RuntimeException("failed to consume entire byte array");
	
    }
    
    public void encodeMessage(    ProxyTxnId id,  Operation o,TimeStamp ts,
    LogicalClock lc, WriteSet wS, String opN){
    	txnId = id;
    	op = o;
    	timestamp = ts;
    	logicalClock = lc;
    	wset = wS;
    	this.opName = opN;
    	this.config(MessageTags.REMOTESHADOW, computeByteSize(txnId, op, timestamp, logicalClock, wset, opN));
    	int offset = getOffset();

    	byte bytes[] = getBytes();

    	txnId.getBytes(getBytes(), offset);
    	offset += txnId.getByteSize();
    	op.getBytes(getBytes(), offset);
    	offset += op.getByteSize();
    	timestamp.getBytes(getBytes(), offset);
    	offset += timestamp.getByteSize();
    	logicalClock.getBytes(getBytes(), offset);
    	offset += logicalClock.getByteSize();
    	wset.getBytes(getBytes(), offset);
    	offset += wset.getByteSize();
    	byte[] opNameArr = opName.getBytes();
    	System.arraycopy(opNameArr, 0, getBytes(), offset, opNameArr.length);
    	offset += opNameArr.length;
    	if (bytes.length != offset)
    	    throw new RuntimeException("failed to consume entire byte array");
    }

    public RemoteShadowOpMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.REMOTESHADOW)
	    throw new RuntimeException("invalid message tag.  Found: "+getTag()+" expected: "+
				       MessageTags.REMOTESHADOW);
	
	txnId = new ProxyTxnId(b, getOffset());
	int offset = getOffset() + txnId.getByteSize();
	op = new Operation(b, offset);
	offset +=op.getByteSize();
	timestamp = new TimeStamp(b, offset);
	offset += timestamp.getByteSize();
	logicalClock = new LogicalClock(b, offset);
	offset += logicalClock.getByteSize();
	wset = new WriteSet(b, offset);
	offset += wset.getByteSize();
	opName = new String(b, offset, b.length - offset);
	offset += b.length - offset;
	if (offset != getBytes().length)
	    throw new RuntimeException("did not consume entire byte array");
    }
    
    
    public void decodeMessage(byte[] b){
    	this.decodeMessage(b, 0);
    	if (getTag() != MessageTags.REMOTESHADOW)
    	    throw new RuntimeException("invalid message tag.  Found: "+getTag()+" expected: "+
    				       MessageTags.REMOTESHADOW);
    	
    	txnId = new ProxyTxnId(b, getOffset());
    	int offset = getOffset() + txnId.getByteSize();
    	op = new Operation(b, offset);
    	offset +=op.getByteSize();
    	timestamp = new TimeStamp(b, offset);
    	offset += timestamp.getByteSize();
    	logicalClock = new LogicalClock(b, offset);
    	offset += logicalClock.getByteSize();
    	wset = new WriteSet(b, offset);
    	offset += wset.getByteSize();
    	opName = new String(b, offset, b.length - offset);
    	offset += b.length - offset;
    	if (offset != getBytes().length)
    	    throw new RuntimeException("did not consume entire byte array");
    }
    
    public void reset(){
        txnId = null;
        op = null;
        timestamp = null;
        logicalClock = null;
        wset = null;
        opName = null;
    }

    public Operation getOperation(){
    	return op;
    }

    public ProxyTxnId getTxnId(){
	return txnId;
    }
    
    public TimeStamp getTimeStamp(){
    	return timestamp;
    }

    public LogicalClock getLogicalClock(){
    	return logicalClock;
    }
    
    public Operation getShadowOperation(){
    	return op;
    }
    
    public WriteSet getWset(){
    	return wset;
    }
    
    public String getOpName(){
    	return this.opName;
    }

    static int computeByteSize(ProxyTxnId tx, Operation op0, TimeStamp ts, LogicalClock lc, WriteSet ws,
    		String opName){
	return tx.getByteSize() + op0.getByteSize() + ts.getByteSize() + lc.getByteSize() + ws.getByteSize() + opName.getBytes().length;
    }


    public String toString(){
	return "<"+getTagString() + ", " + txnId + ", " + op + "," + timestamp + "," + logicalClock + "," + wset + "," + opName + ">";
    }

	@Override
	public String getTagString() {
		return MessageTags.getString(this.getTag());
	}

}
