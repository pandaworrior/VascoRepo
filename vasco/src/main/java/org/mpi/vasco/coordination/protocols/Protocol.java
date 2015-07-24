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

import org.mpi.vasco.coordination.MessageHandlerClientSide;
import org.mpi.vasco.coordination.membership.Role;
import org.mpi.vasco.coordination.protocols.util.LockReply;
import org.mpi.vasco.coordination.protocols.util.LockRequest;
import org.mpi.vasco.txstore.util.ProxyTxnId;

/**
 * The Class Protocol.
 */
public abstract class Protocol {
	
	/** The Constant NUM_OF_PROTOCOLS. */
	public final static int NUM_OF_PROTOCOLS = 2;
	
	/** The Constant PROTOCOL_SYM. */
	public final static int PROTOCOL_SYM = 0;
	
	/** The Constant PROTOCOL_ASYM. */
	public final static int PROTOCOL_ASYM = 1;
	
	
	/** The client. */
	MessageHandlerClientSide client;
	
	public Protocol(MessageHandlerClientSide c){
		this.setClient(c);
	}
	
	/**
	 * Gets the permission for executing an operation.
	 *
	 * @param txnId the txn id
	 * @param lcR the lc r
	 * @return the permission
	 */
	public abstract LockReply getPermission(ProxyTxnId txnId, LockRequest lcR);
	
	/**
	 * Adds the lock reply.
	 *
	 * @param txnId the txn id
	 * @param lcReply the lc reply
	 */
	public abstract void addLockReply(ProxyTxnId txnId, LockReply lcReply);
	
	
	public void setClient(MessageHandlerClientSide client){
		this.client = client;
	}
	
	/**
	 * Gets the message client.
	 *
	 * @return the message client
	 */
	public MessageHandlerClientSide getMessageClient(){
		return client;
	}
}
