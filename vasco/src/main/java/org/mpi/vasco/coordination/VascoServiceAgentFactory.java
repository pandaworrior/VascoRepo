/*******************************************************************************
 * Copyright (c) 2015 Dependable Cloud Group and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dependable Cloud Group - initial API and implementation
 *
 * Creator:
 *     Cheng Li
 *
 * Contact:
 *     chengli@mpi-sws.org    
 *******************************************************************************/
package org.mpi.vasco.coordination;

/**
 * A factory for creating VascoServiceAgent objects,
 * and defining all needed config parameters.
 */
public class VascoServiceAgentFactory {
	
	/** The response waiting time in mill seconds. 
	 * Normally set to the largest rtt latency between two data centers
	 * */
	public static int RESPONSE_WAITING_TIME_IN_MILL_SECONDS = 1;
	
	/**
	 * Creates a new VascoServiceAgent object.
	 *
	 * @return the vasco service agent
	 */
	public VascoServiceAgent createVascoServiceAgent(){
		return null;
	}

	public static int getRESPONSE_WAITING_TIME_IN_MILL_SECONDS() {
		return RESPONSE_WAITING_TIME_IN_MILL_SECONDS;
	}

	public static void setRESPONSE_WAITING_TIME_IN_MILL_SECONDS(
			int rESPONSE_WAITING_TIME_IN_MILL_SECONDS) {
		RESPONSE_WAITING_TIME_IN_MILL_SECONDS = rESPONSE_WAITING_TIME_IN_MILL_SECONDS;
	}
}
