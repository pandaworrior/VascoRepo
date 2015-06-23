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
 * The Class LwwDouble.
 */
public class LwwDouble{
	
	/** The value. */
	private int ldValue;
	
	/** The lww logical ts. */
	private LwwLogicalTimestamp ldLwwLogicalTs;
	
	/*: 
	 public encap specvar ld_value :: "int";
	 public encap specvar ld_lts :: "obj";
	 */
	
	/*:
	 vardefs "ld_value == ldValue"
	 vardefs "ld_lts == ldLwwLogicalTs"
	 */
	
	/**
	 * Instantiates a new lww double.
	 *
	 * @param v the v
	 * @param ts the ts
	 */
	public LwwDouble(int v, LwwLogicalTimestamp ts)
	/*:modifies "ld_value", "ld_lts"
	    ensures "ld_value = v &
	                   ld_lts = ts"
	 */
	{
		this.ldValue = v;
		this.ldLwwLogicalTs = ts;
	}
	
	/**
	 * Sets the logical timestamp.
	 *
	 * @param ts the new logical timestamp
	 */
	public void setLogicalTimestamp(LwwLogicalTimestamp ts)
	/*: modifies "ld_lts"
	     ensures "ld_lts = ts"
	 */
	{
		this.ldLwwLogicalTs = ts;
	}
	
	/**
	 * Update.
	 *
	 * @param v the v
	 * @param lts the lts
	 */
	public void update(int v, LwwLogicalTimestamp lts)
	/*: requires "lts ~= null & ld_lts ~= null" 
	     modifies "ld_value", "ld_lts"
	     ensures "(ld_lts..(old lts_ts) <= lts..lts_ts --> (ld_value = v & ld_lts  = lts)) & 
	                    (ld_lts..(old lts_ts) > lts..lts_ts --> (ld_value = old ld_value & ld_lts = old ld_lts))"
	 */
	{
		int newValue = lts.getValue();
		if(this.ldLwwLogicalTs.isSmallerThan(newValue)){
			this.ldValue = v;
			this.ldLwwLogicalTs = lts;
		}
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = ld_value"
	 */
	{
		// TODO Auto-generated method stub
		return this.ldValue;
	}
	
}
