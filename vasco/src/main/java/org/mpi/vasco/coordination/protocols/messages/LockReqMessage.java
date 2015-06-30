/*******************************************************************************
 * Copyright (c) 2015 Dependable Cloud Group and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dependable Cloud Group - initial API and implementation
 *
 * Creator:
 *     Cheng Li
 *
 * Contact:
 *     chengli@mpi-sws.org    
 *******************************************************************************/
package org.mpi.vasco.coordination.protocols.messages;


import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.ProxyTxnId;

// TODO: Auto-generated Javadoc
/**
 * The Class LockReqMessage.
 */
public class LockReqMessage extends MessageBase {
	
	/** The proxy txn id. */
	protected ProxyTxnId proxyTxnId;
	
	/** The lock req. */
	protected LockRequest lockReq;
    
    /**
     * Instantiates a new lock req message.
     *
     * @param _proxyTxnId the _proxy txn id
     * @param _lockReq the _lock req
     */
    public LockReqMessage(ProxyTxnId _proxyTxnId, LockRequest _lockReq){
	super(MessageTags.LOCKREQ, computeByteSize(_proxyTxnId, _lockReq));
	this.setProxyTxnId(_proxyTxnId);
	this.setLockReq(_lockReq);
	
	int offset = getOffset();
	proxyTxnId.getBytes(getBytes(), offset);
	offset += proxyTxnId.getByteSize();
	
	byte[] _lockReqBytes = _lockReq.getBytes();
	int lenOfLockReqBytes = _lockReqBytes.length;
	System.arraycopy(_lockReqBytes, 0, getBytes(), offset, lenOfLockReqBytes);
	offset += lenOfLockReqBytes;
	if (offset != getBytes().length)
	    throw new RuntimeException("did not consume the entire byte array!");

    }
    
    /**
     * Encode message.
     *
     * @param _proxyTxnId the _proxy txn id
     * @param _lockReq the _lock req
     */
    public void encodeMessage(ProxyTxnId _proxyTxnId, LockRequest _lockReq){
    	this.setProxyTxnId(_proxyTxnId);
    	this.setLockReq(_lockReq);
    	
    	this.config(MessageTags.LOCKREQ, computeByteSize(_proxyTxnId, _lockReq));
    	int offset = getOffset();
    	proxyTxnId.getBytes(getBytes(), offset);
    	offset += proxyTxnId.getByteSize();
    	
    	byte[] _lockReqBytes = _lockReq.getBytes();
    	int lenOfLockReqBytes = _lockReqBytes.length;
    	System.arraycopy(_lockReqBytes, 0, getBytes(), offset, lenOfLockReqBytes);
    	offset += lenOfLockReqBytes;
    	if (offset != getBytes().length)
    	    throw new RuntimeException("did not consume the entire byte array!");
    }

    /**
     * Instantiates a new lock req message.
     *
     * @param b the b
     */
    public LockReqMessage(byte[] b){
	super(b);
	if (getTag() != MessageTags.LOCKREQ)
	    throw new RuntimeException("Invalid message tag.  looking for "+
				       MessageTags.LOCKREQ+ " found "+getTag());
	int offset = getOffset();
	proxyTxnId = new ProxyTxnId(b, offset);
	offset += proxyTxnId.getByteSize();
	
	this.setLockReq(new LockRequest(b, offset));
	offset += this.getLockReq().getByteSize();
	if (offset != b.length)
	    throw new RuntimeException("did not consume the entire byte array!");
    }
    
    /**
     * Decode message.
     *
     * @param b the b
     */
    public void decodeMessage(byte[] b){
    	this.decodeMessage(b, 0);
    	if (getTag() != MessageTags.LOCKREQ)
    	    throw new RuntimeException("Invalid message tag.  looking for "+
    				       MessageTags.LOCKREQ+ " found "+getTag());
    	int offset = getOffset();
    	proxyTxnId = new ProxyTxnId(b, offset);
    	offset += proxyTxnId.getByteSize();
    	
    	this.setLockReq(new LockRequest(b, offset));
    	offset += this.getLockReq().getByteSize();
    	if (offset != b.length)
    	    throw new RuntimeException("did not consume the entire byte array!");
    }
    
    /* (non-Javadoc)
     * @see org.mpi.vasco.network.messages.MessageBase#reset()
     */
    public void reset(){
    	this.setProxyTxnId(null);
    	this.setLockReq(null);
    }

    /**
     * Compute byte size.
     *
     * @param _proxyTxnId the _proxy txn id
     * @param _lockReq the _lock req
     * @return the int
     */
    static int computeByteSize(ProxyTxnId _proxyTxnId, LockRequest _lockReq){
    	return _proxyTxnId.getByteSize() + _lockReq.getByteSize();
    }

    /* (non-Javadoc)
     * @see org.mpi.vasco.network.messages.MessageBase#toString()
     */
    public String toString(){
    	String _str = "<"+getTagString()+", "+this.getProxyTxnId().toString()+" LockRequest "+this.getLockReq().toString();
    	return _str;
    }

	/**
	 * Gets the proxy txn id.
	 *
	 * @return the proxy txn id
	 */
	public ProxyTxnId getProxyTxnId() {
		return proxyTxnId;
	}

	/**
	 * Sets the proxy txn id.
	 *
	 * @param proxyTxnId the new proxy txn id
	 */
	public void setProxyTxnId(ProxyTxnId proxyTxnId) {
		this.proxyTxnId = proxyTxnId;
	}

	/**
	 * Gets the lock req.
	 *
	 * @return the lock req
	 */
	public LockRequest getLockReq() {
		return lockReq;
	}

	/**
	 * Sets the lock req.
	 *
	 * @param lockReq the new lock req
	 */
	public void setLockReq(LockRequest lockReq) {
		this.lockReq = lockReq;
	}

}
