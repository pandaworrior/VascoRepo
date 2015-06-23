/********************************************************************
Copyright (c) 2013 chengli.
All rights reserved. This program and the accompanying materials
are made available under the terms of the GNU Public License v2.0
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

Contributors:
    chengli - initial API and implementation

Contact:
    To distribute or use this code requires prior specific permission.
    In this case, please contact chengli@mpi-sws.org.
********************************************************************/
/**
 * 
 */
package org.mpi.vasco.sieve.staticanalysis.templatecreator.template;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.core.dom.Statement;

import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;

// TODO: Auto-generated Javadoc
/**
 * The Class Operation.
 */
public abstract class Operation {
	
	/** The parameter prefix. */
	final String parameterPrefix = "var";
	
	/** The parameter id count. */
	private static AtomicInteger parameterIdCount = new AtomicInteger();
	
	/** The table name. */
	private String tableName;
	
	/** The table instance. */
	private DatabaseTable tableInstance;
	
	/** The message digestor. */
	protected static MessageDigest messageDigestor = null;
	
	/**
	 * Instantiates a new operation.
	 *
	 * @param tN the t n
	 * @param dT the d t
	 */
	public Operation(String tN, DatabaseTable dT){
		this.setTableName(tN);
		this.setTableInstance(dT);
		if(messageDigestor == null){
			try {
				messageDigestor = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets the table name.
	 *
	 * @return the table name
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Sets the table name.
	 *
	 * @param tableName the new table name
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Gets the table instance.
	 *
	 * @return the table instance
	 */
	public DatabaseTable getTableInstance() {
		return tableInstance;
	}

	/**
	 * Sets the table instance.
	 *
	 * @param tableInstance the new table instance
	 */
	public void setTableInstance(DatabaseTable tableInstance) {
		this.tableInstance = tableInstance;
	}
	
	/**
	 * Gets the next count.
	 *
	 * @return the next count
	 */
	private int getNextCount(){
		return parameterIdCount.getAndIncrement();
	}
	
	/**
	 * Gets the unique parameter name.
	 *
	 * @return the unique parameter name
	 */
	public String getUniqueParameterName(){
		return this.parameterPrefix + this.getNextCount();
	}
	
	/**
	 * Gets the operation finger print.
	 *
	 * @return the operation finger print
	 */
	public abstract String getOperationFingerPrint();

}
