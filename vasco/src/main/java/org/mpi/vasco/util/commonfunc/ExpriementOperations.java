/***************************************************************
Project name: georeplication
Class file name: ExpriementOperations.java
Created at 10:53:21 PM by chengli

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

import java.sql.Timestamp;

/**
 * The Class ExpriementOperations.
 *
 * @author chengli
 */
public class ExpriementOperations {
	
	/**
	 * Pause execution.
	 *
	 * @param milliseconds the milliseconds
	 */
	public static void pauseExecution(long milliseconds) {
		try {
			java.util.Date date= new java.util.Date();
			System.out.println(new Timestamp(date.getTime()) + ": sleep now");
			Thread.sleep(milliseconds);
			System.out.println(new Timestamp(date.getTime()) + ": awaked");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
