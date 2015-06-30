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
package org.mpi.vasco.coordination.protocols.messages;

import org.mpi.vasco.network.messages.MessageTagBase;


// TODO: Auto-generated Javadoc
/**
 * The Class MessageTags.
 */
public class MessageTags extends MessageTagBase{

	// /// PROCESSING LOCAL TRANSACTIONS

	/** The Constant LOCKREQ. */
	public final static int LOCKREQ = 50; // client to lock server
	// start
	/** The Constant LOCKREP. */
	public final static int LOCKREP = 51; // lock server to client

	/**
	 * Gets the string.
	 *
	 * @param i the i
	 * @return the string
	 */
	public final static String getString(int i) {
		switch (i) {
		case MessageTags.LOCKREQ:
			return "LOCKREQ";
		case MessageTags.LOCKREP:
			return "LOCKREP";
		default:

			throw new RuntimeException("Invalid message tag:  " + i);
		}
	}

}