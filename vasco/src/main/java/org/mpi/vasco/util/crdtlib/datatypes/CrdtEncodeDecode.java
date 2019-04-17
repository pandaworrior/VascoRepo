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
package org.mpi.vasco.util.crdtlib.datatypes;

import java.text.DateFormat;

import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LwwDateTime;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LwwDouble;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LwwFloat;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LwwInteger;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LwwLogicalTimestamp;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LwwLong;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LwwString;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.NormalDateTime;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.NormalDouble;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.NormalFloat;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.NormalInteger;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.NormalLong;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.NormalString;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.NumberDeltaDateTime;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.NumberDeltaDouble;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.NumberDeltaFloat;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.NumberDeltaInteger;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.PrimitiveType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.dbutil.DatabaseFunction;

// TODO: Auto-generated Javadoc
/**
 * The Class CrdtEncodeDecode.
 */
final public class CrdtEncodeDecode {
	
	/** The Constant LWWDATETIME. */
	public final static byte LWWDATETIME = 1;
	
	/** The Constant LWWBOOLEAN. */
	public final static byte LWWBOOLEAN = 2;
	
	/** The Constant LWWDOUBLE. */
	public final static byte LWWDOUBLE = 3;
	
	/** The Constant LWWFLOAT. */
	public final static byte LWWFLOAT = 4;
	
	/** The Constant LWWINTEGER. */
	public final static byte LWWINTEGER = 5;
	
	/** The Constant LWWLOGICTIMESTAMP. */
	public final static byte LWWLOGICTIMESTAMP = 6;
	
	/** The Constant LWWSTRING. */
	public final static byte LWWSTRING = 7;
	
	/** The Constant NORMALDATETIME. */
	public final static byte NORMALDATETIME = 8;
	
	/** The Constant NORMALBOOLEAN. */
	public final static byte NORMALBOOLEAN = 9;
	
	/** The Constant NORMALDOUBLE. */
	public final static byte NORMALDOUBLE = 10;
	
	/** The Constant NORMALFLOAT. */
	public final static byte NORMALFLOAT = 11;
	
	/** The Constant NORMALINTEGER. */
	public final static byte NORMALINTEGER = 12;
	
	/** The Constant NORMALSTRING. */
	public final static byte NORMALSTRING = 13;
	
	/** The Constant NUMBERDELTADATETIME. */
	public final static byte NUMBERDELTADATETIME = 14;
	
	/** The Constant NUMBERDELTADOUBLE. */
	public final static byte NUMBERDELTADOUBLE = 15;
	
	/** The Constant NUMBERDELTAFLOAT. */
	public final static byte NUMBERDELTAFLOAT = 16;
	
	/** The Constant NUMBERDELTAINTEGER. */
	public final static byte NUMBERDELTAINTEGER = 17;
	
	public final static byte LWWLONG = 18;
	
	public final static byte NORMALLONG = 19;

	/**
	 * Gets the string.
	 *
	 * @param pt the pt
	 * @return the string
	 */
	public static String getString(PrimitiveType pt){
		if(pt instanceof LwwDateTime){
			return ((LwwDateTime) pt).toString();
		}else if(pt instanceof LwwDouble){
			return ((LwwDouble) pt).toString();
		}else if(pt instanceof LwwFloat){
			return ((LwwFloat) pt).toString();
		}else if(pt instanceof LwwInteger){
			return ((LwwInteger) pt).toString();
		}else if(pt instanceof LwwLong){
			return ((LwwLong) pt).toString();
		} else if(pt instanceof LwwLogicalTimestamp){
			return ((LwwLogicalTimestamp) pt).toString();
		}else if(pt instanceof LwwString){
			return ((LwwString) pt).toString();
		}else if(pt instanceof NormalDateTime){
			return ((NormalDateTime) pt).toString();
		}else if(pt instanceof NormalDouble){
			return ((NormalDouble) pt).toString();
		}else if(pt instanceof NormalFloat){
			return ((NormalFloat) pt).toString();
		}else if(pt instanceof NormalInteger){
			return ((NormalInteger) pt).toString();
		}else if(pt instanceof NormalLong){
			return ((NormalLong) pt).toString();
		}else if(pt instanceof NormalString){
			return ((NormalString) pt).toString();
		}else if(pt instanceof NumberDeltaDateTime){
			return ((NumberDeltaDateTime) pt).toString();
		}else if(pt instanceof NumberDeltaDouble){
			return ((NumberDeltaDouble) pt).toString();
		}else if(pt instanceof NumberDeltaFloat){
			return ((NumberDeltaFloat) pt).toString();
		}else if(pt instanceof NumberDeltaInteger){
			return ((NumberDeltaInteger) pt).toString();
		}else{
			throw new RuntimeException("Can not find your PrimitiveType");
		}
	}
	
	/**
	 * Gets the string.
	 *
	 * @param crdtSet the crdt set
	 * @return the string
	 */
	public static String getString(CrdtSet crdtSet){
		if(crdtSet instanceof AppendOnlySet){
			return ((AppendOnlySet) crdtSet).toString();
		}else if(crdtSet instanceof AppendRemoveSet){
			return ((AppendRemoveSet) crdtSet).toString();
		}else{
			throw new RuntimeException("Can not find your CrdtSet");
		}
	}
	
	/**
	 * Gets the value string.
	 *
	 * @param pt the pt
	 * @return the value string
	 */
	public static String getValueString(DateFormat dateFormat, PrimitiveType pt) {
		if(pt instanceof LwwDateTime){
			return StringOperations.addDoubleQuoteToHeadTail(DatabaseFunction.convertTimestampToDateStr(dateFormat, ((LwwDateTime) pt).getValue()));
		}else if(pt instanceof LwwDouble){
			return Double.toString(((LwwDouble) pt).getValue());
		}else if(pt instanceof LwwFloat){
			return Float.toString(((LwwFloat) pt).getValue());
		}else if(pt instanceof LwwInteger){
			return Long.toString(((LwwInteger) pt).getValue());
		}else if(pt instanceof LwwLong){
			return Long.toString(((LwwLong) pt).getValue());
		}else if(pt instanceof LwwLogicalTimestamp){
			return StringOperations.addDoubleQuoteToHeadTail(((LwwLogicalTimestamp) pt).getLogicalTimeStamp().getValueString());
		}else if(pt instanceof LwwString){
			return StringOperations.addDoubleQuoteToHeadTail(((LwwString) pt).getValue());
		}else if(pt instanceof NormalDateTime){
			return StringOperations.addDoubleQuoteToHeadTail(DatabaseFunction.convertTimestampToDateStr(dateFormat, ((NormalDateTime) pt).getValue()));
		}else if(pt instanceof NormalDouble){
			return Double.toString(((NormalDouble) pt).getValue());
		}else if(pt instanceof NormalFloat){
			return Float.toString(((NormalFloat) pt).getValue());
		}else if(pt instanceof NormalInteger){
			return Integer.toString(((NormalInteger) pt).getValue());
		}else if(pt instanceof NormalLong){
			return Long.toString(((NormalLong) pt).getValue());
		}else if(pt instanceof NormalString){
			return StringOperations.addDoubleQuoteToHeadTail(((NormalString) pt).getValue());
		}else if(pt instanceof NumberDeltaDateTime){
			return StringOperations.addDoubleQuoteToHeadTail(DatabaseFunction.convertTimestampToDateStr(dateFormat, ((NumberDeltaDateTime) pt).getDelta()));
		}else if(pt instanceof NumberDeltaDouble){
			return Double.toString(((NumberDeltaDouble) pt).getDelta());
		}else if(pt instanceof NumberDeltaFloat){
			return Float.toString(((NumberDeltaFloat) pt).getDelta());
		}else if(pt instanceof NumberDeltaInteger){
			return Integer.toString(((NumberDeltaInteger) pt).getDelta());
		}else{
			throw new RuntimeException("Can not find your PrimitiveType");
		}
	}
	
	/**
	 * Checks if is last writer win.
	 *
	 * @param pt the pt
	 * @return true, if is last writer win
	 */
	public static boolean isLastWriterWin(PrimitiveType pt) {
		if(pt instanceof LwwDateTime || pt instanceof LwwDouble 
				|| pt instanceof LwwFloat || pt instanceof LwwInteger 
				|| pt instanceof LwwLogicalTimestamp || pt instanceof LwwString) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if is number delta.
	 *
	 * @param pt the pt
	 * @return true, if is number delta
	 */
	public static boolean isNumberDelta(PrimitiveType pt) {
		if(pt instanceof NumberDeltaDateTime || pt instanceof NumberDeltaDouble
				|| pt instanceof NumberDeltaFloat || pt instanceof NumberDeltaInteger) {
			return true;
		}
		return false;
	}
	
	/**
	 * Checks if is positive num.
	 *
	 * @param pt the pt
	 * @return true, if is positive num
	 */
	public static boolean isPositiveNum(PrimitiveType pt) {
		if(pt instanceof NumberDeltaDateTime){
			return (((NumberDeltaDateTime)pt).getDelta().getTime() >= 0);
		}else if(pt instanceof NumberDeltaDouble){
			return (((NumberDeltaDouble) pt).getDelta() >= 0.0);
		}else if(pt instanceof NumberDeltaFloat){
			return (((NumberDeltaFloat) pt).getDelta() >= 0.0);
		}else if(pt instanceof NumberDeltaInteger){
			return (((NumberDeltaInteger) pt).getDelta() >= 0);
		}else{
			throw new RuntimeException("Is Positive Checks wrong type " + pt.toString());
		}
	}
}
