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
package org.mpi.vasco.util.crdtlib.datatypes;

// TODO: Auto-generated Javadoc
/**
 * The Class CrdtSet.
 */
public abstract class CrdtSet {
	
	/** The data name. */
	String dataName;
	
	/** The data tuple. */
	Tuple dataTuple;
	
	/**
	 * Instantiates a new crdt set.
	 *
	 * @param dN the d n
	 * @param dT the d t
	 */
	public CrdtSet(String dN, Tuple dT){
		this.dataName = dN;
		this.dataTuple = dT;
	}
	
	/**
	 * Insert.
	 *
	 * @param dataTuple the data tuple
	 */
	public abstract void insert(Tuple dataTuple);
	
	/**
	 * Update.
	 *
	 * @param dataTuple the data tuple
	 */
	public abstract void update(Tuple dataTuple);
	
	/**
	 * Delete.
	 *
	 * @param dataTuple the data tuple
	 */
	public abstract void delete(Tuple dataTuple);
	
	/**
	 * Gets the data name.
	 *
	 * @return the data name
	 */
	public String getDataName(){
		return this.dataName;
	}
	
	/**
	 * Gets the tuple.
	 *
	 * @return the tuple
	 */
	public Tuple getTuple(){
		return this.dataTuple;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @see java.lang.Object#toString()
	 * @return
	 */
	public String toString(){
		return null;
	}

}
