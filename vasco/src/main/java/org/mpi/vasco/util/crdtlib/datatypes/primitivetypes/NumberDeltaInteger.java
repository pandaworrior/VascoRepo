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
 * The Class NumberDeltaInteger.
 */
public class NumberDeltaInteger extends PrimitiveType{
	
	/** The value. */
	private int value;
	
	/** The delta. */
	private int delta;
	
	/**
	 * Instantiates a new number delta integer.
	 *
	 * @param dataName the data name
	 * @param delta the delta
	 */
	public NumberDeltaInteger(String dataName, int delta){
		super(dataName);
		this.delta = delta;
	}
	
	/**
	 * Instantiates a new number delta integer.
	 *
	 * @param dataName the data name
	 * @param v the v
	 * @param delta the delta
	 */
	public NumberDeltaInteger(String dataName, int v, int delta){
		super(dataName);
		this.value = v;
		this.delta = delta;
	}
	
	/**
	 * Update.
	 *
	 * @param delta the delta
	 */
	public void update(int delta){
		this.value = this.value + delta;
	}
	
	/**
	 * Gets the new value.
	 *
	 * @param delta the delta
	 * @return the new value
	 */
	public int getNewValue(int delta){
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
		String str = "NumberDeltaInteger" +  " delta " + this.delta;
		return str;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() {
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
	public int getDelta() {
		return delta;
	}

	/**
	 * Sets the delta.
	 *
	 * @param delta the delta to set
	 */
	public void setDelta(int delta) {
		this.delta = delta;
	}

	/* (non-Javadoc)
	 * @see util.crdtlib.datatypes.primitivetypes.PrimitiveType#getValueByName(java.lang.String)
	 */
	@Override
	public String getValueByName(String name) {
		if(name.equals("value"))
			return Integer.toString(this.getValue());
		else {
			if(name.equals("delta")) {
				return Integer.toString(this.getDelta());
			}else {
				System.out.println(this.getClass().toString() + " name is not specified correctly " + name);
				System.exit(-1);
			}
		}
		return null;
	}
}
