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
package org.mpi.vasco.util.crdtlib.dbannotationtypes;

// TODO: Auto-generated Javadoc

/**
 * The Class DatabaseDef.
 */
public class DatabaseDef{
	
	/** The insert. */
	public final static byte INSERT = 1;
	
	/** The unique insert. */
	public final static byte UNIQUEINSERT= 2;
	
	/** The update. */
	public final static byte UPDATE = 3;
	
	/** The delete. */
	public final static byte DELETE = 4;
	
	//define all operation type string
	
	/** The Constant DELETE_OP_STR. */
	public final static String DELETE_OP_STR = "DELETE";
	
	/** The Constant INSERT_OP_STR. */
	public final static String INSERT_OP_STR = "INSERT";
	
	/** The Constant UNIQUE_INSERT_OP_STR. */
	public final static String UNIQUE_INSERT_OP_STR = "UNIQUEINSERT";
	
	/** The Constant UPDATE_OP_STR. */
	public final static String UPDATE_OP_STR = "UPDATE";
	
	/**
	 * Gets the dB op name.
	 *
	 * @param opType the op type
	 * @return the dB op name
	 */
	public static String getDBOpName(byte opType) {
		switch(opType) {
		case INSERT:
			return INSERT_OP_STR;
		case UNIQUEINSERT:
			return UNIQUE_INSERT_OP_STR;
		case UPDATE:
			return UPDATE_OP_STR;
		case DELETE:
			return DELETE_OP_STR;
			default:
				throw new RuntimeException("no such db op type " + opType);
		}
	}
	
}
