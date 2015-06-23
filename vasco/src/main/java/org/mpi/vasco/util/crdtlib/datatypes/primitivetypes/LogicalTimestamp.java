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
package org.mpi.vasco.util.crdtlib.datatypes.primitivetypes;

import org.mpi.vasco.txstore.util.LogicalClock;

// TODO: Auto-generated Javadoc
/**
 * The Class LogicalTimestamp.
 */
public class LogicalTimestamp implements Comparable<LogicalTimestamp>{

	/** The dc num. */
	int dcNum;
	
	long blue;
	
	/** The dc count array. */
	long[] dcCountArray;
	
	/**
	 * Instantiates a new logical timestamp.
	 *
	 * @param dcN the dc n
	 */
	public LogicalTimestamp(int dcN){
		this.dcNum = dcN;
		this.blue = 0;
		dcCountArray = new long[this.dcNum + 1];
		for(int i = 0 ; i < this.dcNum + 1; i++){
			dcCountArray[i] = 0;
		}
	}
	
	public LogicalTimestamp(LogicalClock lc) {
		this.dcNum = lc.getDcEntries().length;
		this.blue = lc.getBlueCount();
		this.dcCountArray = lc.getDcEntries();
	}
	
	/**
	 * Gets the timestamp array.
	 *
	 * @return the timestamp array
	 */
	public long[] getTimestampArray(){
		return dcCountArray;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	/**
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * @param lts
	 * @return
	 */
	@Override
	public int compareTo(LogicalTimestamp lts) {
		boolean isSmallerThan = false;
		boolean isLargerThan = false;
		for(int i = 0; i < this.dcNum + 1; i++){
			if(dcCountArray[i] < lts.getTimestampArray()[i]){
				isSmallerThan = true;
			}else if(dcCountArray[i] >= lts.getTimestampArray()[i]){
				isLargerThan = true;
			}
		}
		if(isSmallerThan && isLargerThan){
			return -1;
		}else if(isSmallerThan){
			return 0;
		}else{
			return 1;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @see java.lang.Object#toString()
	 * @return
	 */
	public String toString(){
		String str = this.getClass().toString() + " value ";
		for(int i = 0; i < this.dcNum; i++){
			str += this.dcCountArray[i] + "-";
		}
		str += this.dcCountArray[this.dcNum];
		return str;
	}
	
	public String getValueString() {
		String str = "" + this.blue;
		for(int i = 0; i < this.dcNum; i++){
			str += "-" + this.dcCountArray[i];
		}
		return str;
	}

}
