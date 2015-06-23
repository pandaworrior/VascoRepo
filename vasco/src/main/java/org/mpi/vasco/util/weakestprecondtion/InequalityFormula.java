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
package org.mpi.vasco.util.weakestprecondtion;

import java.util.List;

import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.DBOpEntry;
import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.PrimitiveType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.DatabaseDef;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class Formula.
 */
public class InequalityFormula extends Formula{
	
	/** The table name. */
	String tableName;
	
	/** The field name. */
	String fieldName;
	
	/** The field input name. */
	String fieldInputName;
	
	/** The logic operator. */
	String logicOperator;
	
	/** The right hand value. */
	String rightHandValue;
	
	/**
	 * Instantiates a new inequality formula.
	 *
	 * @param inputStr the input str
	 */
	public InequalityFormula(String inputStr) {
		super(inputStr);
		this.parseInput(super.getFormulaStr());
	}
	
	/* (non-Javadoc)
	 * @see util.weakestprecondtion.Formula#parseInput(java.lang.String)
	 */
	@Override
	public void parseInput(String str) {
		if(str.indexOf(Formula.formulaComponentDelimiter) == -1) {
			System.out.println("Inequality formula is not in the correct format " + str);
			System.exit(-1);
		}else {
			String[] formulaStrs = str.split(Formula.escapeFormulaComponentDelimiter);
			if(formulaStrs == null ||
					formulaStrs.length < 5) {
				System.out.println("Inequality formula doesn't have enough length " + str);
				System.exit(-1);
			}else {
				this.setTableName(formulaStrs[0]);
				this.setFieldName(formulaStrs[1]);
				this.setFieldInputName(formulaStrs[2]);
				this.setLogicOperator(formulaStrs[3]);
				this.setRightHandValue(formulaStrs[4]);
			}
		}
	}

	/**
	 * Gets the logic operator.
	 *
	 * @return the logic operator
	 */
	public String getLogicOperator() {
		return logicOperator;
	}

	/**
	 * Sets the logic operator.
	 *
	 * @param logicOperator the new logic operator
	 */
	public void setLogicOperator(String logicOperator) {
		this.logicOperator = logicOperator;
	}

	/* (non-Javadoc)
	 * @see util.weakestprecondtion.Formula#isCrdtObjTouchThisFormula(runtimelogic.shadowoperationcreator.shadowoperation.DBOpEntry)
	 */
	@Override
	public boolean isCrdtObjTouchThisFormula(DBOpEntry dbOp) {
		// TODO Auto-generated method stub
		if(dbOp.getDbTableName().equals(this.getTableName())) {
			if(dbOp.getOpType() == DatabaseDef.INSERT ||
					dbOp.getOpType() == DatabaseDef.UNIQUEINSERT) {
				List<PrimitiveType> primaryKeys = dbOp.getPrimaryKeys();
				for(PrimitiveType pk : primaryKeys) {
					if(pk.getDataName().equals(this.getFieldName())) {
						return true;
					}
				}
			}
			List<PrimitiveType> attributes = dbOp.getNormalAttributes();
			for(PrimitiveType ar : attributes) {
				if(ar.getDataName().equals(this.getFieldName())) {
					return true;
				}
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see util.weakestprecondtion.Formula#populateFormula(runtimelogic.shadowoperationcreator.shadowoperation.DBOpEntry)
	 */
	@Override
	public String populateFormula(DBOpEntry dbOp) {
		if(!this.isCrdtObjTouchThisFormula(dbOp)) {
			Debug.println("INEQ CHECK --> true");
			return "true";
		}
		StringBuilder returnStrBuilder = new StringBuilder("");
		/*if(dbOp.getOpType() == DatabaseDef.INSERT ||
				dbOp.getOpType() == DatabaseDef.UNIQUEINSERT) {
			List<PrimitiveType> primaryKeys = dbOp.getPrimaryKeys();
			for(PrimitiveType pk : primaryKeys) {
				if(pk.getDataName().equals(this.getFieldName())) {
					String inputValue = pk.getValueByName(this.getFieldInputName());
					Debug.println("INEQ CHECK --> get " + this.getFieldName() + " value " + inputValue);
					if(returnStrBuilder.length() == 0) {
						returnStrBuilder.append(inputValue);
						returnStrBuilder.append(this.getLogicOperator());
						returnStrBuilder.append(this.getRightHandValue());
					}else {
						returnStrBuilder.append(" && ");
						returnStrBuilder.append(inputValue);
						returnStrBuilder.append(this.getLogicOperator());
						returnStrBuilder.append(this.getRightHandValue());
					}
				}
			}
		}
		List<PrimitiveType> attributes = dbOp.getNormalAttributes();
		for(PrimitiveType ar : attributes) {
			if(ar.getDataName().equals(this.getFieldName())) {
				String inputValue = ar.getValueByName(this.getFieldInputName());
				Debug.println("INEQ CHECK --> get " + this.getFieldName() + " value " + inputValue);
				if(returnStrBuilder.length() == 0) {
					returnStrBuilder.append(inputValue);
					returnStrBuilder.append(this.getLogicOperator());
					returnStrBuilder.append(this.getRightHandValue());
				}else {
					returnStrBuilder.append(" && ");
					returnStrBuilder.append(inputValue);
					returnStrBuilder.append(this.getLogicOperator());
					returnStrBuilder.append(this.getRightHandValue());
				}
			}
		}*/
		PrimitiveType pt = dbOp.getAttributeByGivenName(this.getFieldName());
		String inputValue = pt.getValueByName(this.getFieldInputName());
		Debug.println("INEQ CHECK --> get " + this.getFieldName() + " value " + inputValue); 
		returnStrBuilder.append(inputValue);
		returnStrBuilder.append(this.getLogicOperator());
		returnStrBuilder.append(this.getRightHandValue());
		Debug.println("INEQ CHECK --> " + returnStrBuilder.toString());
		return returnStrBuilder.toString();
	}

	/**
	 * Gets the table name.
	 *
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Sets the table name.
	 *
	 * @param tName the new table name
	 */
	public void setTableName(String tName) {
		int index = tName.indexOf("Table");
		if(index != -1) {
			this.tableName = tName.substring(0, index);
		}else {
			index = tName.indexOf("Record");
			if(index != -1) {
				this.tableName =tName.substring(0, index);
			}else {
				this.tableName = tName;		
			}
		}
	}

	/**
	 * Gets the field name.
	 *
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Sets the field name.
	 *
	 * @param fName the new field name
	 */
	public void setFieldName(String fName) {
		int index = fName.indexOf("Record");
		if(index != -1) {
			this.fieldName = fName.substring(index+7);
		}else {
			this.fieldName = fName;
		}
	}

	/**
	 * Gets the field input name.
	 *
	 * @return the fieldInputName
	 */
	public String getFieldInputName() {
		return fieldInputName;
	}

	/**
	 * Sets the field input name.
	 *
	 * @param fInputName the new field input name
	 */
	public void setFieldInputName(String fInputName) {
		this.fieldInputName = fInputName;
	}

	/**
	 * Gets the right hand value.
	 *
	 * @return the rightHandValue
	 */
	public String getRightHandValue() {
		return rightHandValue;
	}

	/**
	 * Sets the right hand value.
	 *
	 * @param rHandValue the new right hand value
	 */
	public void setRightHandValue(String rHandValue) {
		this.rightHandValue = rHandValue;
	}

	/* (non-Javadoc)
	 * @see util.weakestprecondtion.Formula#toString()
	 */
	@Override
	public String toString() {
		return this.getTableName() + "." + this.getFieldName() + "." + this.getFieldInputName() + "." + this.getLogicOperator() + "." + this.getRightHandValue();
	}
}
