/***************************************************************
Project name: georeplication
Class file name: TrueFormula.java
Created at 11:14:02 AM by chengli

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

/**
 * @author chengli
 *
 */
public final class FalseFormula extends Formula {

	/**
	 * @param inputStr
	 */
	public FalseFormula(String inputStr) {
		super(inputStr);
		this.parseInput(super.getFormulaStr());
	}

	/* (non-Javadoc)
	 * @see util.weakestprecondtion.Formula#parseInput(java.lang.String)
	 */
	@Override
	public void parseInput(String str) {
		if(!str.equals("false")) {
			System.out.println("This must be a false formula");
			System.exit(0);
		}
	}

	/* (non-Javadoc)
	 * @see util.weakestprecondtion.Formula#populateFormula(runtimelogic.shadowoperationcreator.shadowoperation.DBOpEntry)
	 */
	@Override
	public String populateFormula(DBOpEntry dbOp) {
		return super.getFormulaStr();
	}

	/* (non-Javadoc)
	 * @see util.weakestprecondtion.Formula#isCrdtObjTouchThisFormula(runtimelogic.shadowoperationcreator.shadowoperation.DBOpEntry)
	 */
	@Override
	public boolean isCrdtObjTouchThisFormula(DBOpEntry dbOp) {
		return false;
	}

	/* (non-Javadoc)
	 * @see util.weakestprecondtion.Formula#toString()
	 */
	@Override
	public String toString() {
		return super.getFormulaStr();
	}

}
