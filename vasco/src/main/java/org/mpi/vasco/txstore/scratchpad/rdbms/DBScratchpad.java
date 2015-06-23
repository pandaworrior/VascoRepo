package org.mpi.vasco.txstore.scratchpad.rdbms;
import org.mpi.vasco.util.debug.Debug;

import java.util.*;
import org.mpi.vasco.txstore.scratchpad.*;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBGenericOpPair;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBGenericOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBOpPair;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBOperationLog;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBReadSetEntry;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSelectResult;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSingleOpPair;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSingleOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBUpdateResult;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBWriteSetEntry;
import org.mpi.vasco.txstore.scratchpad.resolution.ExecutionPolicy;
import org.mpi.vasco.txstore.util.*;

import java.sql.*;

import net.sf.jsqlparser.*;
import net.sf.jsqlparser.parser.*;

public class DBScratchpad
	implements ScratchpadInterface, IDBScratchpad
{
	public static final int RDBMS_H2 = 1;
	public static final int RDBMS_MYSQL = 2;
	public static final int RDBMS_MIMER = 3;
	public static int SQL_ENGINE = RDBMS_MYSQL;
	
	public static final String SCRATCHPAD_NULL = "@$NULL";
	public static final String SCRATCHPAD_PREFIX = "SCRATCHPAD";
	/*
	public static final String SCRATCHPAD_TABLE_ALIAS_PREFIX = "SP_SPT_";
	public static final String SCRATCHPAD_TEMPTABLE_ALIAS_PREFIX = "SP_TSPT_";
	public static final String SCRATCHPAD_COL_PREFIX = "SP_SP_";
	public static final String SCRATCHPAD_COL_DELETED = "SP_SP_del";
	public static final String SCRATCHPAD_COL_TS = "SP_SP_ts";
	public static final String SCRATCHPAD_COL_VV = "SP_SP_clock";
	*/
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
	
	protected Set<WriteSetEntry> writeSet;
	protected Set<ReadSetEntry> readSet;
	protected WriteSet writeSetFinal;
	protected ReadSet readSetFinal;
	protected List<DBOpPair> opLog;
	protected DBOperationLog opLogFinal;
	protected boolean remote;
	protected boolean readOnly;

    protected DBScratchpadFactory myFactory;
	
	public DBScratchpad( ScratchpadConfig config) throws ScratchpadException {
		Debug.println("Scratchpad init1\n");
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
			Statement statU = conn.createStatement();

			statU.execute( "CREATE TABLE IF NOT EXISTS " + SCRATCHPAD_PREFIX + "_ID ( k int NOT NULL primary key, id int);");
			conn.commit();
			statU.execute( "INSERT INTO " + SCRATCHPAD_PREFIX + "_ID VALUES ( 1, 1);");
			conn.commit();
			statU.execute( "CREATE TABLE IF NOT EXISTS " + SCRATCHPAD_PREFIX + "_TRX ( k int NOT NULL primary key, id int);");
			conn.commit();
			statU.execute( "INSERT INTO " + SCRATCHPAD_PREFIX + "_TRX VALUES ( 1, 1);");
			conn.commit();
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
				Statement statU = conn.createStatement();

				statU.execute( "CREATE TABLE " + SCRATCHPAD_PREFIX + "_ID ( k int NOT NULL primary key, id int)");
				conn.commit();
				statU.execute( "INSERT INTO " + SCRATCHPAD_PREFIX + "_ID VALUES ( 1, 1)");
				conn.commit();
				statU.execute( "CREATE TABLE " + SCRATCHPAD_PREFIX + "_TRX ( k int NOT NULL primary key, id int)");
				conn.commit();
				statU.execute( "INSERT INTO " + SCRATCHPAD_PREFIX + "_TRX VALUES ( 1, 1)");
				conn.commit();
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
			
		}
		
    }

	protected void init( ScratchpadConfig config) throws ScratchpadException {
		try {
			Class.forName( config.getDriver());
			this.writeSet = new HashSet<WriteSetEntry>();
			writeSetFinal = null;
			this.readSet = new HashSet<ReadSetEntry>();
			readSetFinal = null;
			this.opLog = new ArrayList<DBOpPair>();
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
			readOnly = true;
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
/*		Debug.println("DBScratchpad init1\n");
		try {
			statU.execute( "CREATE TABLE IF NOT EXISTS " + SCRATCHPAD_PREFIX + "_ID ( k int NOT NULL primary key, id int);");
			Debug.println("DBScratchpad init2\n");
			statU.execute( "INSERT INTO " + SCRATCHPAD_PREFIX + "_ID VALUES ( 1, 1);");
			conn.commit();
			statU.execute( "CREATE TABLE IF NOT EXISTS " + SCRATCHPAD_PREFIX + "_TRX ( k int NOT NULL primary key, id int);");
			Debug.println("DBScratchpad init2\n");
			statU.execute( "INSERT INTO " + SCRATCHPAD_PREFIX + "_TRX VALUES ( 1, 1);");
			conn.commit();
		} catch( SQLException e) {
			conn.rollback();
		}
*/		Debug.println("DBScratchpad init3\n");
	synchronized( this) {
		for( ; ; ) {
			try {
				ResultSet rs = statQ.executeQuery( "SELECT id FROM " + SCRATCHPAD_PREFIX + "_ID WHERE k = 1;");
				rs.next();
				id =  rs.getInt(1);
				statU.executeUpdate( "UPDATE " + SCRATCHPAD_PREFIX + "_ID SET id = id + 1 WHERE k = 1;");
				conn.commit();
				break;
			} catch( SQLException e) {
				// do nothing
				e.printStackTrace();
			}
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
				policy.init( dm, tableName, id, i, this);
			conn.commit();
		}
		
	}
	
	protected void scratchpadDBInitsMimer() throws SQLException, ScratchpadException {
/*		Debug.println("DBScratchpad init1\n");
		try {
			statU.execute( "CREATE TABLE " + SCRATCHPAD_PREFIX + "_ID ( k int NOT NULL primary key, id int)");
			Debug.println("DBScratchpad init2\n");
			statU.execute( "INSERT INTO " + SCRATCHPAD_PREFIX + "_ID VALUES ( 1, 1)");
			conn.commit();
			statU.execute( "CREATE TABLE " + SCRATCHPAD_PREFIX + "_TRX ( k int NOT NULL primary key, id int)");
			Debug.println("DBScratchpad init2\n");
			statU.execute( "INSERT INTO " + SCRATCHPAD_PREFIX + "_TRX VALUES ( 1, 1)");
			conn.commit();
		} catch( SQLException e) {
			conn.rollback();
		}
*/		Debug.println("DBScratchpad init3\n");
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
				policy.init( dm, tableName, id, i, this);
			conn.commit();
		}
		
	}

	@Override
	public void beginTransaction(ProxyTxnId txnId) {
		try {
		Debug.println("begin Txn " + txnId);
		writeSet.clear();
		readSet.clear();
		readSetFinal = null;
		writeSetFinal = null;
		opLog.clear();
		readOnly = true;
		remote = false;
		curTxId = txnId;
		statBU.clearBatch();
		Iterator<ExecutionPolicy> it = config.getPolicies().iterator();
		while( it.hasNext())
			it.next().beginTx( this);
		statBU.executeBatch();
		conn.commit();
		statBU.clearBatch();
		try {
			statQ.executeQuery( "SELECT id FROM " + SCRATCHPAD_PREFIX + "_TRX WHERE k = 2;");
		} catch( SQLException e) {
			// do nothing
			e.printStackTrace();
		}
		} catch( SQLException e) {
			Debug.kill(e);
		}
/*		try {
		ResultSet rs = statQ.executeQuery( "SELECT * FROM t1_" + id);
		if( rs.next()) {
			Debug.println("Temporary table not empty ????????????????????????????????????????????????");
		} else
			Debug.println("Temporary table ok !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		} catch( Exception e){
			e.printStackTrace();
		}
*/	}

	@Override
	public ReadSet getReadSet() {
		Debug.println( "READ SET = " + readSet);
		ReadSetEntry[] tmpList = new ReadSetEntry[readSet.size()];
		readSet.toArray(tmpList);
		for(int i = 0; i < tmpList.length; i++){
			ReadSetEntry e = tmpList[i];
			Debug.printf("read set entry %s \n", e.toString());
		}
		if( readSetFinal != null)
			return readSetFinal;
		readSetFinal = new ReadSet( readSet);
		return readSetFinal;
	}

	@Override
	public WriteSet getWriteSet() {
		// TODO: return writeSet;
		Debug.println( "WRITE SET = " + writeSet);
		if( writeSetFinal != null)
			return writeSetFinal;
		writeSetFinal = new WriteSet( writeSet);
		return writeSetFinal;
	}
	
	private Result doExecute( DBSingleOperation dbOp, IDBScratchpad db) throws JSQLParserException, ScratchpadException, SQLException  {
		dbOp.parseSQL(parser);
		String newop = new String(dbOp.getOperation());
		Debug.println("execute op:"+ newop);
		String[][] tableName = dbOp.targetTable();
		if( tableName.length == 1) { 
			ExecutionPolicy policy = config.getPolicy(tableName[0][2]);
			if( policy == null)
				throw new ScratchpadException( "No config for table " + tableName[0][2]);
			if( dbOp.isQuery())
				return policy.executeTemporaryQuery( dbOp, db, tableName[0]);
			else
				return policy.executeTemporaryUpdate( dbOp, db);
		} else {
			if( ! dbOp.isQuery())
				throw new ScratchpadException( "Multi-table operation not expected " + dbOp.sql);
			ExecutionPolicy[] policy = new ExecutionPolicy[tableName.length];
			for( int i = 0; i < tableName.length; i++) {
				policy[i] = config.getPolicy(tableName[i][2]);
				if( tableName[i][1] == null)
					tableName[i][1] = policy[i].getAliasTable();
				if( policy[i] == null)
					throw new ScratchpadException( "No config for table " + tableName[i][0]);
			}
			return policy[0].executeTemporaryQuery( dbOp, db, policy, tableName);
		}
	}


	@Override
	public Result execute(Operation op) throws ScratchpadException {
		try {
			
			DBOperation dbOp0 = null;
			if( op instanceof DBGenericOperation)
				dbOp0 = (DBOperation)op;
			else if( op instanceof DBSingleOperation)
				dbOp0 = (DBOperation)op;
			else if( op instanceof Operation)
				dbOp0 = DBOperation.decode(op);
			else
				throw new RuntimeException( "Expecting DBOperation, but object of class " + op.getClass().getName());

			if( dbOp0 instanceof DBSingleOperation) {
				return doExecute( (DBSingleOperation)dbOp0, this);
			} else if( dbOp0 instanceof DBGenericOperation){
				DBGenericOperation dbOp = (DBGenericOperation)dbOp0;
				
				final PrimaryExecResults results = new PrimaryExecResults();
				final boolean registerIndividual = dbOp.registerIndividualOperations();
				final IDBScratchpad mainDB = this;
				final IDBScratchpad dbs = new IDBScratchpad() {
					@Override
					public boolean isReadOnly() {
						return mainDB.isReadOnly();
					}

					@Override
					public ResultSet executeQuery(String op) throws SQLException {
						return mainDB.executeQuery( op);
					}

					@Override
					public int executeUpdate(String op) throws SQLException {
						return mainDB.executeUpdate(op);
					}

					@Override
					public void addToBatchUpdate(String op) throws SQLException {
						mainDB.addToBatchUpdate(op);
					}

					@Override
					public void executeBatch() throws SQLException {
						mainDB.executeBatch();
					}

					@Override
					public boolean addToWriteSet(DBWriteSetEntry entry) {
//						if( registerIndividual)
							return mainDB.addToWriteSet(entry);
//						else
//							return true;
					}

					@Override
					public boolean addToReadSet(DBReadSetEntry readSetEntry) {
//						if( registerIndividual)
							return mainDB.addToReadSet(readSetEntry);
//						else
//							return true;
					}

					@Override
					public void addToOpLog(DBOpPair op) {
						if( registerIndividual)
							mainDB.addToOpLog(op);
					}

					public int executeOp(String op) throws SQLException {
						// TODO Auto-generated method stub
						return 0;
					}
					
				};
				
				int res = dbOp.execute( new IDatabase() {
					/**
					 * Executes a query in the scratchpad state.
					 */
					public DBSelectResult executeQuery( String sql) throws ScratchpadException {
						try {
							DBSingleOperation o = new DBSingleOperation( sql);
							DBSelectResult res = (DBSelectResult)doExecute( o, dbs);
							results.addResult(res);
							return res;
						} catch( Exception e) {
							throw new ScratchpadException(e); 
						}
					}

					/**
					 * Executes an update in the scratchpad state.
					 */
					public DBUpdateResult executeUpdate( String sql) throws ScratchpadException {
						try {
							DBSingleOperation o = new DBSingleOperation( sql);
							DBUpdateResult res = (DBUpdateResult)doExecute( o, dbs);
							results.addResult(res);
							return res;
						} catch( Exception e) {
							throw new ScratchpadException(e); 
						}
						
					}
					
				});
				if( ! registerIndividual) {
					this.addToOpLog( new DBGenericOpPair( dbOp, results));
				}
				
				return DBUpdateResult.createResult( res);
			} else
				throw new ScratchpadException( "Unexpected operation");
		} catch( Exception e) {
			throw new ScratchpadException( e);
		}
	}

	@Override
	public ReadWriteSet complete() {
		return new ReadWriteSet( getReadSet(), getWriteSet());
	}

	@Override
	public OperationLog getOperationLog() throws ScratchpadException {
		// TODO Auto-generated method stub
		Debug.println( "OP LOG = " + opLog);
		if( opLogFinal != null)
			return opLogFinal;
		opLogFinal = DBOperationLog.createLog( opLog);
		return opLogFinal;
	}

	@Override
	public void abort() throws ScratchpadException {
		try {
			conn.rollback();
		} catch (SQLException e) {
			throw new ScratchpadException( e);
		}
	}

	@Override
	public OperationLog commit(LogicalClock lc, TimeStamp ts) throws ScratchpadException {
		applyOperationLog( opLog, lc, ts);
		return getOperationLog();
	}

	protected void applyOperationLog( List<DBOpPair> opLog, final LogicalClock lc, final TimeStamp ts) throws ScratchpadException {
		for( ;;) {
		try {
			conn.rollback();
			statBU.clearBatch();
			
			final Set<String> deletedTables = new HashSet<String>();
			Iterator<DBOpPair> it = opLog.iterator();
			while( it.hasNext()) {
				DBOpPair op0 = it.next();
				if( op0 instanceof DBSingleOpPair) {
					Debug.println("apply log single Op " + lc);
					DBSingleOpPair op = (DBSingleOpPair)op0;

					op.op.parseSQL(parser);
					String[][] tableName = op.op.targetTable();
					if( tableName.length == 1) {
						ExecutionPolicy policy = config.getPolicy(tableName[0][2]);
						if( policy == null)
							throw new ScratchpadException( "No config for table " + tableName[0][0]);
						if( op.op.isQuery())
							throw new ScratchpadException( "Not expected query in apply operation log for table " + tableName[0][0]);
						else {
							policy.executeDefiniteUpdate( op, this, lc, ts, ! deletedTables.contains(tableName[0][2]));
							deletedTables.add(tableName[0][2]);
						}
					} else 
						throw new ScratchpadException( "Not expected multiple table in applyLog : " + op.op.sql);
				} else if( op0 instanceof DBGenericOpPair) {
					Debug.println("apply log generic Op " + lc);
					DBGenericOpPair op = (DBGenericOpPair)op0;
					DBGenericOperation dbOp = op.op;
					
					statBU.executeBatch();
					final PrimaryExecResults results = new PrimaryExecResults();
					final boolean registerIndividual = dbOp.registerIndividualOperations();
					final IDBScratchpad mainDB = this;
					final IDBScratchpad dbs = this;
					
					dbOp.executeShadow( new IDefDatabase() {

						@Override
						public ResultSet executeQuery(String sql) throws SQLException {
							return dbs.executeQuery(sql);
						}

						@Override
						public int executeUpdate(String sql) throws SQLException, ScratchpadException {
							try {
								DBSingleOperation op = new DBSingleOperation( sql);
								op.parseSQL(parser);
								String[][] tableName = op.targetTable();
								if( tableName.length == 1) {
									ExecutionPolicy policy = config.getPolicy(tableName[0][2]);
									if( policy == null)
										throw new ScratchpadException( "No config for table " + tableName[0][0]);
									if( op.isQuery())
										throw new ScratchpadException( "Not expected query in apply operation log for table " + tableName[0][0]);
									else {
										policy.executeDefiniteUpdate( op, mainDB, lc, ts, ! deletedTables.contains(tableName[0][2]));
									}
								} else 
									throw new ScratchpadException( "Not expected multiple table in applyLog : " + op.sql);
								return 1;
							} catch( Exception e) {
								throw new ScratchpadException(e); 
							}
						}
						
						public int executeOp(String sql){
							// TODO Auto-generated method stub
							return 0;
						}

						public void addCleanUpToBatch(String sql)
								throws SQLException {
							// TODO Auto-generated method stub
							
						}
					}, op.results);
				}
			}
			Debug.println("execute batch");
			int []result = statBU.executeBatch();
			Debug.println( "Batch result = " + result);
			
/*			ResultSet rs = statQ.executeQuery( "SELECT * FROM t1_" + id);
			if( rs.next()) {
				Debug.println( rs.getInt(1));
				Debug.println("Temporary table not empty ????????????????????????????????????????????????");
			} else
				Debug.println("Temporary table ok !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
*/			conn.commit();
			Debug.println("batch commit " + lc);
			break;
		} catch( BatchUpdateException e) {
			Debug.println("an exception happens 1 " + lc);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Debug.println("an exception happens 2 " + lc);
				e1.printStackTrace();
//				throw new ScratchpadException( e1);
			}
//			throw new ScratchpadException( e);
		} catch( Exception e) {
			Debug.println("an exception happens 3 " + lc);
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				Debug.println("an exception happens 4 " + lc);
				e1.printStackTrace();
			}
			throw new ScratchpadException( e);
		}
		}
	}
	
	
	public void applyOperationLog(OperationLog opLog, LogicalClock lc, TimeStamp ts) throws ScratchpadException {
		if( opLog instanceof DBOperationLog) {
			applyOperationLog( ((DBOperationLog)opLog).getLog(), lc, ts);
		} else {
			applyOperationLog( DBOperationLog.createLog(opLog), lc, ts);
		}
	}

	@Override
	public void applyOperationLog(OperationLog opLog) throws ScratchpadException {
		if( opLog instanceof DBOperationLog) {
			opLogFinal = (DBOperationLog)opLog;
		} else {
			opLogFinal = DBOperationLog.createLog(opLog);
		}
		remote = true;
	}

	@Override
	public void finalize(LogicalClock lc, TimeStamp ts) throws ScratchpadException {
		if( remote)
			applyOperationLog( opLogFinal.getLog(), lc, ts);
		else
			applyOperationLog( opLog, lc, ts);
	}

	@Override
	public ResultSet executeQuery(String op) throws SQLException {
		return statQ.executeQuery(op);
	}

	@Override
	public int executeUpdate(String op) throws SQLException {
		return statU.executeUpdate(op);
	}
	@Override
	public void addToBatchUpdate( String op) throws SQLException {
		statBU.addBatch(op);
		batchEmpty = false;
	}
	@Override
	public void executeBatch() throws SQLException {
		if( batchEmpty)
			return;
		statBU.executeBatch();
		statBU.clearBatch();
		batchEmpty = true;
	}

	@Override
	public boolean addToWriteSet(DBWriteSetEntry entry) {
		//Debug.println( "add to write set:" + entry);
		writeSetFinal = null;
		readOnly = false;
		return writeSet.add(entry);
	}

	@Override
	public boolean addToReadSet(DBReadSetEntry entry) {
		//Debug.println( "add to read set:" + entry);
		readSetFinal = null;
		return readSet.add(entry);
	}

	@Override
	public void addToOpLog(DBOpPair op) {
		Debug.println( "add to op log: " + op.toString());
		opLogFinal = null;
		opLog.add(op);
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	public void commitShadowOP(Operation op, LogicalClock lc, TimeStamp ts)
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
