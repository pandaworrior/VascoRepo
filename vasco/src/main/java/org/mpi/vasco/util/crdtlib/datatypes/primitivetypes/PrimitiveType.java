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
package org.mpi.vasco.util.crdtlib.datatypes.primitivetypes;

// TODO: Auto-generated Javadoc
/**
 * The Class PrimitiveType.
 */
public abstract class PrimitiveType {
	
	/** The data name. */
	String dataName;
	
	/**
	 * Instantiates a new primitive type.
	 *
	 * @param dbFN the db fn
	 */
	public PrimitiveType(String dbFN){
		this.dataName = dbFN;
	}
	
	/**
	 * Gets the data name.
	 *
	 * @return the data name
	 */
	public String getDataName(){
		return this.dataName;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * To string.
	 *
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();
	
	/**
	 * Equal to.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 */
	public abstract boolean equalTo(PrimitiveType obj);
	
	/**
	 * Gets the value by name.
	 *
	 * @param name the name
	 * @return the value by name
	 */
	public abstract String getValueByName(String name);
}
