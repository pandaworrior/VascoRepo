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

import org.mpi.vasco.coordination.protocols.util.Protocol;

/**
 * A factory for creating VascoServiceAgent objects,
 * and defining all needed config parameters.
 */
public class VascoServiceAgentFactory {
	
	/** The response waiting time in mill seconds. 
	 * Normally set to the largest rtt latency between two data centers
	 * */
	public static int RESPONSE_WAITING_TIME_IN_MILL_SECONDS = 1;
	
	public static int BIG_MAP_INITIAL_SIZE = 10000;
	
	public static int BIG_SET_INITIAL_SIZE = 10000;
	
	public static int SMALL_MAP_INITIAL_SIZE = 10;
	
	public static int SMALL_SET_INITIAL_SIZE = 10;
	
	/**
	 * Creates a new VascoServiceAgent object.
	 *
	 * @return the vasco service agent
	 */
	public static VascoServiceAgent createVascoServiceAgent(String memFile, int clientId){
		return new VascoServiceAgent(memFile, clientId);
	}

	public static int getRESPONSE_WAITING_TIME_IN_MILL_SECONDS() {
		return RESPONSE_WAITING_TIME_IN_MILL_SECONDS;
	}

	public static void setRESPONSE_WAITING_TIME_IN_MILL_SECONDS(
			int rESPONSE_WAITING_TIME_IN_MILL_SECONDS) {
		RESPONSE_WAITING_TIME_IN_MILL_SECONDS = rESPONSE_WAITING_TIME_IN_MILL_SECONDS;
	}
	
	/**
	 * Gets the protocol tag string.
	 *
	 * @param pType the type
	 * @return the protocol tag string
	 */
	public static String getProtocolTagString(int pType){
		switch(pType){
		case Protocol.PROTOCOL_ASYM:
			return "Asymm conflict";
		case Protocol.PROTOCOL_SYM:
			return "Symm_conflict";
			default:
				throw new RuntimeException("No such protocol type " + pType);
		}
	}
	
	public static void main(String[] args){
		if(args.length != 2){
			System.out.println("MessageHandlerServerSide [memshipFile] [id]");
			System.exit(-1);
		}
		
		String membershipFile = args[0];
		int myId = Integer.parseInt(args[1]);
		VascoServiceAgent vA = createVascoServiceAgent(membershipFile, myId);
		vA.getClient().test();
	}
}
