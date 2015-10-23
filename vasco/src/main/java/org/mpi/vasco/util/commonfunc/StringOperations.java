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
package org.mpi.vasco.util.commonfunc;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class StringOperations.
 */
public class StringOperations {
	
	/** The Constant hexCharArray. */
	final protected static char[] hexCharArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	/**
	 * Trim double quotes head tail.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String trimDoubleQuotesHeadTail(String str){
		Debug.println("we are handling: " + str);
		str = str.trim();
		if(str.indexOf('\"') == 0){
			str = str.substring(1);
		}
		if(str.lastIndexOf('\"') == str.length() - 1){
			str = str.substring(0, str.length() - 1);
		}
		Debug.println("after we hanlding this: " + str);
		return str;
	}
	
	/**
	 * Adds the double quotes head tail.
	 *
	 * @param _str the _str
	 * @return the string
	 */
	public static String addDoubleQuotesHeadTail(String _str) {
		_str = "\""+ _str + "\"";
		return _str;
	}
	
	/**
	 * Removes the last semi column.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String removeLastSemiColumn(String str){
		str = str.trim();
		int endIndex = str.lastIndexOf(";");
		if (endIndex == str.length() - 1) {
			str = str.substring(0, endIndex);
		}
		return str;
	}
	
	/**
	 * Removes the last and operator.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String removeLastAndOperator(String str) {
		int endIndex = str.lastIndexOf("&");
		if(endIndex == -1) {
			return str;
		}else {
			return str.substring(0, endIndex);
		}
	}
	
	/**
	 * Removes the last comma.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String removeLastComma(String str){
		str = str.trim();
		int endIndex = str.lastIndexOf(",");
		if (endIndex == str.length() - 1) {
			str = str.substring(0, endIndex);
		}
		return str;
	}
	
	/**
	 * Replace last comma with double quotes.
	 *
	 * @param _str the _str
	 * @return the string
	 */
	public static String replaceLastMathAndWithDoubleQuotes(String _str) {
		_str = removeLastMathAnd(_str) + "\"";
		return _str;
	}
	
	/**
	 * Removes the last math and.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String removeLastMathAnd(String str){
		str = str.trim();
		int endIndex = str.lastIndexOf("&");
		if (endIndex == str.length() - 1) {
			str = str.substring(0, endIndex);
		}
		return str;
	}
	
	/**
	 * Bytes to hex.
	 *
	 * @param bytes the bytes
	 * @return the string
	 */
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexCharArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexCharArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	/**
	 * Removes the all double quotes and plus.
	 *
	 * @param _str the _str
	 * @return the string
	 */
	public static String removeAllDoubleQuotesAndPlus(String _str) {
		_str = trimDoubleQuotesHeadTail(_str);
		_str = _str.replaceAll("\"(\\s*)\\+(\\s*)\"", " ");
		_str = _str.replaceAll("\\\\\"", "");
		return _str;
	}
	
	/**
	 * Removes the all white spaces.
	 *
	 * @param _str the _str
	 * @return the string
	 */
	public static String removeAllWhiteSpaces(String _str) {
		return _str.replaceAll("\\s+","");
	}
	
	/**
	 * Checks if is string seen.
	 *
	 * @param _str the _str
	 * @param strList the str list
	 * @return true, if is string seen
	 */
	public static boolean isStringSeen(String _str, List<String> strList) {
		if(strList == null || strList.size() == 0) {
			return false;
		}else {
			for(String seenStr : strList) {
				if(_str.equals(seenStr)) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Split string by slash return last one.
	 *
	 * @param _str the _str
	 * @return the string
	 */
	public static String splitStringBySlashReturnLastOne(String _str) {
		int index = _str.lastIndexOf('/');
		if(index == -1) {
			return _str;
		}else {
			return _str.substring(index+1);
		}
	}
	
	/**
	 * Adds the single quote to head tail.
	 *
	 * @param _str the _str
	 * @return the string
	 */
	public static String addSingleQuoteToHeadTail(String _str) {
		if(_str.charAt(0) == '\'' && 
				_str.charAt(_str.length()-1) == '\'') {
			return _str;
		}else {
			return "'"+ _str +"'";
		}
	}
	
	/**
	 * Adds the double quote to head tail.
	 *
	 * @param _str the _str
	 * @return the string
	 */
	public static String addDoubleQuoteToHeadTail(String _str) {
		if(_str == null || _str.equals("")){
			return "";
		}
		if(_str.charAt(0) == '\"' && 
				_str.charAt(_str.length()-1) == '\"') {
			return _str;
		}else {
			return "\""+ _str +"\"";
		}
	}
	
	/**
	 * Removes the quotes from head tail.
	 *
	 * @param _str the _str
	 * @return the string
	 */
	public static String removeQuotesFromHeadTail(String _str) {
		int indexOfLastChar = _str.length() - 1;
		if((_str.indexOf("\"") == 0 && _str.lastIndexOf("\"") == indexOfLastChar) ||
				(_str.indexOf("'") == 0 && _str.lastIndexOf("'") == indexOfLastChar)) {
			return _str.substring(1, indexOfLastChar);
		}else {
			return _str;
		}
	}
	
	/**
	 * Concat string split by dot.
	 *
	 * @param strList the str list
	 * @return the string
	 */
	public static String concatStringSplitByDot(List<String> strList) {
		StringBuilder returnStrBuilder = new StringBuilder("");
		for(int i = 0; i < strList.size(); i++) {
			String singleStr = strList.get(i);
			if(i == 0) {
				returnStrBuilder.append(singleStr);
			}else {
				returnStrBuilder.append(".");
				returnStrBuilder.append(singleStr);
			}
		}
		return returnStrBuilder.toString();
	}
}
