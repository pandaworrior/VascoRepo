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

// TODO: Auto-generated Javadoc
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
		try {
			init();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			log.log(Level.SEVERE, e.toString());
		}
		log.log(Level.CONFIG, "H2DBInstance has been successfully launched!");
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentDBImpl#createConn()
	 */
	@Override
	public Connection createConn() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
        Connection c = DriverManager.
            getConnection(super.getUrl(), super.getUsername(), super.getPasswd());
		return c;
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentDBImpl#init()
	 */
	@Override
	public void init() throws ClassNotFoundException, SQLException {
		super.setCon(createConn());
		super.setStmt(createStmt());
		this.disableAutoCommit();
		this.setIsolationLevel();
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentDBImpl#executeTxn(java.lang.String)
	 */
	@Override
	public ResultSet executeTxn(String sql) {
		log.log(Level.SEVERE, "Not implemented yet");
		return null;
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentDBImpl#executeTxnInBatch(java.lang.String, java.util.List)
	 */
	@Override
	public ResultSet executeTxnInBatch(String readSql, List<String> writeSql) {
		ResultSet rs = null;
		
		for(;;){
			try {
				rs = super.getStmt().executeQuery(readSql);
				
				assert(writeSql.size() >= 1);
				
				for(int i = 0; i < writeSql.size();i++){
					super.getStmt().addBatch(writeSql.get(i));
				}
				
				super.getStmt().executeBatch();
				
				super.getCon().commit();
				break;
				
			} catch (SQLException e) {
				log.log(Level.SEVERE, "Problem when executing txn " + e.toString());
			}finally{
				try {
					rs.close();
					super.con.rollback();
				} catch (SQLException e) {
					log.log(Level.SEVERE, "Problem when clear up txn " + e.toString());
				}
			}
		}
		
		return rs;
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentDBImpl#disableAutoCommit()
	 */
	@Override
	public void disableAutoCommit() throws SQLException {
		super.con.setAutoCommit(false);
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentDBImpl#setIsolationLevel()
	 */
	@Override
	public void setIsolationLevel() throws SQLException{
		String sql = "SET LOCK_MODE 1";
		super.getStmt().execute(sql);
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.coordination.protocols.centr.store.PersistentDBImpl#createStmt()
	 */
	@Override
	public Statement createStmt() throws SQLException {
		return super.getCon().createStatement();
	}

}
