/*
 * This class defines methods to parse sql schema to create all table and field
 * crdts.
 */
package org.mpi.vasco.util.annotationparser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.util.crdtlib.dbannotationtypes.AosetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.ArsetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.AusetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.READONLY_Table;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.UosetTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.RuntimeExceptionType;

// TODO: Auto-generated Javadoc
/**
 * The Class SchemaParser.
 */
public class SchemaParser {

	/** The file name. */
	private String fileName = "";
	
	/** The table crdt form map. */
	private HashMap<String, DatabaseTable> tableCrdtFormMap;

	/**
	 * Instantiates a new schema parser.
	 *
	 * @param fN the f n
	 */
	public SchemaParser(String fN) {
		this.setFileName(fN);
	}
	
	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name.
	 *
	 * @param fileName the new file name
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets the table crdt form map.
	 *
	 * @return the table crdt form map
	 */
	public HashMap<String, DatabaseTable> getTableCrdtFormMap() {
		return tableCrdtFormMap;
	}
	
	public List<DatabaseTable> getAllTableInstances(){
		return new ArrayList<DatabaseTable>(this.tableCrdtFormMap.values());
	}

	/**
	 * Sets the table crdt form map.
	 *
	 * @param tableCrdtFormMap the table crdt form map
	 */
	public void setTableCrdtFormMap(HashMap<String, DatabaseTable> tableCrdtFormMap) {
		this.tableCrdtFormMap = tableCrdtFormMap;
	}

	/**
	 * Gets the all create table strings.
	 *
	 * @return the all create table strings
	 */
	public Vector<String> getAllCreateTableStrings(){
		BufferedReader br;
		String schemaContentStr = "";
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName)));
			while ((line = br.readLine()) != null) {
				schemaContentStr = schemaContentStr + line;
			}
			br.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String[] allStrings = schemaContentStr.split(";");
		Vector<String> allCreateTableStrings = new Vector<String>();

		for (int i = 0; i < allStrings.length; i++) {
			if (CreateStatementParser.is_Create_Table_Statement(allStrings[i]) == true) {
				allCreateTableStrings.add(allStrings[i]);
			}
		}

		if (allCreateTableStrings.isEmpty() == true) {
			try {
				throw new RuntimeException(
						"This schema doesn't contain any create statement");
			} catch (RuntimeException e) {
				System.exit(RuntimeExceptionType.SCHEMANOCREATSTAT);
			}
		}

		return allCreateTableStrings;
	}


	
	/**
	 * Parses the annotations.
	 */
	public void parseAnnotations(){
		Vector<String> allTableStrings = this.getAllCreateTableStrings();
		this.tableCrdtFormMap = new HashMap<String, DatabaseTable>();

		for (int i = 0; i < allTableStrings.size(); i++) {
			DatabaseTable dT = CreateStatementParser
					.create_Table_Instance(allTableStrings.elementAt(i));
			if (dT != null) {
				this.tableCrdtFormMap.put(dT.get_Table_Name(), dT);
			}else {
				throw new RuntimeException("Cannot create a tableinstance for this table " + allTableStrings.elementAt(i));
			}
		}

		if (this.tableCrdtFormMap.isEmpty() == true) {
			try {
				throw new RuntimeException("No CRDT tables are created!");
			} catch (RuntimeException e) {
				e.printStackTrace();
				System.exit(RuntimeExceptionType.SCHEMANOCRDTTABLE);
			}
		}
	}
	
	/**
	 * Gets the table by name.
	 *
	 * @param tableName the table name
	 * @return the table by name
	 */
	public DatabaseTable getTableByName(String tableName){
		return this.tableCrdtFormMap.get(tableName);
	}

	/**
	 * Prints the out.
	 */
	public void printOut() {
		Debug.println("Now Print Out all Table information");
		Iterator<Map.Entry<String, DatabaseTable>> it = this.tableCrdtFormMap.entrySet()
				.iterator();
		while (it.hasNext()) {
			Map.Entry<String, DatabaseTable> entry = (Map.Entry<String, DatabaseTable>) it
					.next();
			DatabaseTable dT = entry.getValue();
			if (dT instanceof AosetTable)
				Debug.println(((AosetTable) dT).toString());
			else if (dT instanceof ArsetTable)
				Debug.println(((ArsetTable) dT).toString());
			else if (dT instanceof UosetTable)
				Debug.println(((UosetTable) dT).toString());
			else if(dT instanceof AusetTable)
				Debug.println(((AusetTable) dT).toString());
			else if (dT instanceof READONLY_Table)
				Debug.println(((READONLY_Table) dT).toString());
			else {
				try {
					throw new RuntimeException("The type of CRDT table "
							+ dT.get_CRDT_Table_Type()
							+ "is not supported by our framework!");
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.NOTDEFINEDCRDTTABLE);
				}
			}
		}
		Debug.println("End Print Out all Table information");
	}
}
