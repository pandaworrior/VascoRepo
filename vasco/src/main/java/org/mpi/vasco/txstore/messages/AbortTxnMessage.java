package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.ProxyTxnId;

/**
   Sent from the coordinator to the proxy and the storage when a transaction must be aborted.
 **/

public class AbortTxnMessage extends MessageBase{

    protected ProxyTxnId proxyTxnId;

    public AbortTxnMessage(ProxyTxnId txid){
	super(MessageTags.ABORTTXN, computeByteSize(txid));
	proxyTxnId = txid;
	
	int offset = getOffset();
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	if (offset != getBytes().length)
	    throw new RuntimeException("did not fill up the byte array!");
    }

    public AbortTxnMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.ABORTTXN)
	    throw new RuntimeException("Invalid message tag.  looking for "+
				       MessageTags.ABORTTXN+ " found "+getTag());
	int offset = getOffset();
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array!");
    }

    public ProxyTxnId getTxnId(){
	return proxyTxnId;
    }


    static int computeByteSize(ProxyTxnId proxyTxnId){
	return proxyTxnId.getByteSize();
    }

    public String toString(){
	return "<"+getTagString()+", "+proxyTxnId+">";
    }

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}


}