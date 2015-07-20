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
package org.mpi.vasco.coordination.protocols.centr.store;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mpi.vasco.util.ObjectPool;

/**
 * The Class PersistentDB.
 */
public abstract class PersistentDB{
	
	/** The con. */
	ObjectPool<Connection> conPool;
	
	/** The url. */
	String url;
	
	/** The username. */
	String username;
	
	/** The passwd. */
	String passwd;
	
	/** The str build. */
	StringBuilder strBuild;
	
	/** The Constant log. */
	private static final Logger log = Logger.getLogger( PersistentDB.class.getName() );
	
	/**
	 * Instantiates a new persistent db.
	 *
	 * @param _url the _url
	 * @param _username the _username
	 * @param _passwd the _passwd
	 */
	PersistentDB(String _url, String _username, String _passwd){
		log.log(Level.CONFIG, "Initializing PersistentDB");
		this.setUrl(_url);
		this.setUsername(_username);
		this.setPasswd(_passwd);
		this.setStrBuild(new StringBuilder());
		this.setConPool(new ObjectPool<Connection>());
	}

	/**
	 * Gets the con.
	 *
	 * @return the con
	 */
	public Connection getCon() {
		Connection conn = this.getConPool().borrowObject();
		return conn;
	}

	/**
	 * Gets the url.
	 *et
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 *
	 * @param username the new username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the passwd.
	 *
	 * @return the passwd
	 */	
	public String getPasswd() {
		return passwd;
	}

	/**
	 * Sets the passwd.
	 *
	 * @param passwd the new passwd
	 */
	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	/**
	 * Gets the str build.
	 *
	 * @return the str build
	 */
	public StringBuilder getStrBuild() {
		return strBuild;
	}

	/**
	 * Sets the str build.
	 *
	 * @param strBuild the new str build
	 */
	public void setStrBuild(StringBuilder strBuild) {
		this.strBuild = strBuild;
	}

	public ObjectPool<Connection> getConPool() {
		return conPool;
	}

	public void setConPool(ObjectPool<Connection> conPool) {
		this.conPool = conPool;
	}
	
	public void returnConn(Connection conn){
		this.conPool.returnObject(conn);
	}
}
