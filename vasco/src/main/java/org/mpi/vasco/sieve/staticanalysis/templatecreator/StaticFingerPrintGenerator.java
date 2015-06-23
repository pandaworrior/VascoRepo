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
package org.mpi.vasco.sieve.staticanalysis.templatecreator;
import java.util.List;

import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.sieve.staticanalysis.templatecreator.template.Operation;

/**
 * The Class StaticFingerPrintGenerator.
 */
public class StaticFingerPrintGenerator {
	
	/** The finger print delimiter. */
	public static String fingerPrintDelimiter = ".";
	
	public static String escapeFingerPrintDelimiter = "\\.";
	
	/** The opener of finger print. */
	public static String openerOfFingerPrint = "fingerprint:";
	

	/**
	 * Compute finger print.
	 *
	 * @param opList the op list
	 * @return the string
	 */
	public static String computeFingerPrint(List<Operation> opList){
		String identifierStr = "";
		for(int i = 0; i < opList.size(); i++){
			Operation op = opList.get(i);
			identifierStr += op.getOperationFingerPrint() + fingerPrintDelimiter;
		}
		if(identifierStr.length() > 0){
			identifierStr = openerOfFingerPrint + identifierStr.substring(0, identifierStr.length()-1);
		}
		Debug.println("template identifier string: " + identifierStr);
		return identifierStr;
	}
}
