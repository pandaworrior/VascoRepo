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

import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.network.messages.MessageBase;
import org.mpi.vasco.txstore.util.ProxyTxnId;

/**
 * The Class LockRepMessage. SENT between Lock Server and Client for granting
 * a permission.
 */
public class LockRepMessage extends MessageBase{
	
	/** The proxy txn id. */
	protected ProxyTxnId proxyTxnId;
    
    /** The lock rly. */
    protected LockReply lockRly;

	/**
	 * Instantiates a new lock rep message.
	 *
	 * @param _proxyTxnId the _proxy txn id
	 * @param _lockRly the _lock rly
	 */
	public LockRepMessage(ProxyTxnId _proxyTxnId, LockReply _lockRly){
		super(MessageTags.LOCKREP, computeByteSize(_proxyTxnId, _lockRly));
		this.setProxyTxnId(_proxyTxnId);
		this.setLockRly(_lockRly);
		
		int offset = getOffset();
		proxyTxnId.getBytes(getBytes(), offset);
		offset += proxyTxnId.getByteSize();
		
		byte[] lockRlyBytes = _lockRly.getBytes();
		int lenOfLockRlyBytes = lockRlyBytes.length;
		System.arraycopy(lockRlyBytes, 0, getBytes(), offset, lenOfLockRlyBytes);
		offset += lenOfLockRlyBytes;
		if (offset != getBytes().length)
		    throw new RuntimeException("did not consume the entire byte array!");

	    }
	    
	    /**
    	 * Encode message.
    	 *
    	 * @param _proxyTxnId the _proxy txn id
    	 * @param _lockRly the _lock rly
    	 */
    	public void encodeMessage(	ProxyTxnId _proxyTxnId, LockReply _lockRly){
	    	this.setProxyTxnId(_proxyTxnId);
	    	this.setLockRly(_lockRly);
	    	
	    	this.config(MessageTags.LOCKREP, computeByteSize(_proxyTxnId, _lockRly));
	    	int offset = getOffset();
	    	proxyTxnId.getBytes(getBytes(), offset);
	    	offset += proxyTxnId.getByteSize();
	    	
	    	byte[] lockRlyBytes = _lockRly.getBytes();
			int lenOfLockRlyBytes = lockRlyBytes.length;
			System.arraycopy(lockRlyBytes, 0, getBytes(), offset, lenOfLockRlyBytes);
			offset += lenOfLockRlyBytes;
	    	if (offset != getBytes().length)
	    	    throw new RuntimeException("did not consume the entire byte array!");
	    }

	    /**
    	 * Instantiates a new lock rep message.
    	 *
    	 * @param b the b
    	 */
    	public LockRepMessage(byte[] b){
			super(b);
			if (getTag() != MessageTags.LOCKREP)
			    throw new RuntimeException("Invalid message tag.  looking for "+
						       MessageTags.LOCKREP+ " found "+getTag());
			int offset = getOffset();
			proxyTxnId = new ProxyTxnId(b, offset);
			offset += proxyTxnId.getByteSize();
			
			this.setLockRly(new LockReply(b, offset));
			offset += this.getLockRly().getByteSize();
			if (offset != getBytes().length)
	    	    throw new RuntimeException("did not consume the entire byte array!");
	    }
	    
	    /**
    	 * Decode message.
    	 *
    	 * @param b the b
    	 */
    	public void decodeMessage(byte[] b){
	    	this.decodeMessage(b, 0);
	    	if (getTag() != MessageTags.LOCKREP)
	    	    throw new RuntimeException("Invalid message tag.  looking for "+
	    				       MessageTags.LOCKREP+ " found "+getTag());
	    	int offset = getOffset();
	    	proxyTxnId = new ProxyTxnId(b, offset);
	    	offset += proxyTxnId.getByteSize();
	    	
	    	this.setLockRly(new LockReply(b, offset));
			offset += this.getLockRly().getByteSize();
	    	if (offset != b.length)
	    	    throw new RuntimeException("did not consume the entire byte array!");
	    }
	    
	    /* (non-Javadoc)
    	 * @see org.mpi.vasco.network.messages.MessageBase#reset()
    	 */
    	public void reset(){
	    	this.setProxyTxnId(null);
	    	this.setLockRly(null);
	    }

	    /**
    	 * Compute byte size.
    	 *
    	 * @param _proxyTxnId the _proxy txn id
    	 * @param _lockRly the _lock rly
    	 * @return the int
    	 */
    	static int computeByteSize(ProxyTxnId _proxyTxnId, LockReply _lockRly){
		return _proxyTxnId.getByteSize() + _lockRly.getByteSize();
	    }

	    /* (non-Javadoc)
    	 * @see org.mpi.vasco.network.messages.MessageBase#toString()
    	 */
    	public String toString(){
	    	String _str = "<"+getTagString()+", "+this.getProxyTxnId().toString()+", " + this.getLockRly().toString() +">";
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
		 * Gets the lock rly.
		 *
		 * @return the lock rly
		 */
		public LockReply getLockRly() {
			return lockRly;
		}

		/**
		 * Sets the lock rly.
		 *
		 * @param lockRly the new lock rly
		 */
		public void setLockRly(LockReply lockRly) {
			this.lockRly = lockRly;
		}

		@Override
		public String getTagString() {
			return MessageTags.getString(this.getTag());
		}

}
