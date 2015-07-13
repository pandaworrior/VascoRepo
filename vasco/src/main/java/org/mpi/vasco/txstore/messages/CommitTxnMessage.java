package org.mpi.vasco.txstore.messages;
import org.mpi.vasco.util.UnsignedTypes;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.txstore.util.LogicalClock;

/**
   Sent from the Coordinator to the proxy and storage shims if the transaction should commit.
 **/

public class CommitTxnMessage extends MessageBase{

    protected ProxyTxnId proxyTxnId;
    protected TimeStamp timestamp;
    protected LogicalClock logicalClock;
    protected int color;
    protected int readonly;

    public CommitTxnMessage(ProxyTxnId txid, TimeStamp ts,
			    LogicalClock lclock, int c, int r){
	super(MessageTags.COMMITTXN, computeByteSize(txid, ts, lclock, c, r));
	proxyTxnId = txid;
	this.timestamp = ts;
	logicalClock = lclock;
	color = c;
	readonly = r;
	
	int offset = getOffset();
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	timestamp.getBytes(getBytes(), offset);
	offset += timestamp.getByteSize();
	logicalClock.getBytes(getBytes(), offset);
	offset += logicalClock.getByteSize();
	UnsignedTypes.intToBytes(color, getBytes(), offset);
	offset += UnsignedTypes.uint16Size;
	UnsignedTypes.intToBytes(readonly, getBytes(), offset);
	offset += UnsignedTypes.uint16Size;
	if (offset != getBytes().length)
	    throw new RuntimeException("did not fill up the byte array!");
    }

    public CommitTxnMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.COMMITTXN)
	    throw new RuntimeException("Invalid message tag.  looking for "+
				       MessageTags.COMMITTXN+ " found "+getTag());
	int offset = getOffset();
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	timestamp = new TimeStamp(b, offset);
	offset += timestamp.getByteSize();
	logicalClock = new LogicalClock(b, offset);
	offset += logicalClock.getByteSize();
	color = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	readonly = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array!");
    }

    public ProxyTxnId getTxnId(){
	return proxyTxnId;
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
    
    public boolean isReadonly(){
    	if(readonly == 0)
    		return true;
    	return false;
    }

    static int computeByteSize(ProxyTxnId proxyTxnId, TimeStamp ts,
			       LogicalClock lclock, int c, int r){
	return proxyTxnId.getByteSize() + ts.getByteSize() + lclock.getByteSize() + UnsignedTypes.uint16Size +UnsignedTypes.uint16Size;
    }

    public String toString(){
	return "<"+getTagString()+", "+proxyTxnId+", "+timestamp+", "+logicalClock+"," + color + "," + readonly+ ">";
    }

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTagString() {
		return MessageTags.getString(this.getTag());
	}


}