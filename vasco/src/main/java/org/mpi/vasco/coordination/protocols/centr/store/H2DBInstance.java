package org.mpi.vasco.coordination.protocols.centr.store;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class H2DBInstance extends PersistentDB implements PersistentDBImpl{
	
	private static final Logger log = Logger.getLogger( H2DBInstance.class.getName() );

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

	@Override
	public Connection createConn() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
        Connection c = DriverManager.
            getConnection(super.getUrl(), super.getUsername(), super.getPasswd());
		return c;
	}

	@Override
	public void init() throws ClassNotFoundException, SQLException {
		super.setCon(createConn());
		super.setStmt(createStmt());
		this.disableAutoCommit();
		this.setIsolationLevel();
	}

	@Override
	public ResultSet executeTxn(String sql) {
		log.log(Level.SEVERE, "Not implemented yet");
		return null;
	}

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

	@Override
	public void disableAutoCommit() throws SQLException {
		super.con.setAutoCommit(false);
	}

	@Override
	public void setIsolationLevel() throws SQLException{
		String sql = "SET LOCK_MODE 1";
		super.getStmt().execute(sql);
	}

	@Override
	public Statement createStmt() throws SQLException {
		return super.getCon().createStatement();
	}

}
