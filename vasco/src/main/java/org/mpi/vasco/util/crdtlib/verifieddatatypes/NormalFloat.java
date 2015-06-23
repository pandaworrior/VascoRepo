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
 * The Class NormalFloat.
 */
public class NormalFloat{
	
	/** The value. */
	private int nfValue;
	
	/*: public encap specvar nf_value :: "int"; */
	
	/*: vardefs "nf_value == nfValue"; */
	
	/**
	 * Instantiates a new normal float.
	 *
	 * @param v the v
	 */
	public NormalFloat(int v)
	/*: modifies "nf_value" 
	     ensures "nf_value = v &
	                    (ALL nb. nb : Object.alloc &  nb : NormalFloat & nb ~= null & nb ~= this -->
	                    	nb..nf_value = nb..(old NormalFloat.nf_value)) "*/
	{
		this.nfValue = v;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = nf_value"*/
	{
		// TODO Auto-generated method stub
		return this.nfValue;
	}
}
