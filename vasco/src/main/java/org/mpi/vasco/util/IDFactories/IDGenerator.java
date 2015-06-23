package org.mpi.vasco.util.IDFactories;

import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.RuntimeExceptionType;

import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class IDGenerator.
 */
public class IDGenerator {

	/** The id name. */
	String idName;
	
	/** The id value. */
	AtomicInteger idValue;

	/** The global proxy id. */
	static int globalProxyId;
	
	/** The total proxy. */
	static int totalProxy;
	
	/** The con. */
	static Connection con;

	/**
	 * Initialized.
	 *
	 * @param gPI the g pi
	 * @param tP the t p
	 * @param cN the c n
	 */
	public static void initialized(int gPI, int tP, Connection cN) {
		System.out.println("My global proxy Id is :" + gPI
				+ " the total proxy num is :" + tP);
		globalProxyId = gPI;
		totalProxy = tP;
		con = cN;
	}

	/**
	 * Instantiates a new iD generator.
	 *
	 * @param tableName the table name
	 * @param dataFieldName the data field name
	 */
	IDGenerator(String tableName, String dataFieldName) {
		idName = tableName + "_" + dataFieldName;
		idValue = new AtomicInteger();
		String selectQuery = "SELECT MAX(" + dataFieldName + ") AS "
				+ dataFieldName + " ";
		selectQuery += " FROM " + tableName;

		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(selectQuery);
			if (rs.next()) {
				int id = rs.getInt(dataFieldName);
				idValue.set(id + globalProxyId);
				rs.close();
				Debug.println("set initial " + idName + " to "
						+ idValue.intValue());
			} else {
				Debug.println("no initial value for table " + tableName
						+ " key " + dataFieldName);
				try {
					throw new RuntimeException(
							"No result returned in ID generator for table "
									+ tableName + " key " + dataFieldName);
				} catch (RuntimeException e) {
					e.printStackTrace();
					System.exit(RuntimeExceptionType.NORESULT);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(RuntimeExceptionType.SQLSELECTIONFAIL);
		}

	}

	/**
	 * Gets the next id.
	 *
	 * @return the next id
	 */
	public int getNextId() {
		Debug.println("Current id is :" + idValue.get());
		Debug.println("total proxy id:" + totalProxy);
		return idValue.addAndGet(totalProxy);
	}

}
