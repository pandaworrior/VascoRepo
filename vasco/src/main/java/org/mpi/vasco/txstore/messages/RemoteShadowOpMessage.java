package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.ReadWriteSet;
import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.txstore.util.WriteSet;
import org.mpi.vasco.util.UnsignedTypes;

public class RemoteShadowOpMessage extends MessageBase{
    ProxyTxnId txnId;
    Operation op;
    protected TimeStamp timestamp;
    protected LogicalClock logicalClock;
    int color;
    WriteSet wset;

    public RemoteShadowOpMessage(ProxyTxnId txnId,  Operation op, TimeStamp ts, LogicalClock lc, int color,  WriteSet wset){
	super(MessageTags.REMOTESHADOW, computeByteSize(txnId, op, ts, lc, color, wset));
	this.txnId = txnId;
	this.op = op;
	this.timestamp = ts;
	this.logicalClock = lc;
	this.color = color;
	this.wset = wset;

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
	UnsignedTypes.intToBytes(color, getBytes(), offset);
	offset += UnsignedTypes.uint16Size;
	wset.getBytes(getBytes(), offset);
	offset += wset.getByteSize();
	if (bytes.length != offset)
	    throw new RuntimeException("failed to consume entire byte array");
	
    }
    
    public void encodeMessage(    ProxyTxnId id,  Operation o,TimeStamp ts,
    LogicalClock lc, int c,  WriteSet wS){
    	txnId = id;
    	op = o;
    	timestamp = ts;
    	logicalClock = lc;
    	color = c;
    	wset = wS;
    	this.config(MessageTags.REMOTESHADOW, computeByteSize(txnId, op, timestamp, logicalClock, color, wset));
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
    	UnsignedTypes.intToBytes(color, getBytes(), offset);
    	offset += UnsignedTypes.uint16Size;
    	wset.getBytes(getBytes(), offset);
    	offset += wset.getByteSize();
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
	color = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	wset = new WriteSet(b, offset);
	offset += wset.getByteSize();
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
    	color = UnsignedTypes.bytesToInt(b, offset);
    	offset += UnsignedTypes.uint16Size;
    	wset = new WriteSet(b, offset);
    	offset += wset.getByteSize();
    	if (offset != getBytes().length)
    	    throw new RuntimeException("did not consume entire byte array");
    }
    
    public void reset(){
        txnId = null;
        op = null;
        timestamp = null;
        logicalClock = null;
        color = 0;
        wset = null;
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
        
    public boolean isBlue(){
    	if(color == 1){
    		return true;
    	}
    	return false;
    }
    
    public Operation getShadowOperation(){
    	return op;
    }
    public WriteSet getWset(){
    	return wset;
    }
    
    public int getColor(){
    	return color;
    }

    static int computeByteSize(ProxyTxnId tx, Operation op0, TimeStamp ts, LogicalClock lc, int color, WriteSet ws){
	return tx.getByteSize() + op0.getByteSize() + ts.getByteSize() + lc.getByteSize() + UnsignedTypes.uint16Size + ws.getByteSize();
    }


    public String toString(){
	return "<"+getTagString()+", "+txnId+", "  + op + "," + timestamp + "," + logicalClock+ "," +color + ","+wset+">";
    }

}
