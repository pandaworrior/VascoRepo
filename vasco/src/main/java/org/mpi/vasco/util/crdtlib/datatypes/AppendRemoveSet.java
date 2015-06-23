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
 * The Class AppendRemoveSet.
 */
public class AppendRemoveSet extends CrdtSet{

	/**
	 * Instantiates a new append remove set.
	 *
	 * @param dN the d n
	 * @param dT the d t
	 */
	public AppendRemoveSet(String dN, Tuple dT) {
		super(dN, dT);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.datatypes.CrdtSet#insert(util.crdtlib.datatypes.Tuple)
	 */
	/**
	 * @see util.crdtlib.datatypes.CrdtSet#insert(util.crdtlib.datatypes.Tuple)
	 * @param dataTuple
	 */
	@Override
	public void insert(Tuple dataTuple) {
		// TODO Auto-generated method stub
		if(this.getTuple() == null){
		}
		
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.datatypes.CrdtSet#update(util.crdtlib.datatypes.Tuple)
	 */
	/**
	 * @see util.crdtlib.datatypes.CrdtSet#update(util.crdtlib.datatypes.Tuple)
	 * @param dataTuple
	 */
	@Override
	public void update(Tuple dataTuple) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.datatypes.CrdtSet#delete(util.crdtlib.datatypes.Tuple)
	 */
	/**
	 * @see util.crdtlib.datatypes.CrdtSet#delete(util.crdtlib.datatypes.Tuple)
	 * @param dataTuple
	 */
	@Override
	public void delete(Tuple dataTuple) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see util.crdtlib.datatypes.CrdtSet#toString()
	 */
	/**
	 * @see util.crdtlib.datatypes.CrdtSet#toString()
	 * @return
	 */
	public String toString(){
		String str = "AppendRemoveSet " + super.toString();
		return str;
	}

}
