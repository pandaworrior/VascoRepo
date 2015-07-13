package org.mpi.vasco.txstore.messages;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.OperationLog;
import org.mpi.vasco.util.UnsignedTypes;

/**
   message exchanged between storage at different data centers when exchanging transactions executed at one data center.
 **/
public class OperationEncodingMessage extends MessageBase{

    int dcId;
    ProxyTxnId txnId;
    OperationLog opLog;

    public OperationEncodingMessage(int dc, ProxyTxnId txnId, OperationLog oplog){
	super(MessageTags.OPERATIONENCODING, computeByteSize(txnId, oplog));
	dcId = dc;
	this.txnId = txnId;
	opLog = oplog;

	int offset = getOffset();

	byte bytes[] = getBytes();

	UnsignedTypes.intToBytes(dcId, bytes, offset);
	offset += UnsignedTypes.uint16Size;
	txnId.getBytes(bytes, offset);
	offset+= txnId.getByteSize();
	opLog.getBytes(bytes, offset);
	offset+= opLog.getByteSize();
	if (bytes.length != offset)
	    throw new RuntimeException("failed to consume entire byte array");
	
    }

    public OperationEncodingMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.OPERATIONENCODING)
	    throw new RuntimeException("invalid message tag.  Found: "+getTag()+" expected: "+
				       MessageTags.OPERATIONENCODING);
	
	dcId = UnsignedTypes.bytesToInt(b, getOffset());
	int offset = getOffset() + UnsignedTypes.uint16Size;
	txnId = new ProxyTxnId(b, offset);
	offset += txnId.getByteSize();
	opLog = new OperationLog(b, offset);
	offset +=opLog.getByteSize();
	if (offset != getBytes().length)
	    throw new RuntimeException("did not consume entire byte array");
    }

    public OperationLog getOperationLog(){
	return opLog;
    }

    public ProxyTxnId getTxnId(){
	return txnId;
    }

    public int getDatacenter(){
	return dcId;
    }

    static int computeByteSize(ProxyTxnId tx, OperationLog opLog){
	return UnsignedTypes.uint16Size + tx.getByteSize() + opLog.getByteSize();
    }


    public String toString(){
	return "<"+getTagString()+", "+txnId+", "+opLog+">";
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