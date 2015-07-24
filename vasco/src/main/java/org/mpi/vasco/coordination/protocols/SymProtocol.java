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
package org.mpi.vasco.coordination.protocols;

import java.util.concurrent.ConcurrentHashMap;

import org.mpi.vasco.coordination.MessageHandlerClientSide;
import org.mpi.vasco.coordination.protocols.messages.LockReqMessage;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.coordination.protocols.util.SymMetaData;
import org.mpi.vasco.txstore.util.ProxyTxnId;

/**
 * The Class SymProtocol.
 */
public class SymProtocol extends Protocol{
	
	private ConcurrentHashMap<ProxyTxnId, SymMetaData> symMetaDataMap;
	
	/**
	 * Instantiates a new sym protocol.
	 *
	 * @param xmlFile the xml file
	 * @param clientId the client id
	 */
	public SymProtocol(MessageHandlerClientSide c) {
		super(c);
		this.symMetaDataMap = new ConcurrentHashMap<ProxyTxnId, SymMetaData>();
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.Protocol#getPermission(org.mpi.vasco.txstore.util.ProxyTxnId, org.mpi.vasco.coordination.protocols.util.LockRequest)
	 */
	@Override
	public LockReply getPermission(ProxyTxnId txnId, LockRequest lcR) {
		LockReqMessage msg = new LockReqMessage(txnId,
				client.getMyId(), lcR);
		
		client.sendToLockServer(msg);
		
		SymMetaData meta = this.addInitialMetaData(txnId, lcR);
		synchronized(meta){
			while(meta.getLockReply() == null){
				try {
					meta.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		//return the lcReply and remove it from the set
		this.cleanUpMetaData(txnId);
		return meta.getLockReply();
	}

	@Override
	public void addLockReply(ProxyTxnId txnId, LockReply lcReply) {
		SymMetaData meta = this.symMetaDataMap.get(txnId);
		if(meta == null){
			throw new RuntimeException("Meta for " + txnId.toString() + " doesn't exist");
		}
		synchronized(meta){
			meta.setLockReply(lcReply);
			meta.notify();
		}
	}
	
	public SymMetaData addInitialMetaData(ProxyTxnId txnId, LockRequest lcR){
		SymMetaData meta = new SymMetaData(lcR, null);
		this.symMetaDataMap.put(txnId, meta);
		return meta;
	}
	
	private void cleanUpMetaData(ProxyTxnId txnId){
		this.symMetaDataMap.remove(txnId);
	}

}
