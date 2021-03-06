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
/*
import replicationlayer.core.util.Debug;
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mpi.vasco.coordination.membership;

// TODO: Auto-generated Javadoc
/**
 * The Enum Role.
 *
 * @author chengli
 */
public enum Role {
    
    /** The lockserver. */
    LOCKSERVER, 
    /** The lockclient. */
    LOCKCLIENT;
	// the remaining are more "application" side
	// they should not be included
        //USER, APPPROXY;
}

class RoleFactory{
	public static String getRoleString(Role r){
		switch(r){
		case LOCKSERVER:
			return "Lock Server";
		case LOCKCLIENT:
			return "Lock Client";
			default:
				throw new RuntimeException("Invalid role " + r);
		}
		
	}
}