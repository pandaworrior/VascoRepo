package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.ProxyTxnId;

/**
   Sent from the proxy to the coordinator when it is time to start a transaction.
 **/

public class BeginTxnMessage extends MessageBase{

    protected ProxyTxnId txnId;

    public BeginTxnMessage(ProxyTxnId txid){
	super(MessageTags.BEGINTXN, computeByteSize(txid));
	txnId = txid;
	
	int offset = getOffset();
	txnId.getBytes(getBytes(), offset);
	offset += txnId.getByteSize();
	if (offset != getBytes().length)
	    throw new RuntimeException("did not fill up the byte array!");
    }
    
    public void encode(ProxyTxnId txid){
    	txnId = txid;
    	this.config(MessageTags.BEGINTXN, computeByteSize(txnId));
    	int offset = getOffset();
    	txnId.getBytes(getBytes(), offset);
    	offset += txnId.getByteSize();
    	if (offset != getBytes().length)
    	    throw new RuntimeException("did not fill up the byte array!");
    }

    public BeginTxnMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.BEGINTXN)
	    throw new RuntimeException("Invalid message tag.  looking for "+
				       MessageTags.BEGINTXN+ " found "+getTag());
	int offset = getOffset();
	txnId = new ProxyTxnId(b, offset);
	offset += txnId.getByteSize();
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array!");
    }
    
    public void decodeMessage(byte[] b){
    	this.decodeMessage(b, 0);
    	if (getTag() != MessageTags.BEGINTXN)
    	    throw new RuntimeException("Invalid message tag.  looking for "+
    				       MessageTags.BEGINTXN+ " found "+getTag());
    	int offset = getOffset();
    	txnId = new ProxyTxnId(b, offset);
    	offset += txnId.getByteSize();
    	if (offset != b.length)
    	    throw new RuntimeException("did not consume the entire byte array!");
    }

    public ProxyTxnId getTxnId(){
	return txnId;
    }
    
    public void reset(){
    	txnId = null;
    }


    static int computeByteSize(ProxyTxnId txnId){
	return txnId.getByteSize();
    }

    public String toString(){
	return "<"+getTagString()+", "+txnId+">";
    }


}