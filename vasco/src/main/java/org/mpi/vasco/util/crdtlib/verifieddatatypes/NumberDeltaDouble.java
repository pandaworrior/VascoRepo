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
 * The Class NumberDeltaDouble.
 */
public class NumberDeltaDouble{
	
	/** The value. */
	private int  nddValue;
	
	/** The delta. */
	private int nddDelta;
	
	/*: public encap specvar ndd_value :: "int"; 
	     public encap specvar ndd_delta :: "int";*/
	
	/*: vardefs "ndd_value == nddValue"; 
	    vardefs "ndd_delta == nddDelta"*/
	
	/**
	 * Instantiates a new number delta double.
	 *
	 * @param v the v
	 */
	public NumberDeltaDouble(int v, int d)
	/*: modifies "ndd_value", "ndd_delta" 
    	ensures "ndd_value = v & ndd_delta = d & 
                   (ALL nb. nb : Object.alloc &  nb : NumberDeltaDouble & nb ~= null & nb ~= this -->
                   	(nb..ndd_value = nb..(old NumberDeltaDouble.ndd_value) & 
                   	 nb..ndd_delta = nb..(old NumberDeltaDouble.ndd_delta))) "*/
	{
		this.nddValue = v;
		this.nddDelta = d;
	}
	
	/**
	 * Update.
	 *
	 * @param delta the delta
	 */
	public void update(int delta)
		/*: modifies "ndd_value", "ndd_delta" 
			ensures "ndd_value = old ndd_value + delta & ndd_delta = delta &
               (ALL nb. nb : Object.alloc &  nb : NumberDeltaDouble & nb ~= null & nb ~= this -->
               	(nb..ndd_value = nb..(old NumberDeltaDouble.ndd_value) & 
               	 nb..ndd_delta = nb..(old NumberDeltaDouble.ndd_delta))) "*/
	{
		this.nddDelta = delta;
		this.nddValue = this.nddValue + delta;
	}


	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = ndd_value"*/
	{
		// TODO Auto-generated method stub
		return this.nddValue;
	}
	
	public int getDelta()
	/*: ensures "result = ndd_delta"*/
	{
		return this.nddDelta;
	}
}
