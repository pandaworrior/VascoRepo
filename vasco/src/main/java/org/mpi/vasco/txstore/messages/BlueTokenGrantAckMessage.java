package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.ProxyTxnId;

public class BlueTokenGrantAckMessage extends MessageBase{
	
	protected ProxyTxnId txnId;

	public BlueTokenGrantAckMessage(ProxyTxnId txid) {
		super(MessageTags.BLUETOKENGRANTACK, computeByteSize(txid));
		txnId = txid;
		int offset = getOffset();
		txnId.getBytes(getBytes(), offset);
		offset += txnId.getByteSize();
		if (offset != getBytes().length)
		    throw new RuntimeException("did not fill up the byte array!");
	    }

	 public BlueTokenGrantAckMessage(byte[] b){
		super(b);
		if (getTag() != MessageTags.BLUETOKENGRANTACK)
		    throw new RuntimeException("Invalid message tag.  looking for "+
					       MessageTags.BLUETOKENGRANTACK+ " found "+getTag());
		int offset = getOffset();
		txnId = new ProxyTxnId(b, offset);
		offset += txnId.getByteSize();
		if (offset != b.length)
		    throw new RuntimeException("did not consume the entire byte array!");
	    }

	    public ProxyTxnId getTxnId(){
		return txnId;
	    }


	    static int computeByteSize(ProxyTxnId txnId){
		return txnId.getByteSize();
	    }

	    public String toString(){
		return "<"+getTagString()+", "+txnId+">";
	    }

		@Override
		public void reset() {
			// TODO Auto-generated method stub
			
		}
}
