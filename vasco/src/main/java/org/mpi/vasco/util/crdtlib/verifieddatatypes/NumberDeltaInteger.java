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
 * The Class NumberDeltaInteger.
 */
public class NumberDeltaInteger{
	
	/** The value. */
	private int ndiValue;
	
	/** The delta. */
	private int ndiDelta;
	
	/*: public encap specvar ndi_value :: "int"; 
	     public encap specvar ndi_delta :: "int";*/
	
	/*: vardefs "ndi_value == ndiValue"; 
	    vardefs "ndi_delta == ndiDelta"*/
	
	/**
	 * Instantiates a new number delta integer.
	 *
	 * @param v the v
	 */
	public NumberDeltaInteger(int v, int d)
	/*: modifies "ndi_value", "ndi_delta" 
    	ensures "ndi_value = v & ndi_delta = d & 
                   (ALL nb. nb : Object.alloc &  nb : NumberDeltaInteger & nb ~= null & nb ~= this -->
                   	(nb..ndi_value = nb..(old NumberDeltaInteger.ndi_value) & 
                   	 nb..ndi_delta = nb..(old NumberDeltaInteger.ndi_delta))) "*/
	{
		this.ndiValue = v;
		this.ndiDelta = d;
	}
	
	/**
	 * Update.
	 *
	 * @param delta the delta
	 */
	public void update(int delta)
	/*: modifies "ndi_value", "ndi_delta" 
			ensures "ndi_value = old ndi_value + delta & ndi_delta = delta &
               (ALL nb. nb : Object.alloc &  nb : NumberDeltaInteger & nb ~= null & nb ~= this -->
               	(nb..ndi_value = nb..(old NumberDeltaInteger.ndi_value) & 
               	 nb..ndi_delta = nb..(old NumberDeltaInteger.ndi_delta))) "*/
	{
		this.ndiDelta = delta;
		this.ndiValue = this.ndiValue + delta;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = ndi_value"*/
	{
		// TODO Auto-generated method stub
		return this.ndiValue;
	}
	
	public int getDelta()
	/*: ensures "result = ndi_delta"*/
	{
		return this.ndiDelta;
	}
}
