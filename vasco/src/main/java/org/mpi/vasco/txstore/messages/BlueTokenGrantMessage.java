package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.util.UnsignedTypes;

public class BlueTokenGrantMessage extends MessageBase{
	
	protected ProxyTxnId txnId;
	protected long blueEpoch;

	public BlueTokenGrantMessage(ProxyTxnId txid, long blueE) {
		super(MessageTags.BLUETOKENGRANT, computeByteSize(txid, blueE));
		txnId = txid;
		blueEpoch = blueE;
		int offset = getOffset();
		txnId.getBytes(getBytes(), offset);
		offset += txnId.getByteSize();
		UnsignedTypes.longToBytes(blueEpoch, getBytes(), offset);
		offset += UnsignedTypes.uint64Size;
		if (offset != getBytes().length)
		    throw new RuntimeException("did not fill up the byte array!");
	    }

	 public BlueTokenGrantMessage(byte[] b){
		super(b);
		if (getTag() != MessageTags.BLUETOKENGRANT)
		    throw new RuntimeException("Invalid message tag.  looking for "+
					       MessageTags.BLUETOKENGRANT+ " found "+getTag());
		int offset = getOffset();
		txnId = new ProxyTxnId(b, offset);
		offset += txnId.getByteSize();
		blueEpoch = UnsignedTypes.bytesToLong(b, offset);
		offset += UnsignedTypes.uint64Size;
		if (offset != b.length)
		    throw new RuntimeException("did not consume the entire byte array!");
	    }

	    public ProxyTxnId getTxnId(){
		return txnId;
	    }
	    
	    public long getBlueEpoch(){
	    	return blueEpoch;
	    }


	    static int computeByteSize(ProxyTxnId txnId, long blueEpoch){
		return txnId.getByteSize() + UnsignedTypes.uint64Size;
	    }

	    public String toString(){
		return "<"+getTagString()+", "+txnId+">";
	    }

		@Override
		public void reset() {
			// TODO Auto-generated method stub
			
		}
}
