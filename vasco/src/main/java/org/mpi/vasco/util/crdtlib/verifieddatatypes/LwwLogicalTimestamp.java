package util.crdtlib.verifieddatatypes;
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
 * The Class LogicalTimestamp.
 */
public class LwwLogicalTimestamp{

	/** The counter. */
	private int ts;
	
	/*: public encap specvar lts_ts :: "int"; 
	 */
	
	
	/*: vardefs "lts_ts == ts" 
	 */
	
	/**
	 * Instantiates a new logical timestamp.
	 *
	 * @param dcN the dc n
	 */
	public LwwLogicalTimestamp(int t)
	/*: modifies "lts_ts"
	     ensures "lts_ts = t"*/
	{
		ts = t;
	}
	
	public boolean isSmallerThan(int lt) 
	/*: ensures "(lts_ts <= lt --> result = True) &
	     			    (lts_ts > lt --> result = False) "
	 */
	{
		if(ts <= lt) {
			return true;
		}else {
			return false;
		}
	}
	
	public void update(int lt) 
	/*: modifies "lts_ts"
	     ensures "lts_ts = lt"
	 */
	{
		ts = lt;
	}
	
	//this is for update without knowledge about order
	public void updateComp(int lt) 
	/*: modifies "lts_ts"
	     ensures "(old lts_ts <= lt --> lts_ts = lt) &
	     			    (old lts_ts > lt --> lts_ts = old lts_ts) "
	 */
	{
		if(ts <= lt) {
			ts = lt;
		}
	}
	
	public int getValue() 
	/*: ensures "result = lts_ts "*/
	{
		return ts;
	}

}
