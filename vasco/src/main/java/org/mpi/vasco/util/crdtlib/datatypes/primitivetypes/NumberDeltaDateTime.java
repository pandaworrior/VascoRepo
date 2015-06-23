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

/**
 * The Class NumberDeltaDateTime.
 */
public class NumberDeltaDateTime extends PrimitiveType{
	
	/** The value. */
	private Timestamp value;
	
	/** The delta. */
	private Timestamp delta;
	
	/**
	 * Instantiates a new number delta date time.
	 *
	 * @param dataName the data name
	 * @param ts the ts
	 */
	public NumberDeltaDateTime(String dataName, long ts) {
		super(dataName);
		this.delta = new Timestamp(ts);
	}
	
	/**
	 * Instantiates a new number delta date time.
	 *
	 * @param dataName the data name
	 * @param delta the delta
	 */
	public NumberDeltaDateTime(String dataName, Timestamp delta){
		super(dataName);
		this.delta = delta;
	}
	
	/**
	 * Instantiates a new number delta date time.
	 *
	 * @param dataName the data name
	 * @param v the v
	 * @param delta the delta
	 */
	public NumberDeltaDateTime(String dataName, Timestamp v, Timestamp delta){
		super(dataName);
		this.value = v;
		this.delta = delta;
	}
	
	/**
	 * Update.
	 *
	 * @param delta the delta
	 */
	public void update(Timestamp delta){
		long timestampLong = this.value.getTime() + delta.getTime();
		this.value = new Timestamp(timestampLong);
	}
	
	/**
	 * Gets the new value.
	 *
	 * @param delta the delta
	 * @return the new value
	 */
	public Timestamp getNewValue(Timestamp delta){
		this.update(delta);
		return this.value;
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.datatypes.primitivetypes.PrimitiveType#toString()
	 */
	/**
	 * To string.
	 *
	 * @return the string
	 * @see util.crdtlib.datatypes.primitivetypes.PrimitiveType#toString()
	 */
	@Override
	public String toString() {
		String str = "NumberDeltaDataTime" + " delta " + this.delta;
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
	 * Equal to.
	 *
	 * @param obj the obj
	 * @return true, if successful
	 * @see util.crdtlib.datatypes.primitivetypes.PrimitiveType#equalTo(util.crdtlib.datatypes.primitivetypes.PrimitiveType)
	 */
	@Override
	public boolean equalTo(PrimitiveType obj) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Gets the delta.
	 *
	 * @return the delta
	 */
	public Timestamp getDelta() {
		return delta;
	}

	/**
	 * Sets the delta.
	 *
	 * @param delta the delta to set
	 */
	public void setDelta(Timestamp delta) {
		this.delta = delta;
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.datatypes.primitivetypes.PrimitiveType#getValueByName(java.lang.String)
	 */
	@Override
	public String getValueByName(String name) {
		if(name.equals("value"))
			return Long.toString(this.getValue().getTime());
		else {
			if(name.equals("delta")) {
				return Long.toString(this.getDelta().getTime());
			}else {
				System.out.println(this.getClass().toString() + " name is not specified correctly " + name);
				System.exit(-1);
			}
		}
		return null;
	}
}
