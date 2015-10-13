/*That class defines a databases class to contain all conn info and table definition.
import util.Debug;
 * each database has a id, which is equal to storage id. */
package org.mpi.vasco.txstore.appextend;
import java.sql.*;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;

import org.mpi.vasco.txstore.scratchpad.rdbms.DBScratchpad;
import org.mpi.vasco.util.debug.Debug;

/*for mysql database connection*/

public class Database {
	
	/*dc id*/
	public int dc_id;
	/*storage id*/
	public int db_id;
	/*connection info*/
	public Vector<String> connInfo = new Vector<String>();
	/*table list*/
	public Vector<String> tableList = new Vector<String>();
	/*tableLWWList*/
	public Vector<String> tableLWWList = new Vector<String>();
	/*red tables*/
	public Vector<String> redTableList = new Vector<String>();
	/*blue tables*/
	public Vector<String> blueTableList = new Vector<String>();
	
	public String primaryKey = "";
	/*fetch info from database*/
	
	public String url_prefix = "jdbc:mysql://";
	
	public Connection conn = null;
	
	public HashMap<String, TableSchema> tableSchemaList = new HashMap<String, TableSchema>();
	
	public Database(int i1, int i2, Vector<String> s1, Vector<String> s2, Vector<String> s3, Vector<String> s4, Vector<String> s5, String urlPrefix){
		dc_id = i1;
		db_id = i2;
		connInfo = s1;
		tableList = s2;
		tableLWWList = s3;
		redTableList = s4;
		blueTableList = s5;
		url_prefix = urlPrefix;
	}
	
	/*table definition*/
	
	public Connection connectToDB(){
		// it will use connInfo
		
		String url = url_prefix + connInfo.get(0) + ':' + connInfo.get(1) + '/'
				+ connInfo.get(4);
		String userName = connInfo.get(2);
		String password = connInfo.get(3);
		Debug.printf("connection url: %s \n", url);
		try{
			Class.forName("com.mysql.jdbc.Driver");
			Class.forName("com.mimer.jdbc.Driver");
		}
		catch(ClassNotFoundException e){
			Debug.println("Driver class not found in classpath");
		}
		try{
			conn = DriverManager.getConnection(url, userName, password);
			Debug.println ("Database connection established");
		}
		catch(SQLException e){
			System.err.println("Cannot connect to database server");
			e.printStackTrace();
		}
		return conn;
		
	}
	
	public void disconnectDB(){
		if(conn != null){
			try{
				conn.close();
				Debug.println ("Database connection terminated");
			}
			catch(Exception e){
				/* ignore close errors */
			}
		}
	}

	public void fetchTableInfo(String tableName){
		TableSchema ts = new TableSchema(tableName, db_id);
		String sqlStr = "describe "+ tableName + ";";
		System.out.print(sqlStr+"\n");
		Statement s;
		ResultSet rs;
		try {
			s = conn.createStatement();
			s.executeQuery(sqlStr);
			rs = s.getResultSet();
			while(rs.next()){
				String fieldName = rs.getString(1);
				String typeName = rs.getString(2);
				if (!fieldName.equals(DBScratchpad.SCRATCHPAD_COL_PREFIX+"clock")&&!fieldName.equals(DBScratchpad.SCRATCHPAD_COL_PREFIX+"del")&&!fieldName.equals(DBScratchpad.SCRATCHPAD_COL_PREFIX+"ts"))
					ts.addToColumnList(fieldName, typeName);
			}
			tableSchemaList.put(tableName, ts);
			ts.printOut();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void setTableSchema(TableSchema tch){
		tableSchemaList.put(tch.table_name, tch);
	}
	
	public void setPrimaryKey(String pk){
		primaryKey = pk;
	}
	
	public void fetchTablesInfo(){
		
		String sqlStr = "show tables;";
		Statement s;
		ResultSet rs;
		try {
			s = conn.createStatement();
			s.executeQuery(sqlStr);
			int count = 0;
			rs = s.getResultSet();
			while(rs.next()){
				count++;
			}
			assert(count == tableList.size());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		Iterator<String> i = tableList.iterator();
		while(i.hasNext()){
			fetchTableInfo(i.next());
		}
	}
	
	public void parseDatabase(){
		connectToDB();
		fetchTablesInfo();
		disconnectDB();
		
	}
	
	public Vector<String> getConnInfo(){
		return connInfo;
	}
	
	public Vector<String> getRedTableList(){
		return redTableList;
	}
	
	public Vector<String> getBlueTableList(){
		return blueTableList;
	}
	
	public void printOut(){
		if(Debug.debug == true){
			System.out.print("-------print out database------\n");
			System.out.printf("dcId %d, dbId %d\n", dc_id, db_id);
			
			Iterator<String> i = connInfo.iterator();
			System.out.printf("connInfo: %s, %s, %s, %s, %s\n", i.next(), i.next(),i.next(),i.next(),i.next());
			
			System.out.println("urlPrefix"+url_prefix);
			
			System.out.println("primarykey:" + primaryKey + "\n");
			
			i = tableList.iterator();
			System.out.print("tableList:");
			while(i.hasNext()){
				System.out.print(i.next());
			}
			System.out.print("\n");
			
			i = tableLWWList.iterator();
			System.out.print("tableLWWList:");
			while(i.hasNext()){
				System.out.print(i.next());
			}
			System.out.print("\n");
			
			i = redTableList.iterator();
			System.out.print("redTabeList:");
			while(i.hasNext()){
				System.out.print(i.next());
			}
			System.out.print("\n");
			
			i = blueTableList.iterator();
			System.out.print("blueTabeList:");
			while(i.hasNext()){
				System.out.print(i.next());
			}
			System.out.print("\n");
			
			Iterator<TableSchema> schemaIt = tableSchemaList.values().iterator();
			while(schemaIt.hasNext()){
				TableSchema el = schemaIt.next();
				el.printOut();
			}
		}
		
	}
	
}
