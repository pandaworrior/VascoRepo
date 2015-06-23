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
 * The Class NormalDateTime.
 */
public class NormalDateTime{
	
	/** The value. */
	private int ndtValue;
	
	/*: public encap specvar ndt_value :: "int"; */
	
	/*: vardefs "ndt_value == ndtValue"; */
	
	/**
	 * Instantiates a new normal date time.
	 *
	 * @param v the v
	 */
	public NormalDateTime(int v)
	/*: modifies "ndt_value" 
	     ensures "ndt_value = v &
	                    (ALL nb. nb : Object.alloc &  nb : NormalDateTime & nb ~= null & nb ~= this -->
	                    	nb..ndt_value = nb..(old NormalDateTime.ndt_value)) "*/
	{
		this.ndtValue = v;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = ndt_value"*/
	{
		// TODO Auto-generated method stub
		return this.ndtValue;
	}
}
