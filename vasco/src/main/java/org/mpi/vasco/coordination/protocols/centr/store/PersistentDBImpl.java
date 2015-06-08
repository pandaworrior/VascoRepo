package org.mpi.vasco.coordination.protocols.centr.store;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public interface PersistentDBImpl {
	
	//create a connection
	Connection createConn() throws ClassNotFoundException, SQLException;
	
	Statement createStmt() throws SQLException;
	
	//initialize all configuration with database
	void init()throws ClassNotFoundException, SQLException;
	
	void disableAutoCommit() throws SQLException;
	
	void setIsolationLevel() throws SQLException;
	
	ResultSet executeTxn(String sql);
	
	//execute this transaction in a loop, stop when it succeeds
	ResultSet executeTxnInBatch(String readSql, List<String> writeSql);
}
