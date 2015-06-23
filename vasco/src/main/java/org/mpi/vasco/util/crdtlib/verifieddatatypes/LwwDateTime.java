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
 * The Class LwwDateTime.
 */
public class LwwDateTime{
	
	/** The value. */
	private int ldtValue;
	
	/** The lww logical ts. */
	private LwwLogicalTimestamp ldtLwwLogicalTs;
	
	/*: 
	 public encap specvar ldt_value :: "int";
	 public encap specvar ldt_lts :: "obj";
	 */
	
	/*:
	 vardefs "ldt_value == ldtValue"
	 vardefs "ldt_lts == ldtLwwLogicalTs"
	 */
	
	
	/**
	 * Instantiates a new lww date time.
	 *
	 * @param v the v
	 * @param ts the ts
	 */
	public LwwDateTime(int v, LwwLogicalTimestamp ts)
	/*:modifies "ldt_value", "ldt_lts"
	    ensures "ldt_value = v &
	                   ldt_lts = ts"
	 */
	{
		this.ldtValue = v;
		this.ldtLwwLogicalTs = ts;
	}
	
	/**
	 * Sets the logical timestamp.
	 *
	 * @param ts the new logical timestamp
	 */
	public void setLogicalTimestamp(LwwLogicalTimestamp ts)
	/*: modifies "ldt_lts"
	     ensures "ldt_lts = ts"
	 */
	{
		this.ldtLwwLogicalTs = ts;
	}
	
	/**
	 * Update.
	 *
	 * @param v the v
	 * @param lts the lts
	 */
	public void update(int v, LwwLogicalTimestamp lts)
		/*: requires "lts ~= null &  ldt_lts ~= null" 
	     modifies "ldt_value", "ldt_lts"
	     ensures "(ldt_lts..(old lts_ts) <= lts..lts_ts --> (ldt_value = v & ldt_lts  = lts)) & 
	                    (ldt_lts..(old lts_ts) > lts..lts_ts --> (ldt_value = old ldt_value & ldt_lts = old ldt_lts))"
	 */
	{
		int newValue = lts.getValue();
		if(this.ldtLwwLogicalTs.isSmallerThan(newValue)){
			this.ldtValue = v;
			this.ldtLwwLogicalTs = lts;
		}
	}


	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = ldt_value"
	 */
	{
		// TODO Auto-generated method stub
		return this.ldtValue;
	}
	
}
