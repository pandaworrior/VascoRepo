/***************************************************************
Project name: georeplication
Class file name: Invariant.java
Created at 7:42:04 PM by chengli

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
****************************************************************/

package org.mpi.vasco.util.annotationparser;

import java.util.ArrayList;
import java.util.List;

import org.mpi.vasco.sieve.staticanalysis.templatecreator.DatabaseTableClassCreator;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class Invariant.
 *
 * @author chengli
 */
public class Invariant {
	
	/** The invariant str. */
	private String invariantStr;
	
	/** The table list. */
	private List<String> tableList;
	
	private static SchemaParser sp;
	
	/**
	 * Instantiates a new invariant.
	 *
	 * @param inv the inv
	 */
	public Invariant(String inv) {
		this.setInvariantStr(inv);
		this.tableList = new ArrayList<String>();
		this.setTableList();
	}
	
	public static void setSchemaParser(SchemaParser sP) {
		sp = sP;
	}

	/**
	 * Gets the invariant str.
	 *
	 * @return the invariantStr
	 */
	public String getInvariantStr() {
		return invariantStr;
	}

	/**
	 * Sets the invariant str.
	 *
	 * @param invariantStr the invariantStr to set
	 */
	public void setInvariantStr(String invariantStr) {
		this.invariantStr = invariantStr;
	}

	/**
	 * Gets the table list.
	 *
	 * @return the tableList
	 */
	public List<String> getTableList() {
		return tableList;
	}

	/**
	 * Sets the table list.
	 *
	 * @param tableList the tableList to set
	 */
	public void setTableList(List<String> tableList) {
		this.tableList = tableList;
	}
	
	/**
	 * Adds the table to list.
	 *
	 * @param tableName the table name
	 */
	public void addTableToList(String tableName) {
		this.tableList.add(tableName);
	}
	
	public void setTableList() {
		List<DatabaseTable> dtList = sp.getAllTableInstances();
		for(DatabaseTable dt : dtList) {
			String tableName = dt.get_Table_Name();
			if(this.getInvariantStr().contains(tableName)) {
				Debug.println("we find a tableName " + tableName);
				this.addTableToList(tableName);
			}
		}
	}
}
