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
 * This class is to represent a weakest precondition.
 */
package org.mpi.vasco.util.weakestprecondtion;

import java.util.ArrayList;
import java.util.List;

import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.DBOpEntry;
import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class WeakestPrecondition.
 */
public class WeakestPrecondition {
	
	/** The formula list. */
	List<Formula> formulaList;
	
	/** The delimiter. */
	static String delimiter = " ";
	
	String simplifiedOpName;
	
	public String getSimplifiedOpName() {
		return simplifiedOpName;
	}

	public void setSimplifiedOpName(String simplifiedOpName) {
		this.simplifiedOpName = simplifiedOpName;
	}

	/**
	 * Instantiates a new weakest precondition.
	 *
	 * @param wpStr the wp str
	 */
	public WeakestPrecondition(String wpStr, String simOpName){
		this.formulaList = new ArrayList<Formula>();
		String[] formStrs = wpStr.split(delimiter);
		for(int i = 0; i < formStrs.length; i++){
			Formula f = FormulaFactory.createFormulaByString(formStrs[i]);
			this.formulaList.add(f);
		}
		this.setSimplifiedOpName(simOpName);
	}

	/**
	 * Gets the formula list.
	 *
	 * @return the formula list
	 */
	public List<Formula> getFormulaList() {
		return formulaList;
	}

	/**
	 * Sets the formula list.
	 *
	 * @param formulaList the new formula list
	 */
	public void setFormulaList(List<Formula> formulaList) {
		this.formulaList = formulaList;
	}
	
	/**
	 * Equal to.
	 *
	 * @param wp the wp
	 * @return true, if successful
	 */
	public boolean equalTo(WeakestPrecondition wp) {
		if(this.getFormulaList().size() != wp.getFormulaList().size()) {
			return false;
		}else {
			for(int i =0; i < this.getFormulaList().size(); i++) {
				if(!this.getFormulaList().get(i).equalTo(wp.getFormulaList().get(i)))
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Populate all formulas.
	 *
	 * @param dbOp the db op
	 * @return the string
	 */
	public String populateAllFormulas(DBOpEntry dbOp) {
		StringBuilder resultStrBuilder = new StringBuilder("");
		for(int i = 0; i < this.getFormulaList().size(); i++) {
			if(i == 0) {
				resultStrBuilder.append( this.getFormulaList().get(i).populateFormula(dbOp));
			}else {
				resultStrBuilder.append(" && ");
				resultStrBuilder.append(this.getFormulaList().get(i).populateFormula(dbOp));
			}
		}
		Debug.println("populate weakest precondition " + resultStrBuilder.toString() + " \ndbOp " + dbOp.toString());
		return resultStrBuilder.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * To string.
	 *
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String _str = "";
		for(Formula f : formulaList){
			_str += f.toString() + " ";
		}
		return _str;
	}

}
