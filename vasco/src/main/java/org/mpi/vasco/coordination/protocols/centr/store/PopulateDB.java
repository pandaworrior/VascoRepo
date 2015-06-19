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

public class PopulateDB extends PersistentDB implements PersistentDBImpl{

	PopulateDB(String _url, String _username, String _passwd) {
		super(_url, _username, _passwd);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Connection createConn() throws ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Statement createStmt() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init() throws ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disableAutoCommit() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIsolationLevel() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ResultSet executeTxn(String sql) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSet executeTxnInBatch(String readSql, List<String> writeSql) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args){
		
	}

}
