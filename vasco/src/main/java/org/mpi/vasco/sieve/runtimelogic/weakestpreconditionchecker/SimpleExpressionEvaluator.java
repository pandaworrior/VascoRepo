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
public class SimpleExpressionEvaluator {
	/* 
     * Returns the boolean value of a string script e.g. " 100 >= 200" evaluates to false
     * all formulas must be connected with &&  
     */  
	
	/** The manager. */
	public static ScriptEngineManager manager = new ScriptEngineManager();  
	
	/** The engine. */
	public static ScriptEngine engine = manager.getEngineByName("js");  
    /**
	 * Eval bool expression.
	 *
	 * @param script the script
	 * @return true, if successful
	 */
	public static boolean evalBoolExpression(String script) {  
        Object result = null;  
        try {  
            result = engine.eval(script);  
        } catch (ScriptException e) {  
            e.printStackTrace();  
        }  
          
        return ((Boolean)result).booleanValue();  
    }  
}
