/*******************************************************************************
 * Copyright (c) 2013 Cheng Li.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Cheng Li - initial API and implementation
 * 
 * Contact:
 *     To distribute or use this code requires prior specific permission.
 *     In this case, please contact chengli@mpi-sws.org.
 ******************************************************************************/
package org.mpi.vasco.util.debug;

import java.io.FilenameFilter;
import java.io.File;

// TODO: Auto-generated Javadoc
/**
 * The Class LogFilter.
 */
public class LogFilter implements FilenameFilter { 

	/** The id. */
	String id;

	/**
	 * Instantiates a new log filter.
	 *
	 * @param id the id
	 */
	public LogFilter(String id) {
		this.id = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	/**
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 * @param dir
	 * @param name
	 * @return
	 */
	public boolean accept(File dir, String name) {
		return name.endsWith(id);
	}

}
