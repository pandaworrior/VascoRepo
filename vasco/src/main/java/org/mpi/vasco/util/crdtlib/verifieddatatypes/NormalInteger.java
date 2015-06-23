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
 * The Class NormalInteger.
 */
public class NormalInteger{
	
	/** The value. */
	private int niValue;
	
	/*: public encap specvar ni_value :: "int"; */
	
	/*: vardefs "ni_value == niValue"; */
	
	/**
	 * Instantiates a new normal integer.
	 *
	 * @param v the v
	 */
	public NormalInteger(int v)
	/*: modifies "ni_value" 
	     ensures "ni_value = v &
	                    (ALL nb. nb : Object.alloc &  nb : NormalInteger & nb ~= null & nb ~= this -->
	                    	nb..ni_value = nb..(old NormalInteger.ni_value)) "*/
	{
		this.niValue = v;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = ni_value"*/
	{
		// TODO Auto-generated method stub
		return this.niValue;
	}
}
