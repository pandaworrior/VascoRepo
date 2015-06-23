/***************************************************************
Project name: georeplication
Class file name: TimeMeasurement.java
Created at 2:44:03 PM by chengli

Copyright (c) 2014 chengli.
All rights reserved. This program and the accompanying materials
are made available under the terms of the GNU Public License v2.0
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/gpl-2.0.html

Contributors:
    chengli - initial API and implementation

Contact:
    To distribute or use this code requires prior specific permission.
    In this case, please contact chengli@mpi-sws.org.
****************************************************************/

package org.mpi.vasco.util.commonfunc;

// TODO: Auto-generated Javadoc
/**
 * The Class TimeMeasurement.
 *
 * @author chengli
 */
public class TimeMeasurement {
	
	/**
	 * Gets the current time in ns.
	 *
	 * @return the current time in ns
	 */
	public static long getCurrentTimeInNS() {
		return System.nanoTime();
	}
	
	/**
	 * Compute latency in ms.
	 *
	 * @param startTime the start time
	 * @return the double
	 */
	public static double computeLatencyInMS(long startTime) {
		long endTime = System.nanoTime();
		double latency = (endTime - startTime) * 0.000001;
		return latency;
	}

}
