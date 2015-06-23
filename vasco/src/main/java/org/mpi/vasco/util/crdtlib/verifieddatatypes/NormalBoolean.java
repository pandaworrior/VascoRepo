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
 * The Class NormalBoolean.
 */
public class NormalBoolean{
	
	/** The value. */
	private int nbValue;
	
	/*: public encap specvar nb_value :: "int"; */
	
	/*: vardefs "nb_value == nbValue"; */
	
	/**
	 * Instantiates a new normal boolean.
	 *
	 * @param v the v
	 */
	public NormalBoolean(int v)
	/*: modifies "nb_value" 
	     ensures "nb_value = v &
	                    (ALL nb. nb : Object.alloc &  nb : NormalBoolean & nb ~= null & nb ~= this -->
	                    	nb..nb_value = nb..(old NormalBoolean.nb_value)) "*/
	{
		this.nbValue = v;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getValue() 
	/*: ensures "result = nb_value"*/
	{
		return this.nbValue;
	}
	
}
