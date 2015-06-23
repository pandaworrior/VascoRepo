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
 * The Class NumberDeltaDateTime.
 */
public class NumberDeltaDateTime{
	
	/** The value. */
	private int nddtValue;
	
	/** The delta. */
	private int nddtDelta;
	
	/*: public encap specvar nddt_value :: "int"; 
	     public encap specvar nddt_delta :: "int";*/
	
	/*: vardefs "nddt_value == nddtValue"; 
	    vardefs "nddt_delta == nddtDelta"*/
	
	/**
	 * Instantiates a new number delta date time.
	 *
	 * @param v the v
	 */
	public NumberDeltaDateTime(int v, int d)
	/*: modifies "nddt_value", "nddt_delta" 
    	ensures "nddt_value = v & nddt_delta = d & 
                   (ALL nb. nb : Object.alloc &  nb : NumberDeltaDateTime & nb ~= null & nb ~= this -->
                   	(nb..nddt_value = nb..(old NumberDeltaDateTime.nddt_value) & 
                   	 nb..nddt_delta = nb..(old NumberDeltaDateTime.nddt_delta))) "*/
	{
		this.nddtValue = v;
		this.nddtDelta = d;
	}
	
	/**
	 * Update.
	 *
	 * @param delta the delta
	 */
	public void update(int delta)
	/*: modifies "nddt_value", "nddt_delta" 
		ensures "nddt_value = old nddt_value + delta & nddt_delta = delta &
               (ALL nb. nb : Object.alloc &  nb : NumberDeltaDateTime & nb ~= null & nb ~= this -->
               	(nb..nddt_value = nb..(old NumberDeltaDateTime.nddt_value) & 
               	 nb..nddt_delta = nb..(old NumberDeltaDateTime.nddt_delta))) "*/
	{
		this.nddtDelta = delta;
		this.nddtValue = this.nddtValue + delta;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = nddt_value"*/
	{
		// TODO Auto-generated method stub
		return this.nddtValue;
	}
	
	public int getDelta()
	/*: ensures "result = nddt_delta"*/
	{
		return this.nddtDelta;
	}
}
