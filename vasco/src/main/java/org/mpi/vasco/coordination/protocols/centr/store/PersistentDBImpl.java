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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Interface PersistentDBImpl.
 */
public interface PersistentDBImpl {
	
	//create a connection
	/**
	 * Creates the conn.
	 *
	 * @return the connection
	 * @throws ClassNotFoundException the class not found exception
	 * @throws SQLException the SQL exception
	 */
	Connection createConn() throws ClassNotFoundException, SQLException;
	
	/**
	 * Creates the stmt.
	 *
	 * @return the statement
	 * @throws SQLException the SQL exception
	 */
	Statement createStmt() throws SQLException;
	
	//initialize all configuration with database
	/**
	 * Inits the.
	 *
	 * @throws ClassNotFoundException the class not found exception
	 * @throws SQLException the SQL exception
	 */
	void init()throws ClassNotFoundException, SQLException;
	
	/**
	 * Disable auto commit.
	 *
	 * @throws SQLException the SQL exception
	 */
	void disableAutoCommit() throws SQLException;
	
	/**
	 * Sets the isolation level.
	 *
	 * @throws SQLException the SQL exception
	 */
	void setIsolationLevel() throws SQLException;
	
	/**
	 * Execute txn.
	 *
	 * @param sql the sql
	 * @return the result set
	 */
	ResultSet executeTxn(String sql);
	
	//execute this transaction in a loop, stop when it succeeds
	/**
	 * Execute txn in batch.
	 *
	 * @param readSql the read sql
	 * @param writeSql the write sql
	 * @return the result set
	 */
	ResultSet executeTxnInBatch(String readSql, List<String> writeSql);
}
