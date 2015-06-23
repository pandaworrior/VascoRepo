/***************************************************************
Project name: georeplication
Class file name: FormulaFactory.java
Created at 2:12:07 PM by chengli

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

package org.mpi.vasco.util.weakestprecondtion;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating Formula objects.
 *
 * @author chengli
 */
public class FormulaFactory {
	
	/** The Constant INEQ_LTEQ. */
	public static final String INEQ_LTEQ = "<=";
	
	/** The Constant INEQ_LT. */
	public static final String INEQ_LT = "<";
	
	/** The Constant INEQ_NEQ. */
	public static final String INEQ_NEQ = "<>";
	
	/** The Constant INEQ_GTEQ. */
	public static final String INEQ_GTEQ = ">=";
	
	/** The Constant INEQ_GT. */
	public static final String INEQ_GT = ">";
	
	/**
	 * Creates a new Formula object.
	 *
	 * @param str the str
	 * @return the formula
	 */
	public static Formula createFormulaByString(String str) {
		if(str.contains("true")) {
			return new TrueFormula(str);
		}else {
			if(str.contains("false")) {
				return new FalseFormula(str);
			}else {
				return new InequalityFormula(str);
			}
		}
	}
}
