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
/*
 * This class is doing x;
 * Created by @Creator on @Date
 */
package org.mpi.vasco.coordination.membership;

import java.net.InetAddress;

import org.mpi.vasco.network.Principal;

// TODO: Auto-generated Javadoc
/**
 * The Class LockServer.
 */
public class LockClient extends Principal {
	
	/** The client id. */
	private int clientId;

	/**
	 * Instantiates a new lock server.
	 *
	 * @param cId the c id
	 * @param host the host
	 * @param port the port
	 */
	public LockClient(int cId, String host, int port) {
		super(host, port);
		this.setClientId(cId);
	}

	/**
	 * Instantiates a new lock server.
	 *
	 * @param cId the c id
	 * @param host the host
	 * @param port the port
	 */
	public LockClient(int cId, InetAddress host, int port) {
		super(host, port);
		this.setClientId(cId);
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.network.Principal#toString()
	 */
	public String toString() {
		return "++ LOCKCLIENT " + this.getClientId() + " " + super.toString();
	}

	/**
	 * Gets the client id.
	 *
	 * @return the client id
	 */
	public int getClientId() {
		return clientId;
	}

	/**
	 * Sets the client id.
	 *
	 * @param clientId the new client id
	 */
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

}