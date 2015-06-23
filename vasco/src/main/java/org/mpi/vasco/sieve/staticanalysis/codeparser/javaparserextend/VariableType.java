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
package org.mpi.vasco.sieve.staticanalysis.codeparser.javaparserextend;

import japa.parser.ast.type.Type;

// TODO: Auto-generated Javadoc
/**
 * The Class VariableType.
 */
public class VariableType {
	
	/** The type. */
	private Type type;
	
	/**
	 * Instantiates a new variable type.
	 *
	 * @param t the t
	 */
	public VariableType(Type t){
		this.type = t;
	}
	
	 /**
 	 * Gets the begin column.
 	 *
 	 * @return the begin column
 	 */
    public final int getBeginColumn() {
        return type.getBeginColumn();
    }

    /**
     * Gets the begin line.
     *
     * @return the begin line
     */
    public final int getBeginLine() {
        return type.getBeginLine();
    }

    /**
     * Gets the end column.
     *
     * @return the end column
     */
    public final int getEndColumn() {
        return type.getEndColumn();
    }

    /**
     * Gets the end line.
     *
     * @return the end line
     */
    public final int getEndLine() {
        return type.getEndLine();
    }
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public Type getType(){
		return this.type;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @see java.lang.Object#toString()
	 * @return
	 */
	public String toString(){
		return this.type.toString();
	}

}
