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
 * The Class LwwInteger.
 */
public class LwwInteger{
	
	/** The value. */
	private int liValue;
	
	/** The lww logical ts. */
	private LwwLogicalTimestamp liLwwLogicalTs;
	
	/*: 
	 public encap specvar li_value :: "int";
	 public encap specvar li_lts :: "obj";
	 */
	
	/*:
	 vardefs "li_value == liValue"
	 vardefs "li_lts == liLwwLogicalTs"
	 */
	
	/**
	 * Instantiates a new lww integer.
	 *
	 * @param v the v
	 * @param ts the ts
	 */
	public LwwInteger(int v, LwwLogicalTimestamp ts)
	/*:modifies "li_value", "li_lts"
	    ensures "li_value = v &
	                   li_lts = ts"
	 */
	{
		this.liValue = v;
		this.liLwwLogicalTs = ts;
	}
	
	/**
	 * Sets the logical timestamp.
	 *
	 * @param ts the new logical timestamp
	 */
	public void setLogicalTimestamp(LwwLogicalTimestamp ts)
	/*: modifies "li_lts"
	     ensures "li_lts = ts"
	 */
	{
		this.liLwwLogicalTs = ts;
	}
	
	/**
	 * Update.
	 *
	 * @param v the v
	 * @param lts the lts
	 */
	public void update(int v, LwwLogicalTimestamp lts)
	/*: requires "lts ~= null  & li_lts ~= null" 
	     modifies "li_value", "li_lts"
	     ensures "(li_lts..(old lts_ts) <= lts..lts_ts --> (li_value = v & li_lts  = lts)) & 
	                    (li_lts..(old lts_ts) > lts..lts_ts --> (li_value = old li_value & li_lts = old li_lts))"
	 */
	{
		int newValue = lts.getValue();
		if(this.liLwwLogicalTs.isSmallerThan(newValue)){
			this.liValue = v;
			this.liLwwLogicalTs = lts;
		}
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = li_value"
	 */
	{
		// TODO Auto-generated method stub
		return this.liValue;
	}
	
}
