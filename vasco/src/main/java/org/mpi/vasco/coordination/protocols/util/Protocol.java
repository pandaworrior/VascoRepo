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
package org.mpi.vasco.coordination.protocols.util;

import java.util.Set;

import org.mpi.vasco.coordination.MessageHandlerClientSide;
import org.mpi.vasco.txstore.util.ProxyTxnId;

import bftsmart.tom.ServiceProxy;

// TODO: Auto-generated Javadoc
/**
 * The Class Protocol.
 */
public interface Protocol {
	
	/** The Constant NUM_OF_PROTOCOLS. */
	public final static int NUM_OF_PROTOCOLS = 2;
	
	/** The Constant PROTOCOL_SYM. */
	public final static int PROTOCOL_SYM = 0;
	
	/** The Constant PROTOCOL_ASYM. */
	public final static int PROTOCOL_ASYM = 1;
	
	/**
	 * Gets the permission for executing an operation.
	 *
	 * @param txnId the txn id
	 * @param lcR the lc r
	 * @return the permission
	 */
	public abstract LockReply getPermission(ProxyTxnId txnId, LockRequest lcR);
	
	/**
	 * Gets the local permission, generate a reply to an asym protocol
	 * wait for being aggregated.
	 *
	 * @param txnId the txn id
	 * @param lcR the lc r
	 * @return the local permission
	 */
	public abstract LockReply getLocalPermission(ProxyTxnId txnId, LockRequest lcR);
	
	
	public abstract void waitForBeExcuted(ProxyTxnId txnId, LockRequest lcR);
	
	/**
	 * Adds the lock reply.
	 *
	 * @param txnId the txn id
	 * @param lcReply the lc reply
	 */
	public abstract void addLockReply(ProxyTxnId txnId, LockReply lcReply);
	
	
	/**
	 * cleanUp by active requests. for example, the barrier initiator
	 * @param txnId
	 */
	public abstract void cleanUp(ProxyTxnId txnId, Set<String> keys, String opName);
	
	/*
	 * cleanUp upon receiving a request, for example, the barrier participants
	 */
	public abstract void cleanUpLocal(ProxyTxnId txnId, Set<String> keys, String opName);
	
	//public abstract void waitForPermissionLocalMatch();
}
