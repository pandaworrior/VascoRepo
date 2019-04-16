/**
 * 
 */
package org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.text.DateFormat;

import org.mpi.vasco.txstore.scratchpad.rdbms.jdbc.TxMudConnection;
import org.mpi.vasco.txstore.scratchpad.rdbms.jdbc.TxMudConnection.TxMudResultSet;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSelectResult;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.DBOpEntry;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.RuntimeFingerPrintGenerator;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.ShadowOperation;

import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.util.IDFactories.IDFactories;
import org.mpi.vasco.util.IDFactories.IDGenerator;
import org.mpi.vasco.util.annotationparser.SchemaParser;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.PrimitiveType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DataField;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseFunction;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.RuntimeExceptionType;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

import org.mpi.vasco.util.crdtlib.dbannotationtypes.AosetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.ArsetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.AusetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.CrdtFactory;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.DatabaseDef;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.UosetTable;

// TODO: Auto-generated Javadoc
/**
 * The Class CrdtTransformer.
 */
public class ShadowOperationCreator {

	/** The annotated table schema. */
	static HashMap<String, DatabaseTable> annotatedTableSchema;
	
	/** The c jsql parser. */
	static CCJSqlParserManager cJsqlParser;
	
	/** The i d factory. */
	static IDFactories iDFactory;
	
	/** The is initialized. */
	static boolean isInitialized = false;
	
	/** The global proxy id. */
	public static int globalProxyId;
	
	/** The total proxy num. */
	public static int totalProxyNum; 
	
	/** The con for fetching subselection data. */
	TxMudConnection con;
	
	/** The fp generator. */
	RuntimeFingerPrintGenerator fpGenerator;
	
	/** The cached result set for delta. */
	private TxMudResultSet cachedResultSetForDelta;
	
	/** The date format instance, it is thread-local since it is not thread-safe. */
	DateFormat dateFormat;
	
	/**
	 * Instantiates a new crdt transformer.
	 *
	 * @param sqlSchema the sql schema
	 * @param propertiesStr the properties str
	 * @param userName the user name
	 * @param password the password
	 * @param gPId the g p id
	 * @param numOfProxies the num of proxies
	 * @param txMudConn the tx mud conn
	 */
	public ShadowOperationCreator(String sqlSchema, String propertiesStr, String userName, String password, 
			int gPId, int numOfProxies, TxMudConnection txMudConn) {
		if(!isInitialized) {
			Connection originalConn = this.createRealConnection(propertiesStr, userName, password);
			SchemaParser sP = new SchemaParser(sqlSchema);
			sP.parseAnnotations();
			HashMap<String, DatabaseTable> hMp = sP.getTableCrdtFormMap();
			sP.printOut();
			annotatedTableSchema = hMp;
			cJsqlParser = new CCJSqlParserManager();
			iDFactory = new IDFactories();
			globalProxyId = gPId;
			totalProxyNum = numOfProxies;
			this.initIDFactories(globalProxyId, totalProxyNum, originalConn);
			isInitialized = true;
			this.closeRealConnection(originalConn);
		}
		//this.closeRealConnection(); should not be called since the connection is used to fetch subselect in an insertion or update sql statement
		this.con = txMudConn;
		this.cachedResultSetForDelta = null;
		fpGenerator = new RuntimeFingerPrintGenerator();
		this.setDateFormat(DatabaseFunction.getNewDateFormatInstance());
	}
	
	/**
	 * Creates the real connection.
	 *
	 * @param propertiesStr the properties str
	 * @param userName the user name
	 * @param password the password
	 * @return the connection
	 */
	public Connection createRealConnection(String propertiesStr, String userName, String password) {
		Debug.println("Create real connection for initialized id factory, propertiesStr: " +propertiesStr + " userName: " + userName + " pass: " + password);
		Connection originalConn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      // Setup the connection with the DB
	      try {
	    	  originalConn = DriverManager
			      .getConnection(propertiesStr, userName, password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Debug.println("Successfully create real connection for initializing id factory");
		return originalConn;
	}
	
	/**
	 * Close real connection.
	 *
	 * @param originalConn the original conn
	 */
	private void closeRealConnection(Connection originalConn){
		Debug.println("We have initialized the id factory, now close the real connection");
		try {
			originalConn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// check all tables, create ID Generator for all primarykey but not foreign
	// key
	/**
	 * Inits the id factories.
	 *
	 * @param gPI the g pi
	 * @param pId the id
	 * @param conn the conn
	 */
	public void initIDFactories(int gPI, int pId, Connection conn) {
		Debug.println("We initialize the ID factories!");
		IDGenerator.initialized(gPI, pId, conn);
		Iterator<Map.Entry<String, DatabaseTable>> it = annotatedTableSchema
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, DatabaseTable> entry = (Map.Entry<String, DatabaseTable>) it
					.next();
			DatabaseTable dT = entry.getValue();
			String tableName = dT.get_Table_Name();
			Debug.println("We initialize the ID generator for " + tableName);
			HashMap<String, DataField> pkMap = dT.get_Primary_Key_List();
			Iterator<Map.Entry<String, DataField>> pkIt = pkMap.entrySet()
					.iterator();
			while (pkIt.hasNext()) {
				Map.Entry<String, DataField> pkField = (Map.Entry<String, DataField>) pkIt
						.next();
				DataField pkDF = pkField.getValue();
				if (pkDF.is_AutoIncrement()
						&& pkDF.get_Data_Type().toUpperCase().contains("INT")) {
					Debug.println("We initialize the ID generator for "
							+ tableName + " key " + pkDF.get_Data_Field_Name());
					iDFactory.add_ID_Generator(tableName,
							pkDF.get_Data_Field_Name());
				}
			}
		}
	}
	
	/**
	 * Creates the empty shadow operation.
	 *
	 * @return the shadow operation
	 */
	public ShadowOperation createEmptyShadowOperation() {
		return new ShadowOperation();
	}
	
	/**
	 * Reset cached result set.
	 */
	public void resetCachedResultSet() {
		this.cachedResultSetForDelta = null;
	}

	/*
	 * The following two functions are used to fill in missing fields in an
	 * insertion.
	 */

	/**
	 * Find missing data fields.
	 *
	 * @param tableName the table name
	 * @param colList the col list
	 * @param valueList the value list
	 * @return the sets the
	 */
	public Set<String> findMissingDataFields(String tableName,
			List<String> colList, List<String> valueList) {
		DatabaseTable dtB = annotatedTableSchema.get(tableName);
		return dtB.findMisingDataField(colList, valueList);
	}

	/**
	 * Fill in missing value.
	 *
	 * @param tableName the table name
	 * @param colList the col list
	 * @param valueList the value list
	 */
	public void fillInMissingValue(String tableName, List<String> colList,
			List<String> valueList) {

		Set<String> missFields = findMissingDataFields(tableName, colList,
				valueList);

		DatabaseTable dbT = annotatedTableSchema.get(tableName);
		for (int i = 0; i < valueList.size(); i++) {
			DataField dF = null;
			if (colList != null && colList.size() > 0) {
				dF = dbT.get_Data_Field(colList.get(i));
			} else {
				dF = dbT.get_Data_Field(i);
			}
			String expStr = valueList.get(i).toString().trim();
			if (expStr.equalsIgnoreCase("NOW()")
					|| expStr.equalsIgnoreCase("NOW")
					|| expStr.equalsIgnoreCase("CURRENT_TIMESTAMP")
					|| expStr.equalsIgnoreCase("CURRENT_TIMESTAMP()")
					|| expStr.equalsIgnoreCase("CURRENT_DATE")) {
				valueList.set(i, "'" + DatabaseFunction.CURRENTTIMESTAMP(this.getDateFormat()) + "'");
			}
		}

		// fill in the missing tuples
		if (missFields != null) {
			for (String missingDfName : missFields) {
				colList.add(missingDfName);
				DataField dF = dbT.get_Data_Field(missingDfName);
				if (dF.is_Primary_Key()) {
					if (dF.is_Foreign_Key()) {
						try {
							throw new RuntimeException(
									"Foreign primary key must be specified "
											+ missingDfName + "!");
						} catch (RuntimeException e) {
							e.printStackTrace();
							System.exit(RuntimeExceptionType.FOREIGNPRIMARYKEYMISSING);
						}
					} else {
						/*valueList.add(Integer.toString(iDFactory.getNextId(
								tableName, dF.get_Data_Field_Name())));*/
						throw new RuntimeException("The primary keys' values should not be missing");
					}
				} else {
					if (dF.get_Default_Value() == null) {
						valueList.add(CrdtFactory.getDefaultValueForDataField(this.getDateFormat(), dF));
					}else {
						if (dF.get_Default_Value().equalsIgnoreCase(
								"CURRENT_TIMESTAMP")) {
							valueList.add("'" + DatabaseFunction.CURRENTTIMESTAMP(this.getDateFormat()) + "'");
						} else {
							valueList.add(dF.get_Default_Value());
						}
					}
				}
			}
		}
	}

	/*
	 * The following function is used to replace NOW or CURRENT_TIMESTAMP
	 * functions in an update
	 */
	/**
	 * Replace value for database functions.
	 *
	 * @param tableName the table name
	 * @param valueList the value list
	 */
	public void replaceValueForDatabaseFunctions(String tableName,
			List<String> valueList) {
		for (int i = 0; i < valueList.size(); i++) {
			String valStr = valueList.get(i).trim();
			if (valStr.equalsIgnoreCase("NOW()")
					|| valStr.equalsIgnoreCase("NOW")
					|| valStr.equalsIgnoreCase("CURRENT_TIMESTAMP")
					|| valStr.equalsIgnoreCase("CURRENT_TIMESTAMP()")
					|| valStr.equalsIgnoreCase("CURRENT_DATE")) {
				valueList.set(i, "'" + DatabaseFunction.CURRENTTIMESTAMP(this.getDateFormat()) + "'");
			}
		}
	}
	
	/**
	 * Checks if is primary key missing from where clause.
	 *
	 * @param tableName the table name
	 * @param whereClause the where clause
	 * @return true, if is primary key missing from where clause
	 */
	private boolean isPrimaryKeyMissingFromWhereClause(String tableName,
			Expression whereClause) {
		DatabaseTable dbT = annotatedTableSchema.get(tableName);
		return dbT.isPrimaryKeyMissingFromWhereClause(whereClause.toString());
	}

	/*
	 * This function is to check whether the delete and update are specified by
	 * a or a group of primary keys. If not, it will generate a query to fetch
	 * the list of records matching the condition If the return value is not
	 * empty, then please execute your return string to fetch primary keys;
	 * Otherwise please ignore the function
	 */
	/**
	 * Gets the primary key selection query.
	 *
	 * @param tableName the table name
	 * @param whereClause the where clause
	 * @return the primary key selection query
	 */
	public String getPrimaryKeySelectionQuery(String tableName,
			Expression whereClause) {
		DatabaseTable dbT = annotatedTableSchema.get(tableName);
		return dbT.generatedPrimaryKeyQuery(whereClause.toString());
	}
	
	// intercept the executeUpdate,make it to deterministic
	/**
	 * Make to deterministic.
	 *
	 * @param sqlQuery the sql query
	 * @return the string[]
	 * @throws JSQLParserException the jSQL parser exception
	 */
	public String[] makeToDeterministic(String sqlQuery) throws JSQLParserException {
		
		Debug.println("Try to make the queries deterministic");
		
		String[] deterQueries = null;
		
		// contains current_time_stamp
		// contains NOW(), use the same
		// contains select, do the select first
		// contains delete from where (not specify by full primary key)
		// fill in default value and IDs for insert
		net.sf.jsqlparser.statement.Statement sqlStmt = cJsqlParser.parse(new StringReader(sqlQuery));
		if (sqlStmt instanceof Insert) {
			Insert insertStmt = (Insert) sqlStmt;
			String tableName = insertStmt.getTable().getName();
			List<String> colList = new ArrayList<String>();
			List<String> valList = new ArrayList<String>();
			Iterator colIt = insertStmt.getColumns().iterator();
			while(colIt.hasNext()){
				colList.add(colIt.next().toString());
			}
			//replace selection with their results
			replaceSelectionForInsert(insertStmt, valList);
			//call function to replace and fill in the missing fields
			fillInMissingValue(tableName, colList, valList);
			deterQueries = new String[1];
			deterQueries[0] = assembleInsert(tableName, colList, valList);
		}else if(sqlStmt instanceof Update){
			Update updateStmt = (Update) sqlStmt;
			List<String> colList = new ArrayList<String>();
			List<String> valList = new ArrayList<String>();
			Iterator colIt = updateStmt.getColumns().iterator();
			while(colIt.hasNext()){
				colList.add(colIt.next().toString());
			}
			Iterator valueIt = updateStmt.getExpressions().iterator();
			while(valueIt.hasNext()){
				valList.add(valueIt.next().toString());
			}
			//replace values for selection in the itemlist
			replaceSelectionForUpdate(updateStmt,valList);
			//replace database functions like now or current time stamp
			replaceValueForDatabaseFunctions(updateStmt.getTable().getName(), valList);
			//where clause figure out whether this is specify by primary key or not, if yes, go ahead,
			//it not, please first query 
			deterQueries = fillInMissingPrimaryKeysForUpdate(updateStmt, colList, valList);
			
		}else if(sqlStmt instanceof Delete){
			Delete deleteStmt = (Delete) sqlStmt;
			//where clause figure out whether this is specify by primary key or not, if yes, go ahead,
			//it not, please first query 
			deterQueries = fillInMissingPrimaryKeysForDelete(deleteStmt);
		}
		
		return deterQueries;
	}
	
	/*
	 * These two following functions are to fetch primary key sets for an update/delete which doesn't 
	 * specify the full primary keys in its where clause.
	 * Example: update t1 set a = f where condition, condition doesn't contain all
	 * primary keys. The problem with this update is that it will introduce different
	 * changes to system if they apply against different state.
	 */
	/**
	 * Fill in missing primary keys for update.
	 *
	 * @param updateStmt the update stmt
	 * @param colList the col list
	 * @param valList the val list
	 * @return the string[]
	 */
	public String[] fillInMissingPrimaryKeysForUpdate(Update updateStmt, List<String> colList, List<String> valList){
		String[] newUpdates = null;
		
		if(this.isPrimaryKeyMissingFromWhereClause(updateStmt.getTable().getName(), 
				updateStmt.getWhere())){
			String primaryKeySelectStr = getPrimaryKeySelectionQuery(updateStmt.getTable().getName(), updateStmt.getWhere());
			//execute the primaryKeySelectStr
			try {
				PreparedStatement sPst = con.prepareStatement(primaryKeySelectStr);
				ResultSet rs = sPst.executeQuery();
				newUpdates = assembleUpdates(updateStmt.getTable().getName(), colList, valList, rs);
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Debug.println("Selection is wrong");
			}
		}else{
			Debug.println("No primary key missing, then return the original update query");
			newUpdates = new String[1];
			newUpdates[0] = assembleUpdate(updateStmt.getTable().getName(), colList, valList, updateStmt.getWhere().toString());
		}
		return newUpdates;
	}
	
	/**
	 * Fill in missing primary keys for delete.
	 *
	 * @param delStmt the del stmt
	 * @return the string[]
	 */
	public String[] fillInMissingPrimaryKeysForDelete(Delete delStmt){
		String[] newDeletes = null;
		if(this.isPrimaryKeyMissingFromWhereClause(delStmt.getTable().getName(), 
				delStmt.getWhere())){
			String primaryKeySelectStr = this.getPrimaryKeySelectionQuery(delStmt.getTable().getName(), 
					delStmt.getWhere());
			//execute the primaryKeySelectStr
			try {
				PreparedStatement sPst = con.prepareStatement(primaryKeySelectStr);
				ResultSet rs = sPst.executeQuery();
				newDeletes = assembleDeletes(delStmt.getTable().getName(), rs);
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Debug.println("Selection is wrong");
			}
		}else{
			Debug.println("No primary key missing, then return the original delete query");
			newDeletes = new String[1];
			newDeletes[0] = delStmt.toString();
		}
		return newDeletes;
	}
	
	/*
	 * The following two functions are used to replace the values for selection, encode their results
	 */
	
	/**
	 * Replace selection for insert.
	 *
	 * @param insertStmt the insert stmt
	 * @param valList the val list
	 * @throws JSQLParserException the jSQL parser exception
	 */
	public void replaceSelectionForInsert(Insert insertStmt, List<String> valList) throws JSQLParserException{
		Iterator valueIt = ((ExpressionList)insertStmt.getItemsList()).getExpressions().iterator();
		while(valueIt.hasNext()){
			String valStr = valueIt.next().toString().trim();
			if(valStr.contains("SELECT") || valStr.contains("select")){
				//execute the selection 
				//remove two brackets
				if(valStr.indexOf("(") == 0 && valStr.lastIndexOf(")") == valStr.length()-1){
					valStr = valStr.substring(1, valStr.length()-1);
				}
				PlainSelect plainSelect = ((PlainSelect)((Select)cJsqlParser.parse(new StringReader(valStr))).getSelectBody());
				int selectItemCount = plainSelect.getSelectItems().size();
				PreparedStatement sPst;
				try {
					sPst = con.prepareStatement(valStr);
					ResultSet rs = sPst.executeQuery();
					if(rs.next()){
						for(int i = 0 ; i < selectItemCount; i++){
							Debug.println("we got something from the subselection : " + rs.getString(i+1));
							valList.add(rs.getString(i+1));
						}
					}else{
						throw new RuntimeException("Select must return a value!");
					}
					rs.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	
			}else{
				valList.add(valStr);
			}
		}
	}
	
	/**
	 * Replace selection for update.
	 *
	 * @param upStmt the up stmt
	 * @param valList the val list
	 * @throws JSQLParserException the jSQL parser exception
	 */
	public void replaceSelectionForUpdate(Update upStmt, List<String> valList) throws JSQLParserException{
		Iterator valueIt = upStmt.getExpressions().iterator();
		int colIndex = 0;
		while(valueIt.hasNext()){
			String valStr = valueIt.next().toString().trim();
			if(valStr.contains("SELECT") || valStr.contains("select")){
				//execute the selection 
				//remove two brackets
				if(valStr.indexOf("(") == 0 && valStr.lastIndexOf(")") == valStr.length()-1){
					valStr = valStr.substring(1, valStr.length()-1);
				}
				PlainSelect plainSelect = ((PlainSelect)((Select)cJsqlParser.parse(new StringReader(valStr))).getSelectBody());
				assert (plainSelect.getSelectItems().size() == 1);
				try {
					PreparedStatement sPst = con.prepareStatement(valStr);
					ResultSet rs = sPst.executeQuery();
					if(rs.next()){
						valList.set(colIndex,rs.getObject(1).toString());
					}else{
						throw new RuntimeException("Select must return a value!");
					}
					rs.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Debug.println("Selection is wrong");
				}
				
			}
			colIndex++;
		}
	}

	/*
	 * This function is to assemble an insertion request
	 */
	/**
	 * Assemble insert.
	 *
	 * @param tableName the table name
	 * @param colList the col list
	 * @param valList the val list
	 * @return the string
	 */
	public String assembleInsert(String tableName, List<String> colList,
			List<String> valList) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("insert into ");
		buffer.append(tableName + " ");
		if (colList.size() > 0) {
			buffer.append("(");
			for (int i = 0; i < colList.size(); i++) {
				buffer.append(colList.get(i) + ",");
			}
			buffer.setCharAt(buffer.length() - 1, ')');
		}

		buffer.append(" values (");
		if (colList.size() > 0) {
			for (int i = 0; i < valList.size(); i++) {
				buffer.append(this.get_Value_In_Correct_Format(tableName,
						colList.get(i), valList.get(i)) + ",");
			}
		} else {
			for (int i = 0; i < valList.size(); i++) {
				buffer.append(this.get_Value_In_Correct_Format(tableName, i,
						valList.get(i)) + ",");
			}
		}
		buffer.replace(buffer.length() - 1, buffer.length() + 1, ");");
		Debug.println("Newly generated insertion is " + buffer.toString());
		return buffer.toString();
	}

	/*
	 * This function is used to assemble an update with all information
	 */
	/**
	 * Assemble update.
	 *
	 * @param tableName the table name
	 * @param colList the col list
	 * @param valList the val list
	 * @param whereClauseStr the where clause str
	 * @return the string
	 */
	public String assembleUpdate(String tableName, List<String> colList,
			List<String> valList, String whereClauseStr) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("update ");
		buffer.append(tableName + " set ");
		for (int i = 0; i < colList.size(); i++) {
			buffer.append(colList.get(i) + " = ");
			buffer.append(this.get_Value_In_Correct_Format(tableName,
					colList.get(i), valList.get(i))
					+ ",");
		}
		buffer.deleteCharAt(buffer.lastIndexOf(","));
		buffer.append(" where " + whereClauseStr + ";"); // with the ";"
		Debug.println("Newly generated update is " + buffer.toString());
		return buffer.toString();
	}

	/*
	 * This function is used to assemble a list of updates
	 */
	/**
	 * Assemble updates.
	 *
	 * @param tableName the table name
	 * @param colList the col list
	 * @param valList the val list
	 * @param rs the rs
	 * @return the string[]
	 */
	public String[] assembleUpdates(String tableName, List<String> colList,
			List<String> valList, ResultSet rs) {
		StringBuilder buffer = new StringBuilder();
		String updateMainBody = "";
		buffer.append("update ");
		buffer.append(tableName);
		buffer.append(" set ");
		for (int i = 0; i < colList.size(); i++) {
			buffer.append(colList.get(i) + " = ");
			buffer.append(valList.get(i) + ",");
		}
		buffer.deleteCharAt(buffer.length() - 1);
		buffer.append(" where "); // without the ";"
		updateMainBody = buffer.toString();
		Debug.println("Newly generated update main body is " + updateMainBody);

		DatabaseTable dbT = annotatedTableSchema.get(tableName);
		Set<String> pkSet = dbT.get_Primary_Key_Name_List();
		List<String> updateStrList = new ArrayList<String>();
		try {
			while (rs.next()) {
				StringBuilder singleUpdateStr = new StringBuilder(updateMainBody);
				int pkResultIndex = 0;
				for (String pk : pkSet) {
					if(pkResultIndex == 0) {
						singleUpdateStr.append(pk);
						singleUpdateStr.append(" = ");
						singleUpdateStr.append(rs.getString(pk));
					}else {
						singleUpdateStr.append(" AND ");
						singleUpdateStr.append(pk);
						singleUpdateStr.append(" = ");
						singleUpdateStr.append(rs.getString(pk));
					}
					pkResultIndex++;
				}
				Debug.println("The newly generated update query "
						+ singleUpdateStr.toString());
				updateStrList.add(singleUpdateStr.toString());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.println("The result should not be empty!");
		}
		return  updateStrList.toArray(new String[updateStrList.size()]);
	}

	/*
	 * This function is to assemble a delete query
	 */
	/**
	 * Assemble delete.
	 *
	 * @param tableName the table name
	 * @param whereClauseStr the where clause str
	 * @return the string
	 */
	public String assembleDelete(String tableName, String whereClauseStr) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("delete from ");
		buffer.append(tableName + " where ");
		buffer.append(" " + whereClauseStr + ";"); // with the ";"
		Debug.println("Newly generated delete is " + buffer.toString());
		return buffer.toString();
	}

	/*
	 * This function is to assemble a set of delete queries
	 */
	/**
	 * Assemble deletes.
	 *
	 * @param tableName the table name
	 * @param rs the rs
	 * @return the string[]
	 */
	public String[] assembleDeletes(String tableName, 
			ResultSet rs) {
		StringBuilder buffer = new StringBuilder();
		String deleteMainBody = "";
		buffer.append("delete from ");
		buffer.append(tableName);
		buffer.append(" where ");
		deleteMainBody = buffer.toString();
		Debug.println("Newly generated delete mainbody is " + deleteMainBody);

		DatabaseTable dbT = annotatedTableSchema.get(tableName);
		Set<String> pkSet = dbT.get_Primary_Key_Name_List();
		List<String> deleteStrList = new ArrayList<String>();
		try {
			while (rs.next()) {
				StringBuilder singleDeleteStrBuilder = new StringBuilder(deleteMainBody);
				int pkStrIndex = 0;
				for (String pk : pkSet) {
					if(pkStrIndex == 0) {
						singleDeleteStrBuilder.append(pk);
						singleDeleteStrBuilder.append(" = ");
						singleDeleteStrBuilder.append(rs.getString(pk));
					}else {
						singleDeleteStrBuilder.append(" AND ");
						singleDeleteStrBuilder.append(pk);
						singleDeleteStrBuilder.append(" = ");
						singleDeleteStrBuilder.append(rs.getString(pk));
					}
					pkStrIndex++;
				}
				Debug.println("The newly generated delete query "
						+ singleDeleteStrBuilder.toString());
				deleteStrList.add(singleDeleteStrBuilder.toString());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Debug.println("The result should not be empty!");
		}
		return deleteStrList.toArray(new String[deleteStrList.size()]);
	}

	/*
	 * Return a value within the correct format
	 */
	/**
	 * Gets the _ value_ in_ correct_ format.
	 *
	 * @param tableName the table name
	 * @param fieldIndex the field index
	 * @param Value the value
	 * @return the _ value_ in_ correct_ format
	 */
	public String get_Value_In_Correct_Format(String tableName, int fieldIndex,
			String Value) {
		DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DataField dF = dbT.get_Data_Field(fieldIndex);
		return dF.get_Value_In_Correct_Format(Value);
	}

	/*
	 * Return a value within the correct format
	 */
	/**
	 * Gets the _ value_ in_ correct_ format.
	 *
	 * @param tableName the table name
	 * @param dataFileName the data file name
	 * @param Value the value
	 * @return the _ value_ in_ correct_ format
	 */
	public String get_Value_In_Correct_Format(String tableName,
			String dataFileName, String Value) {
		DatabaseTable dbT = annotatedTableSchema.get(tableName);
		DataField dF = dbT.get_Data_Field(dataFileName);
		return dF.get_Value_In_Correct_Format(Value);
	}
	
	/**
	 * Gets the database instance.
	 *
	 * @param tableName the table name
	 * @return the database instance
	 */
	public DatabaseTable getDatabaseInstance(String tableName) {
		DatabaseTable dTb = annotatedTableSchema.get(tableName);

		if (dTb == null) {
			try {
				throw new RuntimeException(
						"This table doesn't appear in the annotation list"
								+ tableName);
			} catch (RuntimeException e) {
				e.printStackTrace();
				System.exit(RuntimeExceptionType.UNKNOWTABLENAME);
			}
		}
		return dTb;
	}
	
	/**
	 * Adds the db entry to shadow operation.
	 *
	 * @param shdOp the shd op
	 * @param sqlQuery the sql query
	 * @throws JSQLParserException the jSQL parser exception
	 * @throws SQLException the sQL exception
	 */
	public void addDBEntryToShadowOperation(ShadowOperation shdOp, 
			String sqlQuery) throws JSQLParserException, SQLException {
		// remove the last ";"

		sqlQuery = sqlQuery.trim();
		int endIndex = sqlQuery.lastIndexOf(";");
		if (endIndex == sqlQuery.length() - 1) {
			sqlQuery = sqlQuery.substring(0, endIndex);
		}
		net.sf.jsqlparser.statement.Statement sqlStmt = cJsqlParser
				.parse(new StringReader(sqlQuery));
		if (sqlStmt instanceof Insert) {
			Insert insertStatement = (Insert) sqlStmt;
			String tableName = insertStatement.getTable().getName();
			DatabaseTable dTb = this.getDatabaseInstance(tableName);

			if (dTb instanceof AosetTable ||
					dTb instanceof AusetTable) {
				shdOp.addOperation(this.createUniqueInsertDBOpEntry(dTb, insertStatement));
			}else if (dTb instanceof ArsetTable) {
				shdOp.addOperation(this.createInsertDBOpEntry(dTb, insertStatement));
			}else {
				try {
					throw new RuntimeException(
							"The type of CRDT table "
									+ dTb.get_CRDT_Table_Type()
									+ "is not supported by our framework or cannot be modified!");
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.NOTDEFINEDCRDTTABLE);
				}
			}
		} else if (sqlStmt instanceof Update) {
			Update updateStatement = (Update) sqlStmt;
			String tableName = updateStatement.getTable().getName();
			DatabaseTable dTb = this.getDatabaseInstance(tableName);

			if (dTb instanceof ArsetTable || 
					dTb instanceof AusetTable ||
					dTb instanceof UosetTable){
				shdOp.addOperation(this.createUpdateDBOpEntry(dTb, updateStatement, this.getCachedResultSetForDelta()));
			}else {
				try {
					throw new RuntimeException(
							"The type of CRDT table "
									+ dTb.get_CRDT_Table_Type()
									+ "is not supported by our framework or cannot be modified!");
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.NOTDEFINEDCRDTTABLE);
				}
			}

		} else if (sqlStmt instanceof Delete) {
			Delete deleteStatement = (Delete) sqlStmt;
			String tableName = deleteStatement.getTable().getName();
			DatabaseTable dTb = this.getDatabaseInstance(tableName);

			if (dTb instanceof ArsetTable) {
				shdOp.addOperation(this.createDeleteDBOpEntry(dTb, deleteStatement));
			}else {
				try {
					throw new RuntimeException(
							"The type of CRDT table "
									+ dTb.get_CRDT_Table_Type()
									+ "is not supported by our framework or cannot be modified!");
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.NOTDEFINEDCRDTTABLE);
				}
			}
		} else {
			try {
				throw new RuntimeException("Could not identify the sql type "
						+ sqlQuery);
			} catch (RuntimeException e) {
				e.printStackTrace();
				System.exit(RuntimeExceptionType.UNKNOWSQLQUERY);
			}
		}
	}
	
	/**
	 * Creates the insert db op entry.
	 *
	 * @param dbT the db t
	 * @param insertStatement the insert statement
	 * @return the dB op entry
	 * @throws SQLException the sQL exception
	 */
	public DBOpEntry createInsertDBOpEntry(DatabaseTable dbT, Insert insertStatement) throws SQLException {
		DBOpEntry dbOpEntry = new DBOpEntry(DatabaseDef.INSERT, dbT.get_Table_Name());
		Iterator colIt = insertStatement.getColumns().iterator();
		Iterator valueIt = ((ExpressionList)insertStatement.getItemsList()).getExpressions().iterator();
		if(colIt == null || colIt.hasNext() == false) {
			//added in the sorted manner
			int index = 0;
			while(valueIt.hasNext()) {
				String value = valueIt.next().toString();
				DataField df = dbT.get_Data_Field(index);
				PrimitiveType pt = CrdtFactory.generateCrdtPrimitiveType(this.getDateFormat(), df, value, null);
				if(df.is_Primary_Key()){
					dbOpEntry.addPrimaryKey(pt);
				}else{
					dbOpEntry.addNormalAttribute(pt);
				}
				index++;
			}
		}else {
			while(colIt.hasNext() && valueIt.hasNext()) {
				String colName = colIt.next().toString();
				String value = valueIt.next().toString();
				DataField df = dbT.get_Data_Field(colName);
				PrimitiveType pt = CrdtFactory.generateCrdtPrimitiveType(this.getDateFormat(), df, value, null);
				if(df.is_Primary_Key()){
					dbOpEntry.addPrimaryKey(pt);
				}else{
					dbOpEntry.addNormalAttribute(pt);
				}
			}
		}
		return dbOpEntry;
	}
	
	/**
	 * Creates the unique insert db op entry.
	 *
	 * @param dbT the db t
	 * @param insertStatement the insert statement
	 * @return the dB op entry
	 * @throws SQLException the sQL exception
	 */
	public DBOpEntry createUniqueInsertDBOpEntry(DatabaseTable dbT, Insert insertStatement) throws SQLException {
		DBOpEntry dbOpEntry = new DBOpEntry(DatabaseDef.UNIQUEINSERT, dbT.get_Table_Name());
		Iterator colIt = insertStatement.getColumns().iterator();
		Iterator valueIt = ((ExpressionList)insertStatement.getItemsList()).getExpressions().iterator();
		if(colIt == null || colIt.hasNext() == false) {
			//added in the sorted manner
			int index = 0;
			while(valueIt.hasNext()) {
				String value = valueIt.next().toString();
				DataField df = dbT.get_Data_Field(index);
				PrimitiveType pt = CrdtFactory.generateCrdtPrimitiveType(this.getDateFormat(), df, value, null);
				if(df.is_Primary_Key()) {
					dbOpEntry.addPrimaryKey(pt);
				}else {
					dbOpEntry.addNormalAttribute(pt);
				}
				index++;
			}
		}else {
			while(colIt.hasNext() && valueIt.hasNext()) {
				String colName = colIt.next().toString();
				String value = valueIt.next().toString();
				DataField df = dbT.get_Data_Field(colName);
				PrimitiveType pt = CrdtFactory.generateCrdtPrimitiveType(this.getDateFormat(), df, value, null);
				if(df.is_Primary_Key()) {
					dbOpEntry.addPrimaryKey(pt);
				}else {
					dbOpEntry.addNormalAttribute(pt);
				}
			}
		}
		return dbOpEntry;
	}
	
	/**
	 * Creates the update db op entry.
	 *
	 * @param dbT the db t
	 * @param updateStatement the update statement
	 * @param rs the rs
	 * @return the dB op entry
	 * @throws SQLException the sQL exception
	 */
	public DBOpEntry createUpdateDBOpEntry(DatabaseTable dbT, Update updateStatement,
			ResultSet rs) throws SQLException {
		DBOpEntry dbOpEntry = new DBOpEntry(DatabaseDef.UPDATE, dbT.get_Table_Name());
		Iterator colIt = updateStatement.getColumns().iterator();
		Iterator valueIt = updateStatement.getExpressions().iterator();
		while(colIt.hasNext() && valueIt.hasNext()) {
			String colName = colIt.next().toString();
			String value = valueIt.next().toString();
			DataField df = dbT.get_Data_Field(colName);
			PrimitiveType pt = CrdtFactory.generateCrdtPrimitiveType(this.getDateFormat(), df, value, rs);
			dbOpEntry.addNormalAttribute(pt);
		}
		String whereClause = updateStatement.getWhere().toString();
		this.addFieldAndValueInWhereClauseToDBOpEntry(dbT, whereClause, dbOpEntry);
		return dbOpEntry;
	}
	
	/**
	 * Creates the delete db op entry.
	 *
	 * @param dbT the db t
	 * @param deleteStatement the delete statement
	 * @return the dB op entry
	 * @throws SQLException the sQL exception
	 */
	public DBOpEntry createDeleteDBOpEntry(DatabaseTable dbT,Delete deleteStatement) throws SQLException {
		DBOpEntry dbOpEntry = new DBOpEntry(DatabaseDef.DELETE, dbT.get_Table_Name());
		String whereClause = deleteStatement.getWhere().toString();
		this.addFieldAndValueInWhereClauseToDBOpEntry(dbT, whereClause, dbOpEntry);
		return dbOpEntry;
	}
	
	/**
	 * Adds the field and value in where clause to db op entry.
	 *
	 * @param dbT the db t
	 * @param whereClause the where clause
	 * @param dbOpEntry the db op entry
	 * @throws SQLException the sQL exception
	 */
	public void addFieldAndValueInWhereClauseToDBOpEntry(DatabaseTable dbT,
			String whereClause, DBOpEntry dbOpEntry) throws SQLException {
		//add all primary key to the entry
		String[] primaryKeyPairs = whereClause.split("AND");
		assert(primaryKeyPairs.length == dbT.get_Primary_Key_List().size());
		for(int i = 0; i < primaryKeyPairs.length; i++) {
			String primaryKeyPair = primaryKeyPairs[i].replaceAll("\\s+", "");
			String[] fieldAndValue = primaryKeyPair.split("=");
			DataField df = dbT.get_Data_Field(fieldAndValue[0]);
			PrimitiveType pt = CrdtFactory.generateCrdtPrimitiveType(this.getDateFormat(), df, fieldAndValue[1], null);
			dbOpEntry.addPrimaryKey(pt);
		}
	}
	
	/**
	 * Assign next unique id.
	 *
	 * @param tableName the table name
	 * @param dataFieldName the data field name
	 * @return the int
	 */
	public int assignNextUniqueId(String tableName, String dataFieldName) {
		return iDFactory.getNextId(tableName, dataFieldName);
	}

	/**
	 * Sets the cached result set for delta.
	 *
	 * @param cachedResultSetForDelta the cachedResultSetForDelta to set
	 */
	public void setCachedResultSetForDelta(TxMudResultSet cachedResultSetForDelta) {
		this.cachedResultSetForDelta = cachedResultSetForDelta;
	}

	/**
	 * Gets the cached result set for delta.
	 *
	 * @return the cachedResultSetForDelta
	 */
	public TxMudResultSet getCachedResultSetForDelta() {
		return cachedResultSetForDelta;
	}
	
	/**
	 * Gets the fp generator.
	 *
	 * @return the fp generator
	 */
	public RuntimeFingerPrintGenerator getFpGenerator() {
		return this.fpGenerator;
	}

	/**
	 * Gets the date format.
	 *
	 * @return the dateFormat
	 */
	public DateFormat getDateFormat() {
		return dateFormat;
	}

	/**
	 * Sets the date format.
	 *
	 * @param dateFormat the dateFormat to set
	 */
	public void setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}
}
