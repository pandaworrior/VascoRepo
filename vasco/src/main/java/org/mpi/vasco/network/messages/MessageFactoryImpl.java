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
package org.mpi.vasco.network.messages;

// TODO: Auto-generated Javadoc
/**
 * The Interface MessageFactoryImpl.
 */
public interface MessageFactoryImpl {
	
	/**
	 * From bytes.
	 *
	 * @param bytes the bytes
	 * @return the message base
	 */
	public MessageBase fromBytes(byte[] bytes);

}
