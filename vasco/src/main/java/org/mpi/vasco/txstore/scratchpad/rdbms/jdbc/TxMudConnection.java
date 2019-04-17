package org.mpi.vasco.txstore.scratchpad.rdbms.jdbc;
import org.mpi.vasco.util.debug.Debug;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import javax.sql.rowset.CachedRowSet;

import net.sf.jsqlparser.JSQLParserException;

import com.sun.rowset.CachedRowSetImpl;

import org.mpi.vasco.txstore.proxy.ClosedLoopProxyInterface;
import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBGenericOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSelectResult;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSifterEmptyShd;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSifterShd;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSingleOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBUpdateResult;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.Result;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.ShadowOperationCreator;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.ShadowOperation;
import org.mpi.vasco.sieve.runtimelogic.weakestpreconditionchecker.WeakestPreconditionChecker;
import org.mpi.vasco.util.commonfunc.TimeMeasurement;

public class TxMudConnection
	implements Connection
{
	String url;
	Properties props;
	ClosedLoopProxyInterface proxy;
	boolean inTx;
	ProxyTxnId txId;
	DBShadowOperation op;
	int color;
	boolean internalAborted;//this transaction is already aborted by coordinator
	
	public ShadowOperationCreator shdOpCreator;
	ShadowOperation shdOp = null;
	
	//for debug
	//long startTime = -1;
	
	
	TxMudConnection(String url, Properties props, ClosedLoopProxyInterface proxy) {
		this.url = url;
		this.props = props;
		this.proxy = proxy;
		inTx = false;
		op = null;
		color = -1;
		internalAborted = false;
	}

	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 48");
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 54");
		return null;
	}

	@Override
	public void clearWarnings() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 60");
		
	}

	@Override
	public void close() throws SQLException {
		Debug.println("TxMudConnection method close line 59 // TODO review it");
		//System.out.println("TxMudConnection method close line 59 // TODO review it");
		inTx = false;
		internalAborted = false;
		color = -1;
	}

	@Override
	public void commit() throws SQLException {
		Debug.println("Proxy commit a transaction");
		inTx = false;
		
		try {
			if(shdOp != null && !shdOp.isEmpty()) {
				op = DBSifterShd.createOperation(shdOp);
			}else {
				op = DBSifterEmptyShd.createOperation();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(op == null){
			throw new RuntimeException("op is not set");
		}
		
		//get the opName
		String opName = WeakestPreconditionChecker.getShadowOpName(shdOpCreator, shdOp);
		
		if( ! proxy.commit(txId, op, opName)){
			internalAborted = true;
			throw new SQLException( "commit failed");
		}
		//Debug method
		/*double endToEndLatency = TimeMeasurement.computeLatencyInMS(startTime);
		if(endToEndLatency > 50) {
			System.out.println("You have a transaction spent more than 50 ms, " + endToEndLatency);
			if(shdOp == null ||
					shdOp.isEmpty()) {
				System.out.println("This transaction is readonly");
			}else {
				System.out.println(shdOp.toString());
			}
		}*/
		//Debug method
		if(shdOp != null && !shdOp.isEmpty()) {
			shdOp.clear();
		}
	}
	
	/**
	 * added by chengli for shadow operation commit
	 * @param op
	 * @throws SQLException
	 */
	public void commit(DBShadowOperation op) throws SQLException {
		throw new RuntimeException("Should not come here");
		/*inTx = false;
		if( ! proxy.commit(txId, op)){
			internalAborted = true;
			throw new SQLException( "commit failed");
		}*/
	}
	
	public void setShadowOperation(DBShadowOperation op, int color){
		this.op = op;
		this.color = color;
	}


	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 103");
		return null;
	}

	@Override
	public Blob createBlob() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 109");
		return null;
	}

	@Override
	public Clob createClob() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 115");
		return null;
	}

	@Override
	public NClob createNClob() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 121");
		return null;
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 127");
		return null;
	}

	@Override
	public Statement createStatement() throws SQLException {
		return new TxMudStatement();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 138");
		return null;
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 145");
		return null;
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 151");
		return null;
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 157");
		return false;
	}

	@Override
	public String getCatalog() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 163");
		return null;
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 169");
		return null;
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 175");
		return null;
	}

	@Override
	public int getHoldability() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 181");
		return 0;
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 187");
		return null;
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 193");
		return 0;
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 199");
		return null;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 205");
		return null;
	}

	@Override
	public boolean isClosed() throws SQLException {
	//	System.out.println("TxMudConnection method isClosed line 181 // TODO review it");
		return inTx;
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 217");
		return false;
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 223");
		return false;
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 229");
		return null;
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 235");
		return null;
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 241");
		return null;
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 248");
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		if( ! inTx) {
			txId = proxy.beginTxn();
			inTx = true;
			internalAborted = false;
			//startTime = TimeMeasurement.getCurrentTimeInNS();
		}
		return new TxMudPreparedStatement( sql);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 263");
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 269");
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 275");
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 282");
		return null;
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 289");
		return null;
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 295");
		
	}

	@Override
	public void rollback() throws SQLException {
		System.out.println("rollback called by " + txId);
		inTx = false;
		if(internalAborted == false){
			//if it is not internally aborted, please call it again
			proxy.abort( txId);
		}else{
			//if the commit failes, then this rollback is no-op, since the proxy commit already aborts
			System.out.println("internally aborted " + txId);
		}
		if(shdOp != null && !shdOp.isEmpty()) {
			shdOp.clear();
		}
		throw new RuntimeException();
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 307");
		
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		if( autoCommit == true)
			throw new SQLException( "Autocommit not supported 314");
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 319");
		
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		System.out.println(" // TODO Auto-generated method stub 325");
		
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		System.out.println(" // TODO Auto-generated method stub 331");
		
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 337");
		
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 343");
		
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 349");
		return null;
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 355");
		return null;
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 361");
		
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 367");
		
	}
	
	public int executeUpdate(DBGenericOperation op) throws SQLException {
		if( ! inTx) {
			txId = proxy.beginTxn();
			inTx = true;
			internalAborted = false;
			//startTime = TimeMeasurement.getCurrentTimeInNS();
		}
		DBUpdateResult res = DBUpdateResult.createResult(  proxy.execute( op, txId).getResult());
		return res.getUpdateResult();
	}

	
	class TxMudStatement
	implements Statement
{

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 387");
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 393");
		return null;
	}

	@Override
	public void addBatch(String arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 399");
		
	}

	@Override
	public void cancel() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 405");
		
	}

	@Override
	public void clearBatch() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 411");
		
	}

	@Override
	public void clearWarnings() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 417");
		
	}

	@Override
	public void close() throws SQLException {
		// do nothing
	}

	@Override
	public boolean execute(String arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 428");
		return false;
	}

	@Override
	public boolean execute(String arg0, int arg1) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 434");
		return false;
	}

	@Override
	public boolean execute(String arg0, int[] arg1) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 440");
		return false;
	}

	@Override
	public boolean execute(String arg0, String[] arg1) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 446");
		return false;
	}

	@Override
	public int[] executeBatch() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 452");
		return null;
	}

	@Override
	public ResultSet executeQuery(String arg0) throws SQLException {
		if( ! inTx) {
			txId = proxy.beginTxn();
			inTx = true;
			internalAborted = false;
		}
		/*DBSelectResult res;
		try {
			res = DBSelectResult.createResult( proxy.execute( DBSingleOperation.createOperation( arg0), txId));
			return new TxMudResultSet( res);
		} catch (ScratchpadException e) {
			e.printStackTrace();
			throw new SQLException( e);
		}*/
		
		ResultSet res;
		res = proxy.executeOrig( DBSingleOperation.createOperation( arg0), txId);
		return res;
	}

	@Override
	public int executeUpdate(String arg0) throws SQLException {
		if( ! inTx) {
			txId = proxy.beginTxn();
			inTx = true;
			internalAborted = false;
		}
		//make it deterministic
		String[] updateStatements = null;
		try {
			updateStatements = shdOpCreator.makeToDeterministic(arg0);
		} catch (JSQLParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int result = 0;
		for(String updateStr : updateStatements) {
			DBUpdateResult res = DBUpdateResult.createResult(proxy.execute( DBSingleOperation.createOperation( updateStr), txId).getResult());
			result += res.getUpdateResult();
			if(shdOp == null) {
				shdOp = shdOpCreator.createEmptyShadowOperation();
			}
			try {
				shdOpCreator.addDBEntryToShadowOperation(shdOp, updateStr);
			} catch (JSQLParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}

	@Override
	public int executeUpdate(String arg0, int arg1) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 488");
		return 0;
	}

	@Override
	public int executeUpdate(String arg0, int[] arg1) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 494");
		return 0;
	}

	@Override
	public int executeUpdate(String arg0, String[] arg1) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 500");
		return 0;
	}

	@Override
	public Connection getConnection() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 506");
		return null;
	}

	@Override
	public int getFetchDirection() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 507");
		return 0;
	}

	@Override
	public int getFetchSize() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 508");
		return 0;
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 509");
		return null;
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 510 ");
		return 0;
	}

	@Override
	public int getMaxRows() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 511");
		return 0;
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 512");
		return false;
	}

	@Override
	public boolean getMoreResults(int arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 513");
		return false;
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 514");
		return 0;
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 515");
		return null;
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 516");
		return 0;
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 517");
		return 0;
	}

	@Override
	public int getResultSetType() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 518");
		return 0;
	}

	@Override
	public int getUpdateCount() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 519");
		return 0;
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 520");
		return null;
	}

	@Override
	public boolean isClosed() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 521");
		return false;
	}

	@Override
	public boolean isPoolable() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 522");
		return false;
	}

	@Override
	public void setCursorName(String arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 523");
		
	}

	@Override
	public void setEscapeProcessing(boolean arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 524");
		
	}

	@Override
	public void setFetchDirection(int arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 525");
		
	}

	@Override
	public void setFetchSize(int arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 526");
		
	}

	@Override
	public void setMaxFieldSize(int arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 527");
		
	}

	@Override
	public void setMaxRows(int arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 528");
		
	}

	@Override
	public void setPoolable(boolean arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 529");
		
	}

	@Override
	public void setQueryTimeout(int arg0) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 530");
		
	}

	public void closeOnCompletion() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 531");
	}

	public boolean isCloseOnCompletion() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 532");
		return false;
	}
}

	/******************************************************************************************************************
	 * Encodes results
	 * @author nmp
	 *
	 */
	public class TxMudResultSet implements ResultSet
	{
		DBSelectResult res;
		
		public TxMudResultSet( DBSelectResult res) {
			this.res = res;
		}

		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 600");
			return false;
		}

		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 601 ");
			return null;
		}

		@Override
		public boolean absolute(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 602");
			return false;
		}

		@Override
		public void afterLast() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 603");

		}

		@Override
		public void beforeFirst() throws SQLException {
			Debug.println("Move the cursor of the resultset to the one before first");
			this.res.reset();
		}

		@Override
		public void cancelRowUpdates() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 605");

		}

		@Override
		public void clearWarnings() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 606");

		}

		@Override
		public void close() throws SQLException {
			// do nothing
		}

		@Override
		public void deleteRow() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 607");

		}

		@Override
		public int findColumn(String arg0) throws SQLException {
			if (res.getColumnAliasToNumbersMap()==null) 
				throw new SQLException ("order of the attributes in sql query where not defined");
			return res.getColumnAliasToNumbersMap().get(arg0);
			
		}

		@Override
		public boolean first() throws SQLException {
			try {
				return res.first();
			} catch (ScratchpadException e) {
				throw new SQLException( e);
			}
		}

		@Override
		public Array getArray(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 608");
			return null;
		}

		@Override
		public Array getArray(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 609");
			return null;
		}

		@Override
		public InputStream getAsciiStream(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 610");
			return null;
		}

		@Override
		public InputStream getAsciiStream(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 611");
			return null;
		}

		@Override
		public BigDecimal getBigDecimal(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 612");
			return null;
		}

		@Override
		public BigDecimal getBigDecimal(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 613");
			return null;
		}

		@Override
		public BigDecimal getBigDecimal(int arg0, int arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 614");
			return null;
		}

		@Override
		public BigDecimal getBigDecimal(String arg0, int arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 615");
			return null;
		}

		@Override
		public InputStream getBinaryStream(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 616");
			return null;
		}

		@Override
		public InputStream getBinaryStream(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 617");
			return null;
		}

		@Override
		public Blob getBlob(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 618");
			return null;
		}

		@Override
		public Blob getBlob(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 619");
			return null;
		}

		@Override
		public boolean getBoolean(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 620");
			return false;
		}

		@Override
		public boolean getBoolean(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 621");
			return false;
		}

		@Override
		public byte getByte(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 622");
			return 0;
		}

		@Override
		public byte getByte(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 623");
			return 0;
		}

		@Override
		public byte[] getBytes(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 624");
			return null;
		}

		@Override
		public byte[] getBytes(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 625");
			return null;
		}

		@Override
		public Reader getCharacterStream(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 626");
			return null;
		}

		@Override
		public Reader getCharacterStream(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 627");
			return null;
		}

		@Override
		public Clob getClob(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 628");
			return null;
		}

		@Override
		public Clob getClob(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 630");
			return null;
		}

		@Override
		public int getConcurrency() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 631");
			return 0;
		}

		@Override
		public String getCursorName() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 632");
			return null;
		}

		@Override
		public Date getDate(int arg0) throws SQLException {
			try {
				return new Date(res.getDate(arg0).getTime());
			} catch (ScratchpadException e) {
				throw new SQLException( e);
			}
		}

		@Override
		public Date getDate(String arg0) throws SQLException {
			return getDate(findColumn(arg0));
		}

		@Override
		public Date getDate(int arg0, Calendar arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 633");
			return null;
		}

		@Override
		public Date getDate(String arg0, Calendar arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 634");
			return null;
		}

		@Override
		public double getDouble(int col) throws SQLException {
			try {
				return res.getDouble(col);
			} catch (ScratchpadException e) {
				throw new SQLException( e);
			}
		}

		@Override
		public double getDouble(String arg0) throws SQLException {
			return getDouble(findColumn(arg0));
		}

		@Override
		public int getFetchDirection() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 635");
			return 0;
		}

		@Override
		public int getFetchSize() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 636");
			return 0;
		}

		@Override
		public float getFloat(int arg0) throws SQLException {
			try {
				return res.getFloat(arg0);
			} catch (ScratchpadException e) {
				throw new SQLException( e);
			}
		}

		@Override
		public float getFloat(String arg0) throws SQLException {
			return getFloat(findColumn(arg0));
		}

		@Override
		public int getHoldability() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 637");
			return 0;
		}

		@Override
		public int getInt(int col) throws SQLException {
			try {
				return res.getInt(col);
			} catch (ScratchpadException e) {
				throw new SQLException( e);
			}
		}

		@Override
		public int getInt(String arg0) throws SQLException {
			return getInt(findColumn(arg0));
		}

		@Override
		public long getLong(int arg0) throws SQLException {
			try {
				return res.getLong(arg0);
			} catch (ScratchpadException e) {
				throw new SQLException( e);
			}
		}

		@Override
		public long getLong(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 639");
			
			return 0;
		}

		@Override
		public ResultSetMetaData getMetaData() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 640");
			return null;
		}

		@Override
		public Reader getNCharacterStream(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 641");
			return null;
		}

		@Override
		public Reader getNCharacterStream(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 642");
			return null;
		}

		@Override
		public NClob getNClob(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 643");
			return null;
		}

		@Override
		public NClob getNClob(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 644");
			return null;
		}

		@Override
		public String getNString(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 645");
			return null;
		}

		@Override
		public String getNString(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 646");
			return null;
		}

		@Override
		public Object getObject(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 647");
			return null;
		}

		@Override
		public Object getObject(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 648");
			return null;
		}

		@Override
		public Object getObject(int arg0, Map<String, Class<?>> arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 649");
			return null;
		}

		@Override
		public Object getObject(String arg0, Map<String, Class<?>> arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 650");
			return null;
		}

		@Override
		public Ref getRef(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 651");
			return null;
		}

		@Override
		public Ref getRef(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 652");
			return null;
		}

		@Override
		public int getRow() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 653");
			return 0;
		}

		@Override
		public RowId getRowId(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 654");
			return null;
		}

		@Override
		public RowId getRowId(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 655");
			return null;
		}

		@Override
		public SQLXML getSQLXML(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 656");
			return null;
		}

		@Override
		public SQLXML getSQLXML(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 657");
			return null;
		}

		@Override
		public short getShort(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 658");
			return 0;
		}

		@Override
		public short getShort(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 659");
			return 0;
		}

		@Override
		public Statement getStatement() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 660");
			return null;
		}

		@Override
		public String getString(int col) throws SQLException {
			try {
				return res.getString(col);
			} catch (ScratchpadException e) {
				throw new SQLException( e);
			}
		}

		@Override
		public String getString(String arg0) throws SQLException {
			return getString(findColumn(arg0));
		}

		@Override
		public Time getTime(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 661");
			return null;
		}

		@Override
		public Time getTime(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 662");
			return null;
		}

		@Override
		public Time getTime(int arg0, Calendar arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 663");
			return null;
		}

		@Override
		public Time getTime(String arg0, Calendar arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 664");
			return null;
		}

		@Override
		public Timestamp getTimestamp(int arg0) throws SQLException {
			try {
				return res.getTimeStamp(arg0);
			} catch (ScratchpadException e) {
				throw new SQLException( e);
			}
		}

		@Override
		public Timestamp getTimestamp(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 666");
			return null;
		}

		@Override
		public Timestamp getTimestamp(int arg0, Calendar arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 667");
			return null;
		}

		@Override
		public Timestamp getTimestamp(String arg0, Calendar arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 668");
			return null;
		}

		@Override
		public int getType() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 669");
			return 0;
		}

		@Override
		public URL getURL(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 670");
			return null;
		}

		@Override
		public URL getURL(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 671");
			return null;
		}

		@Override
		public InputStream getUnicodeStream(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 672");
			return null;
		}

		@Override
		public InputStream getUnicodeStream(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 673");
			return null;
		}

		@Override
		public SQLWarning getWarnings() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 674");
			return null;
		}

		@Override
		public void insertRow() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 675");

		}

		@Override
		public boolean isAfterLast() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 676");
			return false;
		}

		@Override
		public boolean isBeforeFirst() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 677");
			return false;
		}

		@Override
		public boolean isClosed() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 678");
			return false;
		}

		@Override
		public boolean isFirst() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 679");
			return false;
		}

		@Override
		public boolean isLast() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 680");
			return false;
		}

		@Override
		public boolean last() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 681");
			return false;
		}

		@Override
		public void moveToCurrentRow() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 681");

		}

		@Override
		public void moveToInsertRow() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 681");

		}

		@Override
		public boolean next() throws SQLException {
			try {
				return res.next();
			} catch (ScratchpadException e) {
				throw new SQLException( e);
			}
		}

		@Override
		public boolean previous() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 681");
			return false;
		}

		@Override
		public void refreshRow() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 681");

		}

		@Override
		public boolean relative(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 681");
			return false;
		}

		@Override
		public boolean rowDeleted() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 681");
			return false;
		}

		@Override
		public boolean rowInserted() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 681");
			return false;
		}

		@Override
		public boolean rowUpdated() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 681");
			return false;
		}

		@Override
		public void setFetchDirection(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 681");

		}

		@Override
		public void setFetchSize(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 681");

		}

		@Override
		public void updateArray(int arg0, Array arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 691");

		}

		@Override
		public void updateArray(String arg0, Array arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 691");

		}

		@Override
		public void updateAsciiStream(int arg0, InputStream arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 691");

		}

		@Override
		public void updateAsciiStream(String arg0, InputStream arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 691");

		}

		@Override
		public void updateAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 691");

		}

		@Override
		public void updateAsciiStream(String arg0, InputStream arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 691");

		}

		@Override
		public void updateAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 691");

		}

		@Override
		public void updateAsciiStream(String arg0, InputStream arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 691");

		}

		@Override
		public void updateBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 691");

		}

		@Override
		public void updateBigDecimal(String arg0, BigDecimal arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 691");

		}

		@Override
		public void updateBinaryStream(int arg0, InputStream arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 701");

		}

		@Override
		public void updateBinaryStream(String arg0, InputStream arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 701");

		}

		@Override
		public void updateBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 701");

		}

		@Override
		public void updateBinaryStream(String arg0, InputStream arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 701");

		}

		@Override
		public void updateBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 701");

		}

		@Override
		public void updateBinaryStream(String arg0, InputStream arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 701");

		}

		@Override
		public void updateBlob(int arg0, Blob arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 701");

		}

		@Override
		public void updateBlob(String arg0, Blob arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 701");

		}

		@Override
		public void updateBlob(int arg0, InputStream arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 701");

		}

		@Override
		public void updateBlob(String arg0, InputStream arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 701");

		}

		@Override
		public void updateBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateBlob(String arg0, InputStream arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateBoolean(int arg0, boolean arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateBoolean(String arg0, boolean arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateByte(int arg0, byte arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateByte(String arg0, byte arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateBytes(int arg0, byte[] arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateBytes(String arg0, byte[] arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateCharacterStream(int arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateCharacterStream(String arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateCharacterStream(String arg0, Reader arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateClob(int arg0, Clob arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateClob(String arg0, Clob arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateClob(int arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateClob(String arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateClob(int arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateClob(String arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 711");

		}

		@Override
		public void updateDate(int arg0, Date arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateDate(String arg0, Date arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateDouble(int arg0, double arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateDouble(String arg0, double arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateFloat(int arg0, float arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateFloat(String arg0, float arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateInt(int arg0, int arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateInt(String arg0, int arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateLong(int arg0, long arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateLong(String arg0, long arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateNCharacterStream(int arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateNCharacterStream(String arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateNCharacterStream(String arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateNClob(int arg0, NClob arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateNClob(String arg0, NClob arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateNClob(int arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateNClob(String arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateNClob(int arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateNClob(String arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 731");

		}

		@Override
		public void updateNString(int arg0, String arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateNString(String arg0, String arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateNull(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateNull(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateObject(int arg0, Object arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateObject(String arg0, Object arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateObject(int arg0, Object arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateObject(String arg0, Object arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateRef(int arg0, Ref arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateRef(String arg0, Ref arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateRow() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateRowId(int arg0, RowId arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateRowId(String arg0, RowId arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateSQLXML(int arg0, SQLXML arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateSQLXML(String arg0, SQLXML arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateShort(int arg0, short arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateShort(String arg0, short arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateString(int arg0, String arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateString(String arg0, String arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateTime(int arg0, Time arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 741");

		}

		@Override
		public void updateTime(String arg0, Time arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");

		}

		@Override
		public void updateTimestamp(int arg0, Timestamp arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");

		}

		@Override
		public void updateTimestamp(String arg0, Timestamp arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");

		}

		@Override
		public boolean wasNull() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return false;
		}
		
		public String toString() {
			return res.toString();
		}

		public <T> T getObject(int columnIndex, Class<T> type)
				throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 752");
			return null;
		}

		public <T> T getObject(String columnLabel, Class<T> type)
				throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 753");
			return null;
		}

	}
	
	/******************************************************************************************************************
	 * Encodes prepared statement
	 * @author nmp
	 *
	 */
	class TxMudPreparedStatement
			implements PreparedStatement
	{
		String sql;
		int[] argPos;
		String[] vals;
		
		TxMudPreparedStatement( String sql) {
			this.sql = sql;
			init( 0, 0);
		}
		
		protected void init( int pos, int count) {
			int npos = sql.indexOf('?', pos);
			if( npos == -1) {
				argPos = new int[count];
				vals = new String[count];
				return;
			}
			init( npos + 1, count+1);
			argPos[count] = npos;
		}
		String generateStatement() {
			StringBuffer buffer = new StringBuffer();
			for( int i = 0; i < vals.length; i++) {
				buffer.append( sql.substring( i == 0 ? 0 : argPos[i-1] + 1, argPos[i]));
				buffer.append( vals[i]);
			}
			buffer.append( sql.substring( vals.length > 0 ? argPos[argPos.length-1] + 1 : 0));
			//Debug.println( "STAT = " + buffer.toString());
			return buffer.toString();
		}
		@Override
		public void addBatch(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			
		}

		@Override
		public void cancel() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			
		}

		@Override
		public void clearBatch() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			
		}

		@Override
		public void clearWarnings() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			
		}

		@Override
		public void close() throws SQLException {
			// do nothing
		}

		@Override
		public boolean execute(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return false;
		}

		@Override
		public boolean execute(String arg0, int arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return false;
		}

		@Override
		public boolean execute(String arg0, int[] arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return false;
		}

		@Override
		public boolean execute(String arg0, String[] arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return false;
		}

		@Override
		public int[] executeBatch() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return null;
		}

		@Override
		public ResultSet executeQuery(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return null;
						
		}

		@Override
		public int executeUpdate(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return 0;
		}

		@Override
		public int executeUpdate(String arg0, int arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return 0;
		}

		@Override
		public int executeUpdate(String arg0, int[] arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return 0;
		}

		@Override
		public int executeUpdate(String arg0, String[] arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return 0;
		}

		@Override
		public Connection getConnection() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return null;
		}

		@Override
		public int getFetchDirection() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 751");
			return 0;
		}

		@Override
		public int getFetchSize() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return 0;
		}

		@Override
		public ResultSet getGeneratedKeys() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return null;
		}

		@Override
		public int getMaxFieldSize() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return 0;
		}

		@Override
		public int getMaxRows() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return 0;
		}

		@Override
		public boolean getMoreResults() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return false;
		}

		@Override
		public boolean getMoreResults(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return false;
		}

		@Override
		public int getQueryTimeout() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return 0;
		}

		@Override
		public ResultSet getResultSet() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return null;
		}

		@Override
		public int getResultSetConcurrency() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return 0;
		}

		@Override
		public int getResultSetHoldability() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return 0;
		}

		@Override
		public int getResultSetType() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return 0;
		}

		@Override
		public int getUpdateCount() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return 0;
		}

		@Override
		public SQLWarning getWarnings() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return null;
		}

		@Override
		public boolean isClosed() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return false;
		}

		@Override
		public boolean isPoolable() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			return false;
		}

		@Override
		public void setCursorName(String arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			
		}

		@Override
		public void setEscapeProcessing(boolean arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			
		}

		@Override
		public void setFetchDirection(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			
		}

		@Override
		public void setFetchSize(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			
		}

		@Override
		public void setMaxFieldSize(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 771");
			
		}

		@Override
		public void setMaxRows(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setPoolable(boolean arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setQueryTimeout(int arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public boolean isWrapperFor(Class<?> arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			return false;
		}

		@Override
		public <T> T unwrap(Class<T> arg0) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			return null;
		}

		@Override
		public void addBatch() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void clearParameters() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public boolean execute() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			return false;
		}

		@Override
//		public ResultSet executeQuery() throws SQLException {
//			String sql;
//			sql = generateStatement();
//			System.err.println("\nINFO: generated statement: "+sql+"\n\n");
//			if( ! inTx) {
//				txId = proxy.beginTxn();
//				inTx = true;
//			}
//			DBSelectResult res;
//			try {
//				Result r = proxy.execute( new DBOperation( sql), txId); //raw format got from the network
//				if(r.getColumnAliasToNumbersMap()==null)
//					System.err.println("problem with the map name to column!!! TxMudConnection line 2103");
//				res = DBSelectResult.createResult(r.getResult(), r.getColumnAliasToNumbersMap());
//				return new TxMudResultSet( res);
//			} catch (ScratchpadException e) {
//				e.printStackTrace();
//				throw new SQLException( e);
//			}
//		}
		public ResultSet executeQuery() throws SQLException {
			Debug.println("use execute query from TxMudPreparedStatement");
			String sql;
			sql = generateStatement();
			Debug.println("\nINFO: generated statement: "+sql+"\n\n");
			if( ! inTx) {
				txId = proxy.beginTxn();
				inTx = true;
				internalAborted = false;
			}
			DBSelectResult res;
			try {
				Result r = proxy.execute( DBSingleOperation.createOperation( sql), txId); //raw format got from the network
				res = DBSelectResult.createResult(r);
				TxMudResultSet txMudRs = new TxMudResultSet( res);
				shdOpCreator.setCachedResultSetForDelta(txMudRs);
				return txMudRs;
			} catch (ScratchpadException e) {
				e.printStackTrace();
				System.out.println(sql);
				throw new SQLException( e);
			}
		}

		@Override
		public int executeUpdate() throws SQLException {
			Debug.println("Using execute update from TxMudPreparedStatement");
			if( ! inTx) {
				txId = proxy.beginTxn();
				inTx = true;
				internalAborted = false;
			}
			
			//make it deterministic
			String[] updateStatements = null;
			try {
				//long makeDeterStartTime = TimeMeasurement.getCurrentTimeInNS();
				updateStatements = shdOpCreator.makeToDeterministic(this.generateStatement());
				/*double makeDeterLatency = TimeMeasurement.computeLatencyInMS(makeDeterStartTime);
				if(makeDeterLatency > 500) {
					System.out.println("This statement takes long to deter " + makeDeterLatency + " ms " + this.sql);
				} */
			} catch (JSQLParserException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int result = 0;
			for(String updateStr : updateStatements) {
				DBUpdateResult res = DBUpdateResult.createResult(proxy.execute( DBSingleOperation.createOperation( updateStr), txId).getResult());
				result += res.getUpdateResult();
				if(shdOp == null) {
					shdOp = shdOpCreator.createEmptyShadowOperation();
				}
				try {
					shdOpCreator.addDBEntryToShadowOperation(shdOp, updateStr);
				} catch (JSQLParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			DBUpdateResult finalRes = DBUpdateResult.createResult( result);
			return finalRes.getUpdateResult();
		}

		@Override
		public ResultSetMetaData getMetaData() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			return null;
		}

		@Override
		public ParameterMetaData getParameterMetaData() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			return null;
		}

		@Override
		public void setArray(int arg0, Array arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setAsciiStream(int arg0, InputStream arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setBigDecimal(int arg0, BigDecimal arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setBinaryStream(int arg0, InputStream arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setBlob(int arg0, Blob arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setBlob(int arg0, InputStream arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 791");
			
		}

		@Override
		public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 811");
			
		}

		@Override
		public void setBoolean(int arg0, boolean arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 812");
			
		}

		@Override
		public void setByte(int arg0, byte arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 813");
			
		}

		@Override
		public void setBytes(int arg0, byte[] arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 814");
			
		}

		@Override
		public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 815");
			
		}

		@Override
		public void setCharacterStream(int arg0, Reader arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 816");
			
		}

		@Override
		public void setCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 817");
			
		}

		@Override
		public void setClob(int arg0, Clob arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 818");
			
		}

		@Override
		public void setClob(int arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 819");
			
		}

		@Override
		public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 820");
			
		}

		@Override
		public void setDate(int pos, Date val) throws SQLException {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			vals[pos-1] = "'" + sdf.format(val) + "'";
		}

		@Override
		public void setDate(int arg0, Date arg1, Calendar arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 821");
			
		}

		@Override
		public void setDouble(int pos, double val) throws SQLException {
			vals[pos-1] = "" + val;
						
		}

		@Override
		public void setFloat(int arg0, float arg1) throws SQLException {
			vals[arg0-1] = "" + arg1;
		}

		@Override
		public void setInt(int pos, int val) throws SQLException {
			vals[pos-1] = "" + val;
		}

		@Override
		public void setLong(int pos, long val) throws SQLException {
			vals[pos-1] = "" + val;
		}

		@Override
		public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 823");
			
		}

		@Override
		public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 824");
			
		}

		@Override
		public void setNClob(int arg0, NClob arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 825");
			
		}

		@Override
		public void setNClob(int arg0, Reader arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 826");
			
		}

		@Override
		public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 827");
			
		}

		@Override
		public void setNString(int arg0, String arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 828");
			
		}

		@Override
		public void setNull(int arg0, int arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 829");
			
		}

		@Override
		public void setNull(int arg0, int arg1, String arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 830");
			
		}

		@Override
		public void setObject(int arg0, Object arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 831");
			
		}

		@Override
		public void setObject(int arg0, Object arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 832");
			
		}

		@Override
		public void setObject(int arg0, Object arg1, int arg2, int arg3) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 833");
			
		}

		@Override
		public void setRef(int arg0, Ref arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 834");
			
		}

		@Override
		public void setRowId(int arg0, RowId arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 835");
			
		}

		@Override
		public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 836");
			
		}

		@Override
		public void setShort(int arg0, short arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 837");
			
		}

		@Override
		public void setString(int pos, String val) throws SQLException {
			vals[pos-1] = "'" + val + "'";
		}

		@Override
		public void setTime(int arg0, Time arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 838");
			
		}

		@Override
		public void setTime(int arg0, Time arg1, Calendar arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 839");
			
		}

		@Override
		public void setTimestamp(int pos, Timestamp val) throws SQLException {
			vals[pos-1] = "'" + val + "'";
		}

		@Override
		public void setTimestamp(int arg0, Timestamp arg1, Calendar arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 840");
		}

		@Override
		public void setURL(int arg0, URL arg1) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 841");
			
		}

		@Override
		public void setUnicodeStream(int arg0, InputStream arg1, int arg2) throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 8311");
			
		}

		public void closeOnCompletion() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 8321");
			
		}

		public boolean isCloseOnCompletion() throws SQLException {
			System.out.println(" // TODO Auto-generated method stub 8331");
			return false;
		}
		
	}

	public void abort(Executor executor) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 8341");
		
	}

	public int getNetworkTimeout() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 8351");
		return 0;
	}

	public String getSchema() throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 8361");
		return null;
	}

	public void setNetworkTimeout(Executor executor, int milliseconds)
			throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 8371");
		
	}

	public void setSchema(String schema) throws SQLException {
		System.out.println(" // TODO Auto-generated method stub 8381");
		
	}

}
