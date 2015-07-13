package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.ReadWriteSet;
import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.util.UnsignedTypes;

public class CommitShadowOpMessage extends MessageBase{
    ProxyTxnId txnId;
    Operation op;
    protected TimeStamp timestamp;
    protected LogicalClock logicalClock;

    public CommitShadowOpMessage(ProxyTxnId txnId,  Operation op, TimeStamp ts, LogicalClock lc){
	super(MessageTags.COMMITSHADOW, computeByteSize(txnId, op, ts, lc));
	this.txnId = txnId;
	this.op = op;
	this.timestamp = ts;
	this.logicalClock = lc;

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
	
	if (bytes.length != offset)
	    throw new RuntimeException("failed to consume entire byte array");
	
    }
    
    public void encodeMessage(ProxyTxnId _txnId, Operation _op,TimeStamp _timestamp,
    	    LogicalClock _logicalClock){
    	txnId = _txnId;
    	op = _op;
    	timestamp = _timestamp;
    	logicalClock = _logicalClock;
    	this.config(MessageTags.COMMITSHADOW, computeByteSize(txnId, op, timestamp, logicalClock));
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
    	
    	if (bytes.length != offset)
    	    throw new RuntimeException("failed to consume entire byte array");
    }

    public CommitShadowOpMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.COMMITSHADOW)
	    throw new RuntimeException("invalid message tag.  Found: "+getTag()+" expected: "+
				       MessageTags.COMMITSHADOW);
	
	txnId = new ProxyTxnId(b, getOffset());
	int offset = getOffset() + txnId.getByteSize();
	op = new Operation(b, offset);
	offset +=op.getByteSize();
	timestamp = new TimeStamp(b, offset);
	offset += timestamp.getByteSize();
	logicalClock = new LogicalClock(b, offset);
	offset += logicalClock.getByteSize();
	if (offset != getBytes().length)
	    throw new RuntimeException("did not consume entire byte array");
    }
    
    public void decodeMessage(byte[] b){
    	this.decodeMessage(b, 0);
    	if (getTag() != MessageTags.COMMITSHADOW)
    	    throw new RuntimeException("invalid message tag.  Found: "+getTag()+" expected: "+
    				       MessageTags.COMMITSHADOW);
    	
    	txnId = new ProxyTxnId(b, getOffset());
    	int offset = getOffset() + txnId.getByteSize();
    	op = new Operation(b, offset);
    	offset +=op.getByteSize();
    	timestamp = new TimeStamp(b, offset);
    	offset += timestamp.getByteSize();
    	logicalClock = new LogicalClock(b, offset);
    	offset += logicalClock.getByteSize();
    	if (offset != getBytes().length)
    	    throw new RuntimeException("did not consume entire byte array");
    }
    
    public void reset(){
        txnId = null;
        op = null;
        timestamp = null;
        logicalClock = null;
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

    static int computeByteSize(ProxyTxnId tx, Operation op0, TimeStamp ts, LogicalClock lc){
	return tx.getByteSize() + op0.getByteSize() + ts.getByteSize() + lc.getByteSize();
    }


    public String toString(){
	return "<"+getTagString()+", "+txnId+", "  + op + "," + timestamp + "," + logicalClock+">";
    }

	@Override
	public String getTagString() {
		return MessageTags.getString(this.getTag());
	}

}
