package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.TimeStamp;

/**
   Sent from the coordinator to the proxy when a transaction starts.
   Contains the proxy specified transaction ID and the starting
   timestamp of the transaction.
 **/
public class AckTxnMessage extends MessageBase{

    protected ProxyTxnId proxyTxnId;
    protected TimeStamp timeStamp;
    
    public AckTxnMessage(ProxyTxnId txid, TimeStamp timeStamp){
	super(MessageTags.ACKTXN, computeByteSize(txid, timeStamp));
	proxyTxnId = txid;
	this.timeStamp = timeStamp;
	
	int offset = getOffset();
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	timeStamp.getBytes(getBytes(), offset);
	offset +=timeStamp.getByteSize();
	if (offset != getBytes().length)
	    throw new RuntimeException("did not consume the entire byte array!");

    }
    
    public void setTxnId(ProxyTxnId id){
    	proxyTxnId = id;
    }
    
    public void setTs(TimeStamp ts){
    	timeStamp = ts;
    }
    
    public void encodeMessage(ProxyTxnId id, TimeStamp ts){
    	setTxnId(id);
    	setTs(ts);
    	this.config(MessageTags.ACKTXN, computeByteSize(proxyTxnId, timeStamp));
    	int offset = getOffset();
    	proxyTxnId.getBytes(getBytes(), offset);
    	offset += proxyTxnId.getByteSize();
    	timeStamp.getBytes(getBytes(), offset);
    	offset +=timeStamp.getByteSize();
    	if (offset != getBytes().length)
    	    throw new RuntimeException("did not consume the entire byte array!");
    }

    public AckTxnMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.ACKTXN)
	    throw new RuntimeException("Invalid message tag.  looking for "+
				       MessageTags.ACKTXN+ " found "+getTag());
	int offset = getOffset();
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	timeStamp = new TimeStamp(b, offset);
	offset += timeStamp.getByteSize();
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array!");
    }
    
    public void decodeMessage(byte[] b){
    	this.decodeMessage(b, 0);
    	if (getTag() != MessageTags.ACKTXN)
    	    throw new RuntimeException("Invalid message tag.  looking for "+
    				       MessageTags.ACKTXN+ " found "+getTag());
    	int offset = getOffset();
    	proxyTxnId = new ProxyTxnId(b, offset);
    	offset += proxyTxnId.getByteSize();
    	timeStamp = new TimeStamp(b, offset);
    	offset += timeStamp.getByteSize();
    	if (offset != b.length)
    	    throw new RuntimeException("did not consume the entire byte array!");
    }
    
    public void reset(){
        proxyTxnId = null;
        timeStamp = null;
    }

    public ProxyTxnId getTxnId(){
	return proxyTxnId;
    }

    public TimeStamp getTimeStamp(){
	return timeStamp;
    }


    static int computeByteSize(ProxyTxnId proxyTxnId, TimeStamp timeStamp){
	return proxyTxnId.getByteSize()+timeStamp.getByteSize();
    }

    public String toString(){
	return "<"+getTagString()+", "+proxyTxnId+", "+timeStamp+">";
    }


}