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
package org.mpi.vasco.sieve.staticanalysis.templatecreator.template;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.Statement;

import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.DatabaseDef;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseTable;

// TODO: Auto-generated Javadoc
/**
 * The Class InsertOperation.
 */
public class InsertOperation extends Operation{

	/**
	 * Instantiates a new insert operation.
	 *
	 * @param tN the t n
	 * @param dT the d t
	 */
	public InsertOperation(String tN, DatabaseTable dT) {
		super(tN, dT);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Gets the operation finger print.
	 *
	 * @return the finger print string
	 * @see staticanalysis.templatecreator.template.Operation#getOperationFingerPrint()
	 */
	@Override
	public String getOperationFingerPrint() {
		String identifierStr = super.getTableName() + "." + DatabaseDef.INSERT_OP_STR;
		try {
			byte[] identifierStrInBytes = identifierStr.getBytes("UTF-8");
			Operation.messageDigestor.update(identifierStrInBytes);
			byte[] res = messageDigestor.digest();
			return StringOperations.bytesToHex(res);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RuntimeException("You have trouble to generate a static identifier");
	}

}
