/***************************************************************
Project name: georeplication
Class file name: RuntimeFingerPrintGenerator.java
Created at 8:22:59 PM by chengli

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

package org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.mpi.vasco.util.debug.Debug;
/**
 * The Class RuntimeFingerPrintGenerator.
 *
 * @author chengli
 */
public class RuntimeFingerPrintGenerator {
	
	/** The message digestor. */
	MessageDigest messageDigestor = null;
	
	/**
	 * Initialize message digestor.
	 */
	public RuntimeFingerPrintGenerator() {
		Debug.println("Message Digestor for runtime fingerprint generator is initialized!");
		try {
			messageDigestor = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the message digestor.
	 *
	 * @return the message digestor
	 */
	private MessageDigest getMessageDigestor() {
		return this.messageDigestor;
	}
	
	/**
	 * Compute finger print.
	 *
	 * @param shdOp the shd op
	 * @return the list
	 */
	public List<String> computeFingerPrint(ShadowOperation shdOp) {
		List<String> fingerPrints = new ArrayList<String>();
		ArrayList<DBOpEntry> operationList = shdOp.getOperationList();
		for(int i = 0; i < operationList.size(); i++) {
			DBOpEntry dbOp = operationList.get(i);
			fingerPrints.add(dbOp.computeFingerPrint(this.getMessageDigestor()));
		}
		return fingerPrints;
	}
}
