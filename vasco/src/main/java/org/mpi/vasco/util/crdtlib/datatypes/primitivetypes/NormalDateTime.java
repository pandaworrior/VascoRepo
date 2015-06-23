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

import java.sql.Timestamp;

// TODO: Auto-generated Javadoc
/**
 * The Class NormalDateTime.
 */
public class NormalDateTime extends PrimitiveType{
	
	/** The value. */
	Timestamp value;
	
	public NormalDateTime(String dataName, long ts) {
		super(dataName);
		this.value = new Timestamp(ts);
	}
	
	/**
	 * Instantiates a new normal date time.
	 *
	 * @param dataName the data name
	 * @param v the v
	 */
	public NormalDateTime(String dataName, Timestamp v){
		super(dataName);
		this.value = v;
	}
	
	/**
	 * Update.
	 */
	public void update(){
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.datatypes.primitivetypes.PrimitiveType#toString()
	 */
	/**
	 * @see util.crdtlib.datatypes.primitivetypes.PrimitiveType#toString()
	 * @return
	 */
	@Override
	public String toString() {
		String str = "NormalDateTime" + " value " + this.value;
		return str;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Timestamp getValue() {
		// TODO Auto-generated method stub
		return this.value;
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.datatypes.primitivetypes.PrimitiveType#equalTo(util.crdtlib.datatypes.primitivetypes.PrimitiveType)
	 */
	/**
	 * @see util.crdtlib.datatypes.primitivetypes.PrimitiveType#equalTo(util.crdtlib.datatypes.primitivetypes.PrimitiveType)
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equalTo(PrimitiveType obj) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.datatypes.primitivetypes.PrimitiveType#getValueByName(java.lang.String)
	 */
	@Override
	public String getValueByName(String name) {
		if(name.equals("value")) {
			return Long.toString(this.getValue().getTime());
		}else {
			System.out.println(this.getClass().toString() + " name is not specified correctly " + name);
			System.exit(-1);
		}
		return null;
	}
	
}
