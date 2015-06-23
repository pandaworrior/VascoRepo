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
 * The Class LwwLogicalTimestamp.
 */
public class LwwLogicalTimestamp extends PrimitiveType{

	/** The l timestamp. */
	LogicalTimestamp lTimestamp;
	
	/**
	 * Instantiates a new lww logical timestamp.
	 *
	 * @param dataName the data name
	 */
	public LwwLogicalTimestamp(String dataName){
		super(dataName);
	}
	
	/**
	 * Instantiates a new lww logical timestamp.
	 *
	 * @param dataName the data name
	 * @param lTimestamp the l timestamp
	 */
	public LwwLogicalTimestamp(String dataName, LogicalTimestamp lTimestamp){
		super(dataName);
		this.lTimestamp = lTimestamp;
	}
	
	/**
	 * Gets the logical time stamp.
	 *
	 * @return the logical time stamp
	 */
	public LogicalTimestamp getLogicalTimeStamp(){
		return this.lTimestamp;
	}
	
	/**
	 * Sets the logical timestamp.
	 *
	 * @param lTimestamp the new logical timestamp
	 */
	public void setLogicalTimestamp(LogicalTimestamp lTimestamp){
		this.lTimestamp = lTimestamp;
	}
	
	/**
	 * Update.
	 *
	 * @param lts the lts
	 */
	public void update(LwwLogicalTimestamp lts){
		if(lts.getLogicalTimeStamp().compareTo(this.getLogicalTimeStamp()) == 1){
			this.setLogicalTimestamp(lts.getLogicalTimeStamp());
		}
	}
	
	/**
	 * Checks if is smaller than.
	 *
	 * @param lts the lts
	 * @return true, if is smaller than
	 */
	public boolean isSmallerThan(LwwLogicalTimestamp lts){
		return true;
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
		String str = "LwwLogicalTimestamp" + " value " + this.lTimestamp.toString();
		return str;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public LogicalTimestamp getValue() {
		// TODO Auto-generated method stub
		return this.lTimestamp;
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
			return this.getValue().toString();
		}else {
			System.out.println(this.getClass().toString() + " name is not specified correctly " + name);
			System.exit(-1);
		}
		return null;
	}
	
}
