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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mpi.vasco.util.debug.Debug;

/**
 * The Class H2DBInstance.
 */
public class H2DBInstance extends PersistentDB implements PersistentDBImpl{
	
	/** The Constant log. */
	private static final Logger log = Logger.getLogger( H2DBInstance.class.getName() );

	/**
	 * Instantiates a new h2 db instance.
	 *
	 * @param _url the _url
	 * @param _username the _username
	 * @param _passwd the _passwd
	 */
	H2DBInstance(String _url, String _username, String _passwd) {
		super(_url, _username, _passwd);
		init();
		log.log(Level.CONFIG, "H2DBInstance has been successfully launched!");
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentconnDBImpl#createConn()
	 */
	@Override
	public Connection createConn(){
		Connection c = null;
		try {
			Class.forName("org.h2.Driver");
			c = DriverManager.
		            getConnection(super.getUrl(), super.getUsername(), super.getPasswd());
			this.disableAutoCommit(c);
			this.setIsolationLevel(c);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return c;
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentDBImpl#init()
	 */
	@Override
	public void init(){
		this.createConnInBatch(20);
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentDBImpl#executeTxn(java.lang.String)
	 */
	@Override
	public ResultSet executeReadTxn(String sql) {// TODO Auto-generated catch block
		log.log(Level.SEVERE, "Not implemented yet");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentDBImpl#executeTxnInBatch(java.lang.String, java.util.List)
	 */
	@Override
	public ResultSet executeTxnInBatch(String readSql, List<String> writeSql) {
		Connection conn;
		conn = this.getRealConn();
		ResultSet rs = null;
		Statement st = this.createStmt(conn);
		for(;;){
			try {
				rs = st.executeQuery(readSql);
				st.clearBatch();
				
				assert(writeSql.size() >= 1);
				
				for(int i = 0; i < writeSql.size();i++){
					st.addBatch(writeSql.get(i));
				}
				
				st.executeBatch();
				conn.commit();
				break;
				
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Problem when executing txn " + e.toString());
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
					System.exit(-1);
				}
			}finally{
				try {
					rs.close();
					st.close();
				} catch (SQLException e) {
					log.log(Level.SEVERE, "Problem when clear up txn " + e.toString());
				}
			}
		}
		
		return rs;
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersisargstentDBImpl#disableAutoCommit()
	 */
	@Override
	public void disableAutoCommit(Connection conn) {
		try {
			conn.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentDBImpl#setIsolationLevel()
	 */
	@Override
	public void setIsolationLevel(Connection conn){
		String sql = "SET LOCK_MODE 1";
		Statement st = this.createStmt(conn);
		try {
			st.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void createConnInBatch(int numOfConn){
		Debug.printf("Create %d connections\n", numOfConn);
		while(numOfConn > 0){
			numOfConn--;
			Connection conn = this.createConn();
			this.returnConn(conn);
		}
	}
	
	private Connection getRealConn(){
		Connection conn = super.getCon();
		if(conn == null){
			Debug.printf("Create a new connection and the num of connection %d\n", this.getConPool().numOfObject());
			conn = this.createConn();
		}
		return conn;
	}

	@Override
	public Statement createStmt(Connection conn) {
		Statement st = null;
		try {
			st = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return st;
	}

	@Override
	public int executeUpdateTxn(String sql) {
		Connection conn = this.getRealConn();
		int result = -1;
		Statement st = this.createStmt(conn);
		try {
			result = st.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return result;
	}

}
