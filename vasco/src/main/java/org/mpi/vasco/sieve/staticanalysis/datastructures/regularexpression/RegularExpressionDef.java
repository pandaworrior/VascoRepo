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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mpi.vasco.sieve.staticanalysis.datastructures.path.PathAbstraction;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class RegularExpressionDef.
 */
public class RegularExpressionDef {
	
	/** The Constant Empty. */
	public final static String Empty = "Empty";

	/** The Constant Epsilon. */
	public final static String Epsilon = "Epsilon";

	/** The Constant starOperator. */
	public final static char starOperator = '*';

	/** The Constant unionOperator. */
	public final static char unionOperator = '+';

	/** The Constant concatOperator. */
	public final static char concatOperator = '.';

	/** The Constant leftBracket. */
	public final static char leftBracket = '(';

	/** The Constant rightBracket. */
	public final static char rightBracket = ')';
	
	/** The Constant STAR_OP_VALUE. */
	public final static int STAR_OP_VALUE = -1;

	/** The Constant UINION_OP_VALUE. */
	public final static int UINION_OP_VALUE = -2;

	/** The Constant CONCAT_OP_VALUE. */
	public final static int CONCAT_OP_VALUE = -3;

	/** The Constant LEFT_BRACKET_VALUE. */
	public final static int LEFT_BRACKET_VALUE = -4;

	/** The Constant RIGHT_BRACKET_VALUE. */
	public final static int RIGHT_BRACKET_VALUE = -5;

	/** The Constant delimiter. */
	public final static String delimiter = "[\\"
			+ RegularExpressionDef.leftBracket + "\\"
			+ RegularExpressionDef.rightBracket + "\\"
			+ RegularExpressionDef.starOperator + "\\"
			+ RegularExpressionDef.unionOperator + "\\"
			+ RegularExpressionDef.concatOperator + "]";

	/** The Constant reduceDelimiter. */
	public final static String reduceDelimiter = "[\\" + "\\"
			+ RegularExpressionDef.starOperator + "\\"
			+ RegularExpressionDef.unionOperator + "]";

	/**
	 * The Enum RegExpElementTypes.
	 */
	public enum RegExpElementTypes {

		/** The star op. */
		STAR_OP,

		/** The uinion op. */
		UINION_OP,

		/** The concat op. */
		CONCAT_OP,

		/** The left bracket. */
		LEFT_BRACKET,

		/** The right bracket. */
		RIGHT_BRACKET,

		/** The operand. */
		OPERAND
	}

	/**
	 * Checks if is non special key word.
	 *
	 * @param type the type
	 * @return true, if is non special key word
	 */
	public static boolean isNonSpecialKeyWord(RegularExpressionDef.RegExpElementTypes type) {
		switch(type){
		case OPERAND:
			return true;
			default:
				return false;
		}
	}
	
	/**
	 * Checks if is specical key words.
	 *
	 * @param word the word
	 * @return true, if is specical key words
	 */
	public static boolean isSpecicalKeyWords(char word) {
		if (word == RegularExpressionDef.leftBracket
				|| word == RegularExpressionDef.rightBracket
				|| word == RegularExpressionDef.starOperator
				|| word == RegularExpressionDef.concatOperator
				|| word == RegularExpressionDef.unionOperator) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Find next key words.
	 *
	 * @param beginIndex the begin index
	 * @param regExp the reg exp
	 * @return the int
	 */
	public static int findNextKeyWords(int beginIndex, String regExp) {
		// Debug.println("delimiter: " + RegularExpressionDef.delimiter);
		Pattern patt = Pattern.compile(RegularExpressionDef.delimiter);
		Matcher matcher = patt.matcher(regExp.substring(beginIndex));
		if (matcher.find()) {
			return matcher.start() + beginIndex;
		} else {
			return -1;
		}
	}

	/**
	 * Gets the first star operator.
	 *
	 * @param pathAb the path ab
	 * @return the first star operator
	 */
	public static int getFirstStarOperator(PathAbstraction pathAb) {
		Iterator<Integer> it = pathAb.getRegularExprNodeList()
				.iterator();
		int index = 0;
		while (it.hasNext()) {
			Integer currentNode = it.next();
			if (isStarOperator(currentNode.intValue())) {
				return index;
			}
			index++;
		}
		return -1;
	}

	/**
	 * Gets the top union operator.
	 *
	 * @param pathAb the path ab
	 * @return the top union operator
	 */
	public static int getTopUnionOperator(PathAbstraction pathAb) {
		Iterator<Integer> it = pathAb.getRegularExprNodeList().iterator();
		ArrayList<Integer> unionOperatorList = new ArrayList<Integer>();
		int index = 0;
		int topUnionOperatorIndex = -1;
		while (it.hasNext()) {
			Integer currentNode = it.next();
			if (isUnionOperator(currentNode.intValue())) {
				unionOperatorList.add(new Integer(index));
			}
			index++;
		}
		if (unionOperatorList.isEmpty()) {
			return topUnionOperatorIndex;
		} else {
			// find the top union operator
			// the top union operator is an operator that has lowest priority
			// example: (1+2)+(2+3) the middle + is the top union operator
			topUnionOperatorIndex = unionOperatorList.get(0).intValue();
			int leftOperandStartIndex = findIndexOfLeftSubExpressionForUnion(
					pathAb, topUnionOperatorIndex);
			int rightOperandEndIndex = findIndexOfRightSubExpressionForUnion(
					pathAb, topUnionOperatorIndex);
			;
			for (int i = 1; i < unionOperatorList.size(); i++) {
				int unionOperatorIndex = unionOperatorList.get(i).intValue();
				int tempLeftOperandStartIndex = findIndexOfLeftSubExpressionForUnion(
						pathAb, unionOperatorIndex);
				int tempRightOperandEndIndex = findIndexOfRightSubExpressionForUnion(
						pathAb, unionOperatorIndex);
				if (tempLeftOperandStartIndex <= leftOperandStartIndex
						&& tempRightOperandEndIndex >= rightOperandEndIndex) {
					topUnionOperatorIndex = unionOperatorIndex;
					leftOperandStartIndex = tempLeftOperandStartIndex;
					rightOperandEndIndex = tempRightOperandEndIndex;
				}
			}
			return topUnionOperatorIndex;
		}
	}

	/**
	 * Find index of left sub expression for union.
	 *
	 * @param pathAb the path ab
	 * @param unionOperatorIndex the union operator index
	 * @return the int
	 */
	public static int findIndexOfLeftSubExpressionForUnion(
			PathAbstraction pathAb, int unionOperatorIndex) {
		int itIndex = unionOperatorIndex - 1;
		Stack<Integer> s = new Stack<Integer>();
		boolean isRightBracketSeen = false;
		while (itIndex >= 0) {
			Integer currIt = pathAb.getRegularExprNodeList().get(
					itIndex);
			if (isRightBracket(currIt.intValue())) {
				s.push(currIt);
				if(!isRightBracketSeen) {
					isRightBracketSeen = true;
				}
			} else {
				if (isLeftBracket(currIt.intValue())) {
					if (s.isEmpty()) {
						/*if(isRightBracketSeen) {
							return itIndex;
						}else {
							return itIndex + 1;
						}*/
						return itIndex + 1;
					} else {
						s.pop();
					}
				}
			}
			itIndex--;
		}
		return itIndex + 1;
	}

	/**
	 * Find index of right sub expression for union.
	 *
	 * @param pathAb the path ab
	 * @param unionOperatorIndex the union operator index
	 * @return the int
	 */
	public static int findIndexOfRightSubExpressionForUnion(
			PathAbstraction pathAb, int unionOperatorIndex) {
		int itIndex = unionOperatorIndex + 1;
		Stack<Integer> s = new Stack<Integer>();
		boolean isLeftBracketSeen = false;
		while (itIndex < pathAb.getRegularExprNodeList().size()) {
			Integer currIt = pathAb.getRegularExprNodeList().get(
					itIndex);
			if (isLeftBracket(currIt.intValue())) {
				s.push(currIt);
				if(!isLeftBracketSeen) {
					isLeftBracketSeen = true;
				}
			} else {
				if (isRightBracket(currIt.intValue())) {
					if (s.isEmpty()) {
						/*if(isLeftBracketSeen)
							return itIndex;
						else
							return itIndex -1;*/
						return itIndex - 1;
					} else {
						s.pop();
					}
				}
			}
			itIndex++;
		}
		return itIndex - 1;
	}

	/**
	 * Find index of sub expression for star.
	 *
	 * @param pathAb the path ab
	 * @param starOperatorIndex the star operator index
	 * @return the int
	 */
	public static int findIndexOfSubExpressionForStar(PathAbstraction pathAb,
			int starOperatorIndex) {
		Stack<Integer> s = new Stack<Integer>();
		int itIndex = starOperatorIndex;
		while (itIndex >= 0) {
			Integer currIt = pathAb.getRegularExprNodeList().get(itIndex);
			if (isRightBracket(currIt.intValue())) {
				s.push(currIt);
			} else {
				if (isLeftBracket(currIt.intValue())) {
					s.pop();
					if (s.isEmpty()) {
						return itIndex;
					}
				}
			}
			itIndex--;
		}
		return itIndex + 1;
	}
	
	/**
	 * Gets the all union operator index covered by a star operator.
	 *
	 * @param pathAb the path ab
	 * @param starOperandIndex the star operand index
	 * @param starOperatorIndex the star operator index
	 * @return the all union operator index covered by a star operator
	 */
	public static List<Integer> getAllUnionOperatorIndexCoveredByAStarOperator(
			PathAbstraction pathAb, int starOperandIndex, int starOperatorIndex){
		List<Integer> unionOperatorIndexList = pathAb.getAllUnionOperatorIndex(starOperandIndex, 
				starOperatorIndex);
		return unionOperatorIndexList;
	}
	
	/**
	 * Creates the new path abstraction keep left operand.
	 *
	 * @param pathAb the path ab
	 * @param startOperatorIndex the start operator index
	 * @param unionOperatorIndex the union operator index
	 * @return the path abstraction
	 */
	public static PathAbstraction createNewPathAbstractionKeepLeftOperand(
			PathAbstraction pathAb, int starOperatorIndex, int unionOperatorIndex) {
		int rightOperandIndex = findIndexOfRightSubExpressionForUnion(pathAb,
				unionOperatorIndex);
		PathAbstraction newPathAb = pathAb.pathAbstractionClone();
		newPathAb.removeSpecificRegularExprNode(starOperatorIndex);
		newPathAb.removeSpecificRegularExprNodeRange(unionOperatorIndex, rightOperandIndex);
		return newPathAb;
	}
	
	/**
	 * Creates the new path abstraction keep right operand.
	 *
	 * @param pathAb the path ab
	 * @param startOperatorIndex the start operator index
	 * @param unionOperatorIndex the union operator index
	 * @return the path abstraction
	 */
	public static PathAbstraction createNewPathAbstractionKeepRightOperand(
			PathAbstraction pathAb, int startOperatorIndex, int unionOperatorIndex) {
		int leftOperandIndex = findIndexOfLeftSubExpressionForUnion(pathAb,
				unionOperatorIndex);
		PathAbstraction newPathAb = pathAb.pathAbstractionClone();
		newPathAb.removeSpecificRegularExprNode(startOperatorIndex);
		newPathAb.removeSpecificRegularExprNodeRange(leftOperandIndex, unionOperatorIndex);
		return newPathAb;
	}
	
	/**
	 * Creates the new path abstraction keep left right operand.
	 *
	 * @param pathAb the path ab
	 * @param startOperatorIndex the start operator index
	 * @param unionOperatorIndex the union operator index
	 * @return the path abstraction
	 */
	public static PathAbstraction createNewPathAbstractionKeepLeftRightOperand(
			PathAbstraction pathAb, int startOperatorIndex, int unionOperatorIndex) {
		PathAbstraction newPathAb = pathAb.pathAbstractionClone();
		newPathAb.setRegularElementAtIndex(unionOperatorIndex, CONCAT_OP_VALUE);
		newPathAb.removeSpecificRegularExprNode(startOperatorIndex);
		return newPathAb;
	}
	
	public static PathAbstraction createNewPathAbstractionKeepLoopBody(
			PathAbstraction pathAb, int starOperandStartIndex, int starOperatorIndex) {
		PathAbstraction newPathAb = pathAb.pathAbstractionClone();
		int[] indices = new int[3];
		indices[0] = starOperandStartIndex;
		indices[1] = starOperatorIndex - 1;
		indices[2] = starOperatorIndex;
		newPathAb.removeSpecificRegularExprNodes(indices);
		return newPathAb;
	}

	/**
	 * Reduce regular expression with star.
	 *
	 * @param pathAb the path ab
	 * @param starOperatorIndex the star operator index
	 * @param starOperandStartIndex the star operand start index
	 * @return the list
	 */
	public static List<PathAbstraction> reduceRegularExpressionWithStar(
			PathAbstraction pathAb, int starOperatorIndex,
			int starOperandStartIndex) {
		List<PathAbstraction> returnList = new ArrayList<PathAbstraction>();
		//System.out.println("handle star for " + pathAb.toStringPlainText());
		//first identify all alternations that covered by this star
		List<Integer> unionOperatorIndexList = RegularExpressionDef.getAllUnionOperatorIndexCoveredByAStarOperator(
				pathAb, starOperandStartIndex, starOperatorIndex);
		for(Integer unionOperatorIndex : unionOperatorIndexList) {
			//keep the leftOperand
			PathAbstraction leftOpPathAb = createNewPathAbstractionKeepLeftOperand(pathAb, 
					starOperatorIndex, unionOperatorIndex);
			returnList.add(leftOpPathAb);
			//System.out.println("keep left: " + leftOpPathAb.toStringPlainText());
			//keep the rightOperand
			PathAbstraction rightOpPathAb = createNewPathAbstractionKeepRightOperand(pathAb,
					starOperatorIndex, unionOperatorIndex);
			returnList.add(rightOpPathAb);
			//System.out.println("keep right: " + rightOpPathAb.toStringPlainText());
			//concatenate leftOperand and rightOperand
			PathAbstraction bothOpPathAb = createNewPathAbstractionKeepLeftRightOperand(pathAb,
					starOperatorIndex, unionOperatorIndex);
			returnList.add(bothOpPathAb);
			//System.out.println("keep both " + bothOpPathAb.toStringPlainText());
		}
		// create without loop regular expression
		PathAbstraction noLoopPathAb = pathAb.pathAbstractionClone();
		noLoopPathAb.removeSpecificRegularExprNodeRange(starOperandStartIndex,
				starOperatorIndex);
		if(unionOperatorIndexList.size() == 0) {
			//create regular expression preserving the loop body
			PathAbstraction singlePathAb = createNewPathAbstractionKeepLoopBody(pathAb,
					starOperandStartIndex,
					starOperatorIndex);
			returnList.add(singlePathAb);
		}
		returnList.add(noLoopPathAb);
		//System.out.println("keep noloop " + noLoopPathAb.toStringPlainText());
		return returnList;
	}

	/**
	 * Reduce regular expression with union.
	 *
	 * @param pathAb the path ab
	 * @param unionOperatorIndex the union operator index
	 * @param leftOperandStartIndex the left operand start index
	 * @param rightOperandStartIndex the right operand start index
	 * @return the list
	 */

	public static List<PathAbstraction> reduceRegularExpressionWithUnion(
			PathAbstraction pathAb, int unionOperatorIndex,
			int leftOperandStartIndex, int rightOperandStartIndex) {
		List<PathAbstraction> returnList = new ArrayList<PathAbstraction>();
		//System.out.println("handle union for " + pathAb.toStringPlainText());
		// create two regular expressions
		PathAbstraction onecasePathAb = pathAb.pathAbstractionClone();
		//Debug.println("one case path abstraction: "
			//	+ onecasePathAb.toStringPlainText());
		PathAbstraction othercasePathAb = pathAb.pathAbstractionClone();
		//Debug.println("other case path abstraction: "
			//	+ othercasePathAb.toStringPlainText());

		onecasePathAb.removeSpecificRegularExprNodeRange(
				leftOperandStartIndex, unionOperatorIndex);
		//System.out.println("keep one " + onecasePathAb.toStringPlainText());
		othercasePathAb.removeSpecificRegularExprNodeRange(unionOperatorIndex,
				rightOperandStartIndex);
		//System.out.println("keep the other " + othercasePathAb.toStringPlainText());
		returnList.add(onecasePathAb);
		returnList.add(othercasePathAb);
		//Debug.println("one case path abstraction: "
		//		+ onecasePathAb.toStringPlainText());
		//Debug.println("other case path abstraction: "
			//	+ othercasePathAb.toStringPlainText());
		return returnList;
	}

	/**
	 * Removes the duplicate operators.
	 *
	 * @param regExprStr the reg expr str
	 * @return the string
	 */
	public static String removeDuplicateOperators(String regExprStr) {
		// Debug.println("before: " + regExprStr);
		regExprStr = regExprStr.replaceAll("\\.+", "\\.");
		regExprStr = regExprStr.replaceAll("\\++", "\\+");
		regExprStr = regExprStr.replace("(.", "(");
		regExprStr = regExprStr.replace(".)", ")");
		regExprStr = regExprStr.replace("(+", "(");
		regExprStr = regExprStr.replace("+)", ")");
		// Debug.println("after: " + regExprStr);
		return regExprStr;
	}
	
	/**
	 * Convert reg expr to string.
	 *
	 * @param regElement the reg element
	 * @return the string
	 */
	public static String convertRegExprToString(int regElement) {
		if(regElement < 0) {
			switch(regElement) {
			case RegularExpressionDef.STAR_OP_VALUE:
				return String.valueOf(RegularExpressionDef.starOperator);
			case RegularExpressionDef.CONCAT_OP_VALUE:
				return String.valueOf(RegularExpressionDef.concatOperator);
			case RegularExpressionDef.UINION_OP_VALUE:
				return String.valueOf(RegularExpressionDef.unionOperator);
			case RegularExpressionDef.LEFT_BRACKET_VALUE:
				return String.valueOf(RegularExpressionDef.leftBracket);
			case RegularExpressionDef.RIGHT_BRACKET_VALUE:
				return String.valueOf(RegularExpressionDef.rightBracket);
				default:
					throw new RuntimeException("invalid regElement " + regElement);
			}
		}else {
			return Integer.toString(regElement);
		}
	}
	
	/**
	 * Convert string to reg expr.
	 *
	 * @param regElement the reg element
	 * @return the int
	 */
	public static int convertStringToRegExpr(String regElement) {
		if(regElement.equals(RegularExpressionDef.starOperator)) {
			return RegularExpressionDef.STAR_OP_VALUE;
		}else if(regElement.equals(RegularExpressionDef.concatOperator)) {
			return RegularExpressionDef.CONCAT_OP_VALUE;
		}else if(regElement.equals(RegularExpressionDef.unionOperator)) {
			return RegularExpressionDef.UINION_OP_VALUE;
		}else if(regElement.equals(RegularExpressionDef.leftBracket)) {
			return RegularExpressionDef.LEFT_BRACKET_VALUE;
		}else if(regElement.equals(RegularExpressionDef.rightBracket)) {
			return RegularExpressionDef.RIGHT_BRACKET_VALUE;
		}else {
			return Integer.parseInt(regElement);
		}
	}
	
	/**
	 * Convert special char to reg expr.
	 *
	 * @param specialKeyWord the special key word
	 * @return the int
	 */
	public static int convertSpecialCharToRegExpr(char specialKeyWord) {
		switch(specialKeyWord) {
		case RegularExpressionDef.starOperator:
			return RegularExpressionDef.STAR_OP_VALUE;
		case RegularExpressionDef.concatOperator:
			return RegularExpressionDef.CONCAT_OP_VALUE;
		case RegularExpressionDef.unionOperator:
			return RegularExpressionDef.UINION_OP_VALUE;
		case RegularExpressionDef.leftBracket:
			return RegularExpressionDef.LEFT_BRACKET_VALUE;
		case RegularExpressionDef.rightBracket:
			return RegularExpressionDef.RIGHT_BRACKET_VALUE;
			default:
				throw new RuntimeException("invalid type " + specialKeyWord);
		}
	}
	
	/**
	 * Convert string to non operator element.
	 *
	 * @param regElement the reg element
	 * @return the int
	 */
	public static int convertStringToNonOperatorElement(String regElement) {
		try {
			return Integer.parseInt(regElement);
		}catch(NumberFormatException e) {
			e.printStackTrace();
			throw new RuntimeException("The non operator element is not in a right format" + regElement);
		}
	}
	
	/**
	 * Checks if is special key word.
	 *
	 * @param regElement the reg element
	 * @return true, if is special key word
	 */
	public static boolean isSpecialKeyWord(int regElement) {
		switch(regElement) {
		case RegularExpressionDef.STAR_OP_VALUE:
		case RegularExpressionDef.CONCAT_OP_VALUE:
		case RegularExpressionDef.UINION_OP_VALUE:
		case RegularExpressionDef.LEFT_BRACKET_VALUE:
		case RegularExpressionDef.RIGHT_BRACKET_VALUE:
			return true;
			default:
				return false;
		}
	}
	
	/**
	 * Checks if is left bracket.
	 *
	 * @param regElement the reg element
	 * @return true, if is left bracket
	 */
	public static boolean isLeftBracket(int regElement) {
		if(regElement == RegularExpressionDef.LEFT_BRACKET_VALUE) {
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * Checks if is right bracket.
	 *
	 * @param regElement the reg element
	 * @return true, if is right bracket
	 */
	public static boolean isRightBracket(int regElement) {
		if(regElement == RegularExpressionDef.RIGHT_BRACKET_VALUE) {
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * Checks if is star operator.
	 *
	 * @param regElement the reg element
	 * @return true, if is star operator
	 */
	public static boolean isStarOperator(int regElement) {
		if(regElement == RegularExpressionDef.STAR_OP_VALUE) {
			return true;
		}else {
			return false;
		}
	}
	
	/**
	 * Checks if is union operator.
	 *
	 * @param regElement the reg element
	 * @return true, if is union operator
	 */
	public static boolean isUnionOperator(int regElement) {
		if(regElement == RegularExpressionDef.UINION_OP_VALUE) {
			return true;
		}else {
			return false;
		}
	}

}
