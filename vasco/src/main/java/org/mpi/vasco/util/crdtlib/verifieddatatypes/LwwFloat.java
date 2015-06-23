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
 * The Class LwwFloat.
 */
public class LwwFloat{
	
	/** The value. */
	private int lfValue;
	
	/** The lww logical ts. */
	private LwwLogicalTimestamp lfLwwLogicalTs;
	
	/*: 
	 public encap specvar lf_value :: "int";
	 public encap specvar lf_lts :: "obj";
	 */
	
	/*:
	 vardefs "lf_value == lfValue"
	 vardefs "lf_lts == lfLwwLogicalTs"
	 */
	
	/**
	 * Instantiates a new lww float.
	 *
	 * @param v the v
	 * @param ts the ts
	 */
	public LwwFloat(int v, LwwLogicalTimestamp ts)
	/*:modifies "lf_value", "lf_lts"
	    ensures "lf_value = v &
	                   lf_lts = ts"
	 */
	{
		this.lfValue = v;
		this.lfLwwLogicalTs = ts;
	}
	
	/**
	 * Sets the logical timestamp.
	 *
	 * @param ts the new logical timestamp
	 */
	public void setLogicalTimestamp(LwwLogicalTimestamp ts)
	/*: modifies "lf_lts"
	     ensures "lf_lts = ts"
	 */
	{
		this.lfLwwLogicalTs = ts;
	}
	
	/**
	 * Update.
	 *
	 * @param v the v
	 * @param lts the lts
	 */
	public void update(int v, LwwLogicalTimestamp lts)
	/*: requires "lts ~= null & lf_lts ~= null" 
	     modifies "lf_value", "lf_lts"
	     ensures "(lf_lts..(old lts_ts) <= lts..lts_ts --> (lf_value = v & lf_lts  = lts)) & 
	                    (lf_lts..(old lts_ts) > lts..lts_ts --> (lf_value = old lf_value & lf_lts = old lf_lts))"
	 */
	{
		int newValue = lts.getValue();
		if(this.lfLwwLogicalTs.isSmallerThan(newValue)){
			this.lfValue = v;
			this.lfLwwLogicalTs = lts;
		}
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = lf_value"
	 */
	{
		// TODO Auto-generated method stub
		return this.lfValue;
	}
	
}
