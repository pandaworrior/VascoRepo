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
public class LockServer extends Principal {
	
	/** The server id. */
	private int serverId;
	
	/** The db port. */
	private int dbPort;

	/**
	 * Instantiates a new lock server.
	 *
	 * @param sId the s id
	 * @param host the host
	 * @param port the port
	 * @param dbPort the db port
	 */
	public LockServer(int sId, String host, int port, int dbPort) {
		super(host, port);
		this.setServerId(sId);
		this.setDbPort(dbPort);
	}

	/**
	 * Instantiates a new lock server.
	 *
	 * @param sId the s id
	 * @param host the host
	 * @param port the port
	 * @param dbPort the db port
	 */
	public LockServer(int sId, InetAddress host, int port, int dbPort) {
		super(host, port);
		this.setServerId(sId);
		this.setDbPort(dbPort);
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.network.Principal#toString()
	 */
	public String toString() {
		return "++ LOCKSERVER " + this.getServerId() + " " + super.toString();
	}

	/**
	 * Gets the server id.
	 *
	 * @return the server id
	 */
	public int getServerId() {
		return serverId;
	}

	/**
	 * Sets the server id.
	 *
	 * @param serverId the new server id
	 */
	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	/**
	 * Gets the db port.
	 *
	 * @return the db port
	 */
	public int getDbPort() {
		return dbPort;
	}

	/**
	 * Sets the db port.
	 *
	 * @param dbPort the new db port
	 */
	public void setDbPort(int dbPort) {
		this.dbPort = dbPort;
	}

}