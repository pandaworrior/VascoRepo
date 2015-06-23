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
package org.mpi.vasco.sieve.staticanalysis.datastructures.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.mpi.vasco.sieve.staticanalysis.datastructures.controlflowgraph.CFGGraph;
import org.mpi.vasco.sieve.staticanalysis.datastructures.regularexpression.RegularExpressionDef;
import org.mpi.vasco.sieve.staticanalysis.datastructures.regularexpression.SimpleRegularExpression;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class PathAbstraction.
 * 
 * @author chengli
 */
public class PathAbstraction {

	/** The regular expr str. */
	String regularExprStr;

	/** The reg exp elements. */
	List<Integer> regExpElements;

	/**
	 * Instantiates a new path abstraction.
	 */
	public PathAbstraction() {
		this.regExpElements = new ArrayList<Integer>();
	}

	/**
	 * Instantiates a new path abstraction.
	 *
	 * @param regExprStr the reg expr str
	 */
	public PathAbstraction(String regExprStr) {
		this.regularExprStr = regExprStr;
		this.regExpElements = new ArrayList<Integer>();
		this.setRegExpressionNodeList();
	}

	/**
	 * Instantiates a new path abstraction.
	 *
	 * @param <T1> the generic type
	 * @param <T2> the generic type
	 * @param cfg the cfg
	 */
	public <T1, T2> PathAbstraction(CFGGraph<T1, T2> cfg) {
		this.regularExprStr = this.computeRegularExpression(cfg);
		this.regExpElements = new ArrayList<Integer>();
		this.setRegExpressionNodeList();
	}

	/**
	 * Sets the regular expr string.
	 */
	public void setRegularExprString() {
		this.regularExprStr = this.toStringPlainText();
	}

	/**
	 * Gets the regular expr node list.
	 *
	 * @return the regular expr node list
	 */
	public List<Integer> getRegularExprNodeList() {
		return this.regExpElements;
	}

	/**
	 * Adds the regular expression node.
	 *
	 * @param regExprNode the reg expr node
	 */
	public void addRegularExpressionNode(Integer regExprNode) {
		this.regExpElements.add(regExprNode);
	}

	/**
	 * Break into pieces.
	 */
	private void setRegExpressionNodeList() {
		int index = 0;
		int length = this.regularExprStr.length();
		while (index < length) {
			char currentChar = this.regularExprStr.charAt(index);
			if (RegularExpressionDef.isSpecicalKeyWords(currentChar)) {
				regExpElements.add(RegularExpressionDef.convertSpecialCharToRegExpr(currentChar));
				index = index + 1;
				continue;
			}
			int endIndex = RegularExpressionDef.findNextKeyWords(index,
					this.regularExprStr);
			if (endIndex == -1) {
				endIndex = this.regularExprStr.length();
			}
			regExpElements.add(RegularExpressionDef.convertStringToNonOperatorElement(this.regularExprStr
					.substring(index, endIndex)));
			index = endIndex;
		}
	}

	/**
	 * Compute regular expression.
	 *
	 * @param <T1> the generic type
	 * @param <T2> the generic type
	 * @param cfg the cfg
	 * @return the string
	 */
	public <T1, T2> String computeRegularExpression(CFGGraph<T1, T2> cfg) {
		// this step is required to order the node list for regular expression
		// computation
		cfg.setNodeList(cfg.getNodeListViaBFS());
		String[][] nodeIdMatrix = cfg.convertGraphIntoMatrix();
		String[] regularExpArray = cfg.initalizeRegExpArray();
		int nodeCount = cfg.getNodeList().size();

		int topIndex, subIndex, innerIndex;
		for (topIndex = nodeCount - 1; topIndex >= 0; topIndex--) {
			regularExpArray[topIndex] = SimpleRegularExpression.simpleConcat(
					SimpleRegularExpression
							.simpleStar(nodeIdMatrix[topIndex][topIndex]),
					regularExpArray[topIndex]);
			for (subIndex = 0; subIndex < nodeCount; subIndex++) {
				nodeIdMatrix[topIndex][subIndex] = SimpleRegularExpression
						.simpleConcat(SimpleRegularExpression
								.simpleStar(nodeIdMatrix[topIndex][topIndex]),
								nodeIdMatrix[topIndex][subIndex]);
			}
			for (subIndex = 0; subIndex < nodeCount; subIndex++) {
				regularExpArray[subIndex] = SimpleRegularExpression
						.simpleUnion(regularExpArray[subIndex],
								SimpleRegularExpression.simpleConcat(
										nodeIdMatrix[subIndex][topIndex],
										regularExpArray[topIndex]));
				for (innerIndex = 0; innerIndex < nodeCount; innerIndex++) {
					nodeIdMatrix[subIndex][innerIndex] = SimpleRegularExpression
							.simpleUnion(nodeIdMatrix[subIndex][innerIndex],
									SimpleRegularExpression.simpleConcat(
											nodeIdMatrix[subIndex][topIndex],
											nodeIdMatrix[topIndex][innerIndex]));
				}
			}
		}
		return regularExpArray[0];
	}

	/**
	 * Path abstraction clone.
	 *
	 * @return the path abstraction
	 */
	public PathAbstraction pathAbstractionClone() {
		PathAbstraction pathAb = new PathAbstraction();
		pathAb.regularExprStr = this.regularExprStr;
		for (Integer regExprNode : this.regExpElements) {
			pathAb.addRegularExpressionNode(regExprNode);
		}
		return pathAb;
	}

	/**
	 * Removes the specific regular expr node.
	 *
	 * @param index the index
	 */
	public void removeSpecificRegularExprNode(int index) {
		assert (index >= 0 && index <= this.regExpElements.size() - 1);
		this.regExpElements.remove(index);
		this.setRegularExprString();
	}
	
	/**
	 * Removes the specific regular expr nodes.
	 *
	 * @param indices the indices
	 */
	public void removeSpecificRegularExprNodes(int[] indices) {
		//first sort the index array
		if(indices == null || indices.length == 0) {
			return;
		}
		Arrays.sort(indices);
		int len = indices.length - 1;
		while(len >= 0) {
			this.regExpElements.remove(indices[len]);
			len--;
		}
		this.setRegularExprString();
	}

	/**
	 * Removes the specific regular expr node range.
	 *
	 * @param startIndex the start index
	 * @param endIndex the end index
	 */
	public void removeSpecificRegularExprNodeRange(int startIndex, int endIndex) {
		assert (startIndex <= endIndex);
		assert (startIndex >= 0 && startIndex <= this.regExpElements.size() - 1);
		assert (endIndex >= 0 && endIndex <= this.regExpElements.size() - 1);

		// if the operator before startIndex and after endIndex is ., remove one
		/*if (startIndex >= 1 && endIndex <= this.regExpElements.size() - 2) {
			Integer beforeNode = this.regExpElements
					.get(startIndex - 1);
			Integer afterNode = this.regExpElements
					.get(endIndex + 1);
			if (beforeNode.intValue() == RegularExpressionDef.CONCAT_OP_VALUE
					&& afterNode.intValue() == RegularExpressionDef.CONCAT_OP_VALUE) {
				startIndex = startIndex - 1;
			}
			if (afterNode.intValue() == RegularExpressionDef.CONCAT_OP_VALUE
					&& !(beforeNode.intValue() == RegularExpressionDef.CONCAT_OP_VALUE)) {
				endIndex = endIndex + 1;
			}
		}*/

		// if the operator after endIndex is . and before startIndex is no, then
		// remove .
		/*if(endIndex < this.regExpElements.size()) {
			endIndex = endIndex + 1;
		}*/
		this.regExpElements.subList(startIndex, endIndex+1).clear();
		this.setRegularExprString();
	}

	/**
	 * Removes the brackets.
	 */
	public void removeBrackets() {
		Iterator<Integer> it = this.regExpElements.iterator();
		while (it.hasNext()) {
			Integer currentNode = it.next();
			if (currentNode.intValue() == RegularExpressionDef.LEFT_BRACKET_VALUE ||
					currentNode.intValue() == RegularExpressionDef.RIGHT_BRACKET_VALUE) {
				it.remove();
			}
		}
		this.setRegularExprString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	/**
	 * To string.
	 *
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = "";
		for (Integer element : this.regExpElements) {
			str += element.intValue() + " ";
		}
		return str;
	}

	/**
	 * To string plain text.
	 *
	 * @return the string
	 */
	public String toStringPlainText() {
		String str = "";
		//if(Debug.debug) {
			for (Integer element : this.regExpElements) {
				str += RegularExpressionDef.convertRegExprToString(element.intValue());
			}
		//}
		return str;
	}
	
	/**
	 * To string with non operators plain text.
	 *
	 * @return the string
	 */
	public String toStringWithNonOperatorsPlainText() {
		String str = "";
		for (Integer element : this.regExpElements) {
			if(RegularExpressionDef.isSpecialKeyWord(element.intValue())) {
				continue;
			}
			str += element.intValue() + ".";
		}
		return str;
	}

	/**
	 * Prints the out.
	 */
	public void printOut() {
		Debug.println("Print out PathAbstraction info");
		//Debug.println("RegExpr string: " + this.regularExprStr);
		System.out.println("RegExpr string: " + this.regularExprStr);
		//Debug.println(toString());
	}
	
	/**
	 * Prints the out in plain text.
	 */
	public void printOutInPlainText(){
		Debug.println("Print out PathAbstraction info");
		if(Debug.debug) {
			String _str = "PathAbstraction: ";
			_str += toString();
			System.out.println(_str);
		}
	}
	
	/**
	 * Gets the all non operator elements.
	 *
	 * @return the all non operator elements
	 */
	public List<Integer> getAllNonOperatorElements(){
		List<Integer> nonOperatorElements = new ArrayList<Integer>();
		for(Integer element : this.regExpElements){
			if(!RegularExpressionDef.isSpecialKeyWord(element.intValue())){
				nonOperatorElements.add(element);
			}
		}
		assert(nonOperatorElements.size() > 1);
		return nonOperatorElements;
	}
	
	/**
	 * Gets the all union operator index.
	 *
	 * @param startIndex the start index
	 * @param endIndex the end index
	 * @return the all union operator index
	 */
	public List<Integer> getAllUnionOperatorIndex(int startIndex, int endIndex){
		List<Integer> unionOperatorIndex = new ArrayList<Integer>();
		for(int i = startIndex; i < endIndex; i++) {
			Integer element = this.regExpElements.get(i);
			if(RegularExpressionDef.isUnionOperator(element.intValue())) {
				unionOperatorIndex.add(i);
			}
		}
		return unionOperatorIndex;
	}
	
	/**
	 * Sets the regular element at index.
	 *
	 * @param index the index
	 * @param value the value
	 */
	public void setRegularElementAtIndex(int index, int value) {
		this.regExpElements.set(index, new Integer(value));
	}

}
