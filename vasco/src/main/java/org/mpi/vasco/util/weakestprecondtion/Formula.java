/***************************************************************
Project name: georeplication
Class file name: Formula.java
Created at 11:01:57 AM by chengli

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

import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.DBOpEntry;

// TODO: Auto-generated Javadoc
/**
 * The Abstract Class Formula.
 *
 * @author chengli
 */
public abstract class Formula {
	
	/** The formula str. */
	private String formulaStr;
	
	/** The start of logic operator. */
	static char startOfLogicOperator = '(';
	
	/** The end of logic operator. */
	static char endOfLogicOperator = ')';
	
	/** The Constant formulaComponentDelimiter. */
	public final static String formulaComponentDelimiter = ".";
	
	/** The Constant escapeFormulaComponentDelimiter. */
	public final static String escapeFormulaComponentDelimiter = "\\.";
	
	/**
	 * Instantiates a new formula.
	 *
	 * @param inputStr the input str
	 */
	public Formula(String inputStr) {
		this.setFormulaStr(inputStr);
	}
	
	/**
	 * Parses the input.
	 *
	 * @param _str the _str
	 */
	public abstract void parseInput(String _str);
	
	/**
	 * Populate formula.
	 *
	 * @param dbOp the db op
	 * @return the string
	 */
	public abstract String populateFormula(DBOpEntry dbOp);
	
	/**
	 * Checks if is crdt obj touch this formula.
	 *
	 * @param dbOp the db op
	 * @return true, if is crdt obj touch this formula
	 */
	public abstract boolean isCrdtObjTouchThisFormula(DBOpEntry dbOp);
	
	/**
	 * Equal to.
	 *
	 * @param f the f
	 * @return true, if successful
	 */
	public boolean equalTo(Formula f) {
		return this.getFormulaStr().equals(f.getFormulaStr());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();
	
	/**
	 * Sets the formula str.
	 *
	 * @param formulaStr the formulaStr to set
	 */
	public void setFormulaStr(String formulaStr) {
		this.formulaStr = formulaStr.substring(formulaStr.indexOf(startOfLogicOperator)+1, formulaStr.indexOf(endOfLogicOperator));
	}
	
	/**
	 * Gets the formula str.
	 *
	 * @return the formulaStr
	 */
	public String getFormulaStr() {
		return formulaStr;
	}
}
