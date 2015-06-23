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
 * The Class NormalDouble.
 */
public class NormalDouble{
	
	/** The value. */
	private int ndValue;
	
	/*: public encap specvar nd_value :: "int"; */
	
	/*: vardefs "nd_value == ndValue"; */
	
	/**
	 * Instantiates a new normal double.
	 *
	 * @param v the v
	 */
	public NormalDouble(int v)
	/*: modifies "nd_value" 
	     ensures "nd_value = v &
	                    (ALL nb. nb : Object.alloc &  nb : NormalDouble & nb ~= null & nb ~= this -->
	                    	nb..nd_value = nb..(old NormalDouble.nd_value)) "*/
	{
		this.ndValue = v;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = nd_value"*/
	{
		// TODO Auto-generated method stub
		return this.ndValue;
	}
}
