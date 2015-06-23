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
package util.crdtlib.verifieddatatypes;

// TODO: Auto-generated Javadoc
/**
 * The Class LwwString.
 */
public class LwwString{
	
	/** The value. */
	private int lsValue;
	
	/** The lww logical ts. */
	private LwwLogicalTimestamp lsLwwLogicalTs;
	
	/*: 
	 public encap specvar ls_value :: "int";
	 public encap specvar ls_lts :: "obj";
	 */
	
	/*:
	 vardefs "ls_value == lsValue"
	 vardefs "ls_lts == lsLwwLogicalTs"
	 */
	
	
	/**
	 * Instantiates a new lww string.
	 *
	 * @param v the v
	 * @param ts the ts
	 */
	public LwwString(int v, LwwLogicalTimestamp ts)
	/*:modifies "ls_value", "ls_lts"
	    ensures "ls_value = v &
	                   ls_lts = ts"
	 */
	{
		this.lsValue = v;
		this.lsLwwLogicalTs = ts;
	}
	
	/**
	 * Sets the logical timestamp.
	 *
	 * @param ts the new logical timestamp
	 */
	public void setLogicalTimestamp(LwwLogicalTimestamp ts)
	/*: modifies "ls_lts"
	     ensures "ls_lts = ts"
	 */
	{
		this.lsLwwLogicalTs = ts;
	}
	
	/**
	 * Update.
	 *
	 * @param v the v
	 * @param lts the lts
	 */
	public void update(int v, LwwLogicalTimestamp lts)
	/*: requires "lts ~= null  & ls_lts ~= null" 
	     modifies "ls_value", "ls_lts"
	     ensures "(ls_lts..(old lts_ts) <= lts..lts_ts --> (ls_value = v & ls_lts  = lts)) & 
	                    (ls_lts..(old lts_ts) > lts..lts_ts --> (ls_value = old ls_value & ls_lts = old ls_lts))"
	 */
	{
		int newValue = lts.getValue();
		if(this.lsLwwLogicalTs.isSmallerThan(newValue)){
			this.lsValue = v;
			this.lsLwwLogicalTs = lts;
		}
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = ls_value"
	 */
	{
		// TODO Auto-generated method stub
		return this.lsValue;
	}
}
