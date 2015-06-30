package org.mpi.vasco.coordination.protocols.messages;

import java.util.ArrayList;
import java.util.List;

import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.util.UnsignedTypes;

public class LockReqMessage extends MessageBase {
	protected ProxyTxnId proxyTxnId;
	protected String opName;
    protected List<String> keyList;
    
    public LockReqMessage(ProxyTxnId _proxyTxnId, String _opName, List<String> _keyList){
	super(MessageTags.LOCKREQ, computeByteSize(_proxyTxnId, _opName, _keyList));
	this.setProxyTxnId(_proxyTxnId);
	this.setOpName(_opName);
	this.setKeyList(_keyList);
	
	int offset = getOffset();
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	
	byte[] opNameBytes = _opName.getBytes();
	int opNameByteLen = opNameBytes.length;
	UnsignedTypes.intToBytes(opNameByteLen, getBytes(), offset);
	offset += UnsignedTypes.uint16Size;
	System.arraycopy(opNameBytes, 0, getBytes(), offset, opNameByteLen);
	offset += opNameByteLen;
	
	for(String key : _keyList){
		byte[] keyBytes = key.getBytes();
		int keyByteLen = keyBytes.length;
		UnsignedTypes.intToBytes(keyByteLen, getBytes(), offset);
		offset += UnsignedTypes.uint16Size;
		System.arraycopy(keyBytes, 0, getBytes(), offset, keyByteLen);
		offset += keyByteLen;
	}
	if (offset != getBytes().length)
	    throw new RuntimeException("did not consume the entire byte array!");

    }
    
    public void encodeMessage(	ProxyTxnId _proxyTxnId, String _opName, List<String> _keyList){
    	this.setProxyTxnId(_proxyTxnId);
    	this.setOpName(_opName);
    	this.setKeyList(_keyList);
    	this.config(MessageTags.LOCKREQ, computeByteSize(_proxyTxnId, _opName, _keyList));
    	int offset = getOffset();
    	proxyTxnId.getBytes(getBytes(), offset);
    	offset += proxyTxnId.getByteSize();
    	
    	byte[] opNameBytes = _opName.getBytes();
    	int opNameByteLen = opNameBytes.length;
    	UnsignedTypes.intToBytes(opNameByteLen, getBytes(), offset);
    	offset += UnsignedTypes.uint16Size;
    	System.arraycopy(opNameBytes, 0, getBytes(), offset, opNameByteLen);
    	offset += opNameByteLen;
    	
    	int numOfKey = _keyList.size();
    	UnsignedTypes.intToBytes(numOfKey, getBytes(), offset);
    	offset += UnsignedTypes.uint16Size;
    	
    	for(String key : _keyList){
    		byte[] keyBytes = key.getBytes();
    		int keyByteLen = keyBytes.length;
    		UnsignedTypes.intToBytes(keyByteLen, getBytes(), offset);
    		offset += UnsignedTypes.uint16Size;
    		System.arraycopy(keyBytes, 0, getBytes(), offset, keyByteLen);
    		offset += keyByteLen;
    	}
    	if (offset != getBytes().length)
    	    throw new RuntimeException("did not consume the entire byte array!");
    }

    public LockReqMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.LOCKREQ)
	    throw new RuntimeException("Invalid message tag.  looking for "+
				       MessageTags.LOCKREQ+ " found "+getTag());
	int offset = getOffset();
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	
	int opNameByteLen = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	
	this.opName = new String(b, offset, opNameByteLen);
	offset += opNameByteLen;
	
	int numOfKey = UnsignedTypes.bytesToInt(b, offset);
	offset += UnsignedTypes.uint16Size;
	
	this.setKeyList(new ArrayList<String>());
	for(int i = 0; i < numOfKey; i++){
		int keyByteLen = UnsignedTypes.bytesToInt(b, offset);
		offset += UnsignedTypes.uint16Size;
		String key = new String(b, offset, keyByteLen);
		offset += keyByteLen;
		this.getKeyList().add(key);
	}
	
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array!");
    }
    
    public void decodeMessage(byte[] b){
    	this.decodeMessage(b, 0);
    	if (getTag() != MessageTags.LOCKREQ)
    	    throw new RuntimeException("Invalid message tag.  looking for "+
    				       MessageTags.LOCKREQ+ " found "+getTag());
    	int offset = getOffset();
    	proxyTxnId = new ProxyTxnId(b, offset);
    	offset += proxyTxnId.getByteSize();
    	
    	int opNameByteLen = UnsignedTypes.bytesToInt(b, offset);
    	offset += UnsignedTypes.uint16Size;
    	
    	this.opName = new String(b, offset, opNameByteLen);
    	offset += opNameByteLen;
    	
    	int numOfKey = UnsignedTypes.bytesToInt(b, offset);
    	offset += UnsignedTypes.uint16Size;
    	
    	this.setKeyList(new ArrayList<String>());
    	for(int i = 0; i < numOfKey; i++){
    		int keyByteLen = UnsignedTypes.bytesToInt(b, offset);
    		offset += UnsignedTypes.uint16Size;
    		String key = new String(b, offset, keyByteLen);
    		offset += keyByteLen;
    		this.getKeyList().add(key);
    	}
    	if (offset != b.length)
    	    throw new RuntimeException("did not consume the entire byte array!");
    }
    
    public void reset(){
    	this.setProxyTxnId(null);
    	this.setOpName(null);
    	this.setKeyList(null);
    }

    static int computeByteSize(ProxyTxnId _proxyTxnId, String _opName, List<String> _keyList){
	int messageSize = _proxyTxnId.getByteSize()+ _opName.getBytes().length;
	for(String k : _keyList){
		messageSize += k.getBytes().length;
	}
	return messageSize;
    }

    public String toString(){
    	String _str = "<"+getTagString()+", "+this.getProxyTxnId().toString()+", Operation name: "+this.getOpName()+">";
    	for(String key: this.getKeyList()){
    		_str+= " key :" + key;
    	}
    	return _str;
    }

	public ProxyTxnId getProxyTxnId() {
		return proxyTxnId;
	}

	public void setProxyTxnId(ProxyTxnId proxyTxnId) {
		this.proxyTxnId = proxyTxnId;
	}

	public String getOpName() {
		return opName;
	}

	public void setOpName(String opName) {
		this.opName = opName;
	}

	public List<String> getKeyList() {
		return keyList;
	}

	public void setKeyList(List<String> keyList) {
		this.keyList = keyList;
	}

}
