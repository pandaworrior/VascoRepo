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
 * The Class NumberDeltaFloat.
 */
public class NumberDeltaFloat{
	
	/** The value. */
	private int ndfValue;
	
	/** The delta. */
	private int ndfDelta;
	
	/*: public encap specvar ndf_value :: "int"; 
	     public encap specvar ndf_delta :: "int";*/
	
	/*: vardefs "ndf_value == ndfValue"; 
	    vardefs "ndf_delta == ndfDelta"*/
	
	/**
	 * Instantiates a new number delta float.
	 *
	 * @param v the v
	 */
	public NumberDeltaFloat(int v, int d)
	/*: modifies "ndf_value", "ndf_delta" 
    	ensures "ndf_value = v & ndf_delta = d & 
                   (ALL nb. nb : Object.alloc &  nb : NumberDeltaFloat & nb ~= null & nb ~= this -->
                   	(nb..ndf_value = nb..(old NumberDeltaFloat.ndf_value) & 
                   	 nb..ndf_delta = nb..(old NumberDeltaFloat.ndf_delta))) "*/
	{
		this.ndfValue = v;
		this.ndfDelta = d;
	}
	
	/**
	 * Update.
	 *
	 * @param delta the delta
	 */
	public void update(int delta)
	/*: modifies "ndf_value", "ndf_delta" 
			ensures "ndf_value = old ndf_value + delta & ndf_delta = delta &
               (ALL nb. nb : Object.alloc &  nb : NumberDeltaFloat & nb ~= null & nb ~= this -->
               	(nb..ndf_value = nb..(old NumberDeltaFloat.ndf_value) & 
               	 nb..ndf_delta = nb..(old NumberDeltaFloat.ndf_delta))) "*/
	{
		this.ndfDelta = delta;
		this.ndfValue = this.ndfValue + delta;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = ndf_value"*/
	{
		// TODO Auto-generated method stub
		return this.ndfValue;
	}
	
	public int getDelta()
	/*: ensures "result = ndf_delta"*/
	{
		return this.ndfDelta;
	}
}
