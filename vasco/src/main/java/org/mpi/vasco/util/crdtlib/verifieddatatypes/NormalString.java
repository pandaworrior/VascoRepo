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
 * The Class NormalString.
 */
public class NormalString{
	
	/** The value. */
	private int nsValue;
	
	/*: public encap specvar ns_value :: "int"; */
	
	/*: vardefs "ns_value == nsValue"; */
	
	/**
	 * Instantiates a new normal string.
	 *
	 * @param v the v
	 */
	public NormalString(int v)
	/*: modifies "ns_value" 
	     ensures "ns_value = v &
	                    (ALL nb. nb : Object.alloc &  nb : NormalString & nb ~= null & nb ~= this -->
	                    	nb..ns_value = nb..(old NormalString.ns_value)) "*/
	{
		this.nsValue = v;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = ns_value"*/
	{
		// TODO Auto-generated method stub
		return this.nsValue;
	}
}
