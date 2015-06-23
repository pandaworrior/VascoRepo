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
package org.mpi.vasco.sieve.runtimelogic.weakestpreconditionchecker;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleExpressionEvaluator. The simple expression evaluator
 * is to return true or false after evaluating a simple logic formula.
 * We call it simple since we might have to implement a complicated
 * formula evaluator. But now let's just use it.
 * @author chengli
 */
public class SimpleInequalityExpressionEvaluator {
	
	public static String GREAT_THAN_EQ = ">=";
	public static String GREAT_THAN = ">";
	public static String LESS_THAN_EQ = "<=";
	public static String LESS_THAN = "<";
	public static String NOT_EQ = "<>";
	
    /**
	 * Eval bool expression.
	 *
	 * @param script the script
	 * @return true, if successful
	 */
	public static boolean evalBoolExpression(String script) {  
		if(script.contains(" ")) {
			script = script.replaceAll("\\s+", "");
		}
		int index = script.indexOf(GREAT_THAN_EQ);
		if(index != -1) {
			int a = Integer.parseInt(script.substring(0, index));
			int b = Integer.parseInt(script.substring(index+2));
			return evalGreatThanEq(a,b);
		}else {
			index = script.indexOf(NOT_EQ);
			if(index != -1) {
				int a = Integer.parseInt(script.substring(0, index));
				int b = Integer.parseInt(script.substring(index+2));
				return evalGreatThan(a,b);
			}else {
				index = script.indexOf(LESS_THAN_EQ);
				if(index != -1) {
					int a = Integer.parseInt(script.substring(0, index));
					int b = Integer.parseInt(script.substring(index+2));
					return evalLessThanEq(a,b);
				}else {
					index = script.indexOf(LESS_THAN);
					if(index != -1) {
						int a = Integer.parseInt(script.substring(0, index));
						int b = Integer.parseInt(script.substring(index+1));
						return evalLessThan(a,b);
					}else {
						index = script.indexOf(GREAT_THAN);
						if(index != -1) {
							int a = Integer.parseInt(script.substring(0, index));
							int b = Integer.parseInt(script.substring(index+1));
							return evalNotEq(a,b);
						}else {
							throw new RuntimeException("No such in eq sign found " + script);
						}
					}
				}
			}
		}
    }  
	
	public static boolean evalGreatThanEq(int a, int b) {
		if(a >= b) {
			return true;
		}
		return false;
	}
	
	public static boolean evalGreatThan(int a, int b) {
		if(a > b) {
			return true;
		}
		return false;
	}
	
	public static boolean evalLessThanEq(int a, int b) {
		if(a <= b) {
			return true;
		}
		return false;
	}
	
	public static boolean evalLessThan(int a, int b) {
		if(a < b) {
			return true;
		}
		return false;
	}
	
	public static boolean evalNotEq(int a, int b) {
		if(a != b) {
			return true;
		}
		return false;
	}
	
}
