/*
 * Scratchpad to commit shadow operations
 */
package org.mpi.vasco.txstore.scratchpad.rdbms;
import org.mpi.vasco.util.debug.Debug;

import java.text.DateFormat;
import java.util.*;
import org.mpi.vasco.txstore.scratchpad.*;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBOpPair;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBReadSetEntry;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSingleOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBWriteSetEntry;
import org.mpi.vasco.txstore.scratchpad.resolution.ExecutionPolicy;
import org.mpi.vasco.txstore.util.*;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseFunction;

import java.sql.*;

import net.sf.jsqlparser.*;
import net.sf.jsqlparser.parser.*;
import net.sf.jsqlparser.statement.update.Update;

public class DBCommitScratchpad
	implements ScratchpadInterface, IDBScratchpad
{
	public static final int RDBMS_H2 = 1;
	public static final int RDBMS_MYSQL = 2;
	public static final int RDBMS_MIMER = 3;
	public static int SQL_ENGINE = RDBMS_MYSQL;
	
	public static final String SCRATCHPAD_NULL = "@$NULL";
	public static final String SCRATCHPAD_PREFIX = "SCRATCHPAD";
	public static final String SCRATCHPAD_TABLE_ALIAS_PREFIX = "_SPT_";
	public static final String SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX = "_TSPT_";
	public static final String SCRATCHPAD_COL_PREFIX = "_SP_";
	public static final String SCRATCHPAD_COL_DELETED = "_SP_del";
	public static final String SCRATCHPAD_COL_TS = "_SP_ts";
	public static final String SCRATCHPAD_COL_VV = "_SP_clock";
	
	
	protected ScratchpadConfig config;
	protected Connection conn;
	protected Statement statQ;
	protected Statement statU;
	protected Statement statBU;
	protected boolean batchEmpty;
	protected int id;
	protected ProxyTxnId curTxId;
	protected CCJSqlParserManager parser;

    protected DBScratchpadFactory myFactory;
    
    //added by the req	uirement from SIEVE
    DateFormat dateFormat;
	
	public DBCommitScratchpad( ScratchpadConfig config) throws ScratchpadException {
		Debug.println("Scratchpad init1\n");
		this.dateFormat = DatabaseFunction.getNewDateFormatInstance();
		init( config.duplicate());
	}
	
    public void setFactory(DBScratchpadFactory fac){
	myFactory = fac;
    }
    
    public static void prepareDBScratchpad( ScratchpadConfig config) throws ScratchpadException {
		if( config.getURL().indexOf( ":h2:") != -1)
			SQL_ENGINE = RDBMS_H2;
		else if( config.getURL().indexOf( ":mysql:") != -1)
			SQL_ENGINE = RDBMS_MYSQL;
		else if( config.getURL().indexOf( ":mimer:") != -1)
			SQL_ENGINE = RDBMS_MIMER;
		if( SQL_ENGINE != RDBMS_MIMER) {
			Connection conn = null;
    	try {
			Class.forName( config.getDriver());
			conn = DriverManager.getConnection(config.getURL(), config.getUser(), config.getPassword());
			conn.setTransactionIsolation( Connection.TRANSACTION_REPEATABLE_READ);
			conn.setAutoCommit( false);
			conn.commit();
			Debug.println("Get a connection\n");
			conn.close();
		} catch( Exception e) {
			if( conn != null)
				try {
					conn.close();
				} catch (SQLException e1) {
					// do nothing
					e1.printStackTrace();
				}
			Debug.println( "Scratchpad tables already exist");
		}
		} else {
			Connection conn = null;
	    	try {
				Class.forName( config.getDriver());
				conn = DriverManager.getConnection(config.getURL(), config.getUser(), config.getPassword());
				conn.setTransactionIsolation( Connection.TRANSACTION_REPEATABLE_READ);
				conn.setAutoCommit( false);
				conn.commit();
				Debug.println("Get a connection\n");
			} catch( Exception e) {
				if( conn != null)
					try {
						conn.close();
					} catch (SQLException e1) {
						// do nothing
						e1.printStackTrace();
					}
			}
			
		}
		
    }

	protected void init( ScratchpadConfig config) throws ScratchpadException {
		try {
			Class.forName( config.getDriver());
			this.config = config;
			this.parser = new CCJSqlParserManager() ;
			if( config.getURL().indexOf( ":h2:") != -1)
				SQL_ENGINE = RDBMS_H2;
			else if( config.getURL().indexOf( ":mysql:") != -1)
				SQL_ENGINE = RDBMS_MYSQL;
			else if( config.getURL().indexOf( ":mimer:") != -1)
				SQL_ENGINE = RDBMS_MIMER;
			//comments added by Cheng Li
			Debug.println("Scratchpad init2\n");
			Debug.printf("config %s, %s, %s, %s\n",config.getDriver(), config.getURL(), config.getUser(), config.getPassword());
			conn = DriverManager.getConnection(config.getURL(), config.getUser(), config.getPassword());
			conn.setTransactionIsolation( Connection.TRANSACTION_REPEATABLE_READ);
			conn.setAutoCommit( false);
			conn.commit();
			Debug.println("Get a connection\n");
			statQ = conn.createStatement();
			statU = conn.createStatement();
			statBU = conn.createStatement();
			batchEmpty = true;
	//		getDBDefinition();
			scratchpadDBInits();
		} catch( Exception e) {
			throw new ScratchpadException( e);
		}
	}	
	
	protected void scratchpadDBInits() throws SQLException, ScratchpadException {
		if( SQL_ENGINE == RDBMS_MIMER) {
			scratchpadDBInitsMimer();
			return;
		}
        Debug.println("DBScratchpad init3\n");
		DatabaseMetaData dm = conn.getMetaData() ;
		String[] types = {"TABLE"} ;
		ResultSet tblSet = dm.getTables( null, null, "%", types ) ;
		
		ArrayList<String> tables = new ArrayList<String>();
		while( tblSet.next() )
		{
			String tableName = tblSet.getString( 3 );
			if( tableName.startsWith( SCRATCHPAD_PREFIX))
				continue;
			tables.add(tableName);
		}
		Collections.sort( tables);
			
		for( int i = 0; i < tables.size(); i++) {
			String tableName = tables.get(i);
			ExecutionPolicy policy = config.getPolicy(tableName);
			if( policy == null)
				Debug.println( "No config for table " + tableName);
			else 
				policy.init( dm, tableName, id, i);
			conn.commit();
		}
		
	}
	
	//TODO: need to change finally
	protected void scratchpadDBInitsMimer() throws SQLException, ScratchpadException {
        Debug.println("DBScratchpad init3\n");
		for( ; ; ) {
			try {
				ResultSet rs = statQ.executeQuery( "SELECT id FROM " + SCRATCHPAD_PREFIX + "_ID WHERE k = 1");
				rs.next();
				id =  rs.getInt(1);
				statU.executeUpdate( "UPDATE " + SCRATCHPAD_PREFIX + "_ID SET id = id + 1 WHERE k = 1");
				conn.commit();
				break;
			} catch( SQLException e) {
				// do nothing
				e.printStackTrace();
			}
		}
		Debug.println( "Id : " + id);
		DatabaseMetaData dm = conn.getMetaData() ;
		String[] types = {"TABLE"} ;
		ResultSet tblSet = dm.getTables( null, null, "%", types ) ;
		
		ArrayList<String> tables = new ArrayList<String>();
		while( tblSet.next() )
		{
			String tableName = tblSet.getString( 3 );
			if( tableName.startsWith( SCRATCHPAD_PREFIX))
				continue;
			tables.add(tableName);
		}
		Collections.sort( tables);
			
		for( int i = 0; i < tables.size(); i++) {
			String tableName = tables.get(i);
			ExecutionPolicy policy = config.getPolicy(tableName);
			if( policy == null)
				Debug.println( "No config for table " + tableName);
			else 
				policy.init( dm, tableName, id, i);
			//conn.commit();
		}
		
	}

	public void beginTransaction(ProxyTxnId txnId) {
		try {
		Debug.println("begin Txn " + txnId);
		curTxId = txnId;
		statBU.clearBatch();
		batchEmpty = true;
		/*try {
			statQ.executeQuery( "SELECT id FROM " + SCRATCHPAD_PREFIX + "_TRX WHERE k = 2;");
		} catch( SQLException e) {
			// do nothing
			e.printStackTrace();
		}*/
		} catch( SQLException e) {
			Debug.kill(e);
		}
	}


	public void abort() throws ScratchpadException {
		try {
			conn.rollback();
		} catch (SQLException e) {
			throw new ScratchpadException( e);
		}
	}
	
	
	public void commitShadowOP(Operation op,  final LogicalClock lc, final TimeStamp ts) throws ScratchpadException{
		for( ;;) {
		try {
			cleanBatch();
			final Set<String> deletedTables = new HashSet<String>();
			DBOperation dbOp0 = null;
			dbOp0 = DBOperation.decode(op);
			
			if( dbOp0 instanceof DBShadowOperation){
				Debug.println("apply log shadow Op " + lc);
				DBShadowOperation dbShadowOp = (DBShadowOperation)dbOp0;
				
				dbShadowOp.setTimestamp(lc, ts);
				dbShadowOp.setDateFormat(this.dateFormat);
				
				//statBU.executeBatch();//TODO: simply it
				//final PrimaryExecResults results = new PrimaryExecResults();
				//final boolean registerIndividual = dbShadowOp.registerIndividualOperations();
				final IDBScratchpad mainDB = this;
				final IDBScratchpad dbs = this;
				
				dbShadowOp.executeShadow( new IDefDatabase() {

					public ResultSet executeQuery(String sql) throws SQLException {
						return dbs.executeQuery(sql);
					}
					
					public void addCleanUpToBatch(String sql) throws SQLException{
						long cleanTs = (long) (ts.toLong()*0.9);
						sql += " AND " + SCRATCHPAD_COL_DELETED + "<> 0 AND " + SCRATCHPAD_COL_TS + " < " + Long.toString(cleanTs);
						mainDB.addToBatchUpdate(sql);
					}
					
					/*
					 * Execute the sql statement without any modification
					 * (non-Javadoc)
					 * @see org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase#executeOp(java.lang.String)
					 */
					public int executeOp(String sql)throws SQLException, ScratchpadException{
						mainDB.addToBatchUpdate(sql);
						return 1;
					}

					public int executeUpdate(String sql) throws SQLException, ScratchpadException {
						throw new RuntimeException("For SIEVE you should not call this function");
					}
				});
			}else
				throw new RuntimeException( "Expecting DBOperation, but object of class " + op.getClass().getName()); 
			int []result = statBU.executeBatch();
			Debug.println( "Batch result = " + result.length);
            conn.commit();
			Debug.println("batch commit " + lc);
			break;
		} catch( BatchUpdateException e) {
			System.err.println("an exception happens 1 " + lc);
			e.printStackTrace();
		} catch( Exception e) {
			System.err.println("an exception happens 2 " + lc);
			e.printStackTrace();
			throw new ScratchpadException( e);
		}
		try {
			conn.rollback();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}



	public ResultSet executeQuery(String op) throws SQLException {
		Debug.println("execute query " + op);
		return statQ.executeQuery(op);
	}

	public int executeUpdate(String op) throws SQLException {
		return statU.executeUpdate(op);
	}
	public void addToBatchUpdate( String op) throws SQLException {
		statBU.addBatch(op);
		batchEmpty = false;
	}
	public void executeBatch() throws SQLException {
		if( batchEmpty)
			return;
		statBU.executeBatch();
		statBU.clearBatch();
		batchEmpty = true;
	}
	
	public void cleanBatch() throws SQLException{
		if(batchEmpty)
			return;
		statBU.clearBatch();
		batchEmpty = true;
	}

	public boolean isReadOnly() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addToWriteSet(DBWriteSetEntry entry) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean addToReadSet(DBReadSetEntry readSetEntry) {
		// TODO Auto-generated method stub
		return false;
	}

	public void addToOpLog(DBOpPair op) {
		// TODO Auto-generated method stub
		
	}

	public ReadSet getReadSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public WriteSet getWriteSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public Result execute(Operation op) throws ScratchpadException {
		// TODO Auto-generated method stub
		return null;
	}

	public ReadWriteSet complete() {
		// TODO Auto-generated method stub
		return null;
	}

	public OperationLog getOperationLog() throws ScratchpadException {
		// TODO Auto-generated method stub
		return null;
	}

	public OperationLog commit(LogicalClock lc, TimeStamp ts)
			throws ScratchpadException {
		// TODO Auto-generated method stub
		return null;
	}

	public void applyOperationLog(OperationLog opLog)
			throws ScratchpadException {
		// TODO Auto-generated method stub
		
	}

	public void finalize(LogicalClock lc, TimeStamp ts)
			throws ScratchpadException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ResultSet executeOrig(Operation op) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.txstore.scratchpad.rdbms.IDBScratchpad#executeOp(java.lang.String)
	 */
	public int executeOp(String op) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

}
