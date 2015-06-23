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
package org.mpi.vasco.sieve.staticanalysis.datastructures.regularexpression;

// TODO: Auto-generated Javadoc
/**
 * The Class SimpleRegularExpression.
 */
public class SimpleRegularExpression {

	/**
	 * Simple union.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the string
	 */
	public static String simpleUnion(String a, String b) {
		if (a == b) {
			return a;
		} else {
			if (a == RegularExpressionDef.Empty) {
				return b;
			} else {
				if (b == RegularExpressionDef.Empty) {
					return a;
				} else {
					return "(" + a + "+" + b + ")";
				}
			}
		}
	}

	/**
	 * Simple star.
	 *
	 * @param a the a
	 * @return the string
	 */
	public static String simpleStar(String a) {
		if (a == RegularExpressionDef.Empty) {
			return RegularExpressionDef.Epsilon;
		} else {
			if (a == RegularExpressionDef.Epsilon) {
				return RegularExpressionDef.Epsilon;
			} else {
				return a + "*";
			}
		}
	}

	/**
	 * Simple concat.
	 *
	 * @param a the a
	 * @param b the b
	 * @return the string
	 */
	public static String simpleConcat(String a, String b) {
		if (a == RegularExpressionDef.Epsilon) {
			return b;
		} else {
			if (b == RegularExpressionDef.Epsilon) {
				return a;
			} else {
				if (a == RegularExpressionDef.Empty
						|| b == RegularExpressionDef.Empty) {
					return RegularExpressionDef.Empty;
				} else {
					return "(" + a + "." + b + ")";
				}
			}
		}
	}
}
