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

/**
 * The Class CodeNodeIdentifier.
 * 
 * @author chengli
 */
public class CodeNodeIdentifier extends MethodIdentifier {

	/** The begin line. */
	private int beginLine;

	/** The begin column. */
	private int beginColumn;

	/** The end line. */
	private int endLine;

	/** The end column. */
	private int endColumn;

	/**
	 * Instantiates a new code node identifier.
	 * 
	 * @param pN
	 *            the package name
	 * @param cN
	 *            the class name
	 * @param mN
	 *            the method name
	 * @param bL
	 *            the begin line
	 * @param bC
	 *            the begin column
	 * @param eL
	 *            the end line
	 * @param eC
	 *            the end column
	 */
	public CodeNodeIdentifier(String pN, String cN, String mN, int bL, int bC,
			int eL, int eC) {
		super(pN, cN, mN);
		this.setBeginLine(bL);
		this.setBeginColumn(bC);
		this.setEndLine(eL);
		this.setEndColumn(eC);
	}

	/**
	 * Gets the begin line.
	 *
	 * @return the beginLine
	 */
	public int getBeginLine() {
		return beginLine;
	}

	/**
	 * Sets the begin line.
	 *
	 * @param beginLine the beginLine to set
	 */
	public void setBeginLine(int beginLine) {
		this.beginLine = beginLine;
	}

	/**
	 * Gets the begin column.
	 *
	 * @return the beginColumn
	 */
	public int getBeginColumn() {
		return beginColumn;
	}

	/**
	 * Sets the begin column.
	 *
	 * @param beginColumn the beginColumn to set
	 */
	public void setBeginColumn(int beginColumn) {
		this.beginColumn = beginColumn;
	}

	/**
	 * Gets the end line.
	 *
	 * @return the endLine
	 */
	public int getEndLine() {
		return endLine;
	}

	/**
	 * Sets the end line.
	 *
	 * @param endLine the endLine to set
	 */
	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	/**
	 * Gets the end column.
	 *
	 * @return the endColumn
	 */
	public int getEndColumn() {
		return endColumn;
	}

	/**
	 * Sets the end column.
	 *
	 * @param endColumn the endColumn to set
	 */
	public void setEndColumn(int endColumn) {
		this.endColumn = endColumn;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/**
	 * @see staticanalysis.codeparser.MethodIdentifier#toString()
	 * @return the str
	 */
	public String toString() {
		String str = this.packageName + "-" + this.className + "-"
				+ this.methodName + "-" + Integer.toString(this.beginLine)
				+ "-" + Integer.toString(this.beginColumn) + "-"
				+ Integer.toString(this.endLine) + "-"
				+ Integer.toString(this.endColumn);
		//String str = Integer.toString(this.beginLine);
		return str;
	}
	
	/**
	 * Equals.
	 *
	 * @param cdId the cd id
	 * @return true, if successful
	 */
	public boolean equals(CodeNodeIdentifier cdId){
		if(super.getPackageName().equals(cdId.getPackageName()) && super.getClassName().equals(cdId.getClass()) &&
				super.getMethodName().equals(cdId.getMethodName()) &&
				this.getBeginLine() == cdId.getBeginLine() &&
				this.getBeginColumn() == cdId.getBeginColumn() &&
				this.getEndLine() == cdId.getEndLine() &&
				this.getEndColumn() == cdId.getEndColumn()){
			return true;
		}else{
			return false;
		}
	}
}
