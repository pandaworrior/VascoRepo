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

// TODO: Auto-generated Javadoc
/**
 * The Class LwwBoolean.
 */
public class LwwBoolean{
	
	/** The value. */
	private int lbValue;
	
	/** The lww logical ts. */
	private LwwLogicalTimestamp lbLwwLogicalTs;
	
	/*: 
	 public encap specvar lb_value :: "int";
	 public encap specvar lb_lts :: "obj";
	 */
	
	/*:
	 vardefs "lb_value == lbValue"
	 vardefs "lb_lts == lbLwwLogicalTs"
	 */
	
	/**
	 * Instantiates a new lww boolean.
	 *
	 * @param v the v
	 * @param ts the ts
	 */
	public LwwBoolean(int v, LwwLogicalTimestamp ts)
	/*:modifies "lb_value", "lb_lts"
	    ensures "lb_value = v &
	                   lb_lts = ts"
	 */
	{
		lbValue = v;
		lbLwwLogicalTs = ts;
	}
	
	/**
	 * Sets the logical timestamp.
	 *
	 * @param ts the new logical timestamp
	 */
	public void setLogicalTimestamp(LwwLogicalTimestamp ts)
	/*: modifies "lb_lts"
	     ensures "lb_lts = ts"
	 */
	{
		this.lbLwwLogicalTs = ts;
	}
	
	/**
	 * Update.
	 *
	 * @param v the v
	 * @param lts the lts
	 */
	public void update(int v, LwwLogicalTimestamp lts)
	/*: requires "lb_lts ~= null & lts ~= null"
	     modifies "lb_value", "lb_lts"
	     ensures "(lb_lts..(old lts_ts) <= lts..lts_ts --> (lb_value = v & lb_lts = lts)) & 
	                    (lb_lts..(old lts_ts) > lts..lts_ts --> (lb_value = old lb_value & lb_lts = old lb_lts))"
	 */
	{
		int newTsValue = lts.getValue();
		if(this.lbLwwLogicalTs.isSmallerThan(newTsValue)){
			this.lbValue = v;
			this.lbLwwLogicalTs = lts;
		}
	}
	

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = lb_value"
	 */
	{
		// TODO Auto-generated method stub
		return this.lbValue;
	}
}
