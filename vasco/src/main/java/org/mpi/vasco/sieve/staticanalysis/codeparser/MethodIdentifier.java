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
package org.mpi.vasco.sieve.staticanalysis.codeparser;

// TODO: Auto-generated Javadoc
/**
 * The Class MethodIdentifier.
 * 
 * @author chengli
 */
public class MethodIdentifier {
	
	/** The package name. */
	protected String packageName;

	/** The class name. */
	protected String className;

	/** The method name. */
	protected String methodName;

	/**
	 * Instantiates a new method identifier.
	 *
	 * @param pN the p n
	 * @param cN the c n
	 * @param mN the m n
	 */
	public MethodIdentifier(String pN, String cN, String mN) {
		this.packageName = pN;
		this.className = cN;
		this.methodName = mN;
	}

	/**
	 * Gets the package name.
	 *
	 * @return the package name
	 */
	public String getPackageName() {
		return this.packageName;
	}

	/**
	 * Gets the class name.
	 *
	 * @return the class name
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Gets the method name.
	 *
	 * @return the method name
	 */
	public String getMethodName() {
		return this.methodName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @see java.lang.Object#toString()
	 * @return
	 */
	public String toString() {
		String str = this.packageName + "-" + this.className + "-"
				+ this.methodName;
		return str;
	}
}
