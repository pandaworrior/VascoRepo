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
package org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;


import org.mpi.vasco.util.crdtlib.datatypes.CrdtEncodeDecode;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.*;
/*
import util.crdtlib.datatypes.primitivetypes.LwwBoolean;
import util.crdtlib.datatypes.primitivetypes.LwwDateTime;
import util.crdtlib.datatypes.primitivetypes.LwwDouble;
import util.crdtlib.datatypes.primitivetypes.LwwFloat;
import util.crdtlib.datatypes.primitivetypes.LwwInteger;
import util.crdtlib.datatypes.primitivetypes.LwwLogicalTimestamp;
import util.crdtlib.datatypes.primitivetypes.LwwString;
import util.crdtlib.datatypes.primitivetypes.NormalBoolean;
import util.crdtlib.datatypes.primitivetypes.NormalDateTime;
import util.crdtlib.datatypes.primitivetypes.NormalDouble;
import util.crdtlib.datatypes.primitivetypes.NormalFloat;
import util.crdtlib.datatypes.primitivetypes.NormalInteger;
import util.crdtlib.datatypes.primitivetypes.NormalString;
import util.crdtlib.datatypes.primitivetypes.NumberDeltaDateTime;
import util.crdtlib.datatypes.primitivetypes.NumberDeltaDouble;
import util.crdtlib.datatypes.primitivetypes.NumberDeltaFloat;
import util.crdtlib.datatypes.primitivetypes.NumberDeltaInteger;
import util.crdtlib.datatypes.primitivetypes.PrimitiveType;
*/
import org.mpi.vasco.util.debug.Debug;

/**
 * The Class ShadowOperation.
 */
public class ShadowOperation {
	
	/** The operation list. */
	ArrayList<DBOpEntry> operationList = null;
	
	/**
	 * Instantiates a new shadow op template.
	 *
	 */
	public ShadowOperation(){
		this.operationList = new ArrayList<DBOpEntry>();
	}
	
	/**
	 * Instantiates a new shadow operation.
	 *
	 * @param dis the dis
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ShadowOperation(DataInputStream dis) throws IOException {
		this.operationList = new ArrayList<DBOpEntry>();
		int operationCount =dis.readInt();
		for(int i = 0; i < operationCount; i++) {
			//decode every DBOperationEntry
			byte opType = dis.readByte();
			String tableName = dis.readUTF();
			DBOpEntry dbOpEntry = this.decodeDBOpEntry(opType, tableName, dis);
			this.addOperation(dbOpEntry);
		}
	}
	
	/**
	 * Encode shadow operation.
	 *
	 * @return the byte[]
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public byte[] encodeShadowOperation() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(this.getOperationCount());
		for(int i = 0; i < this.getOperationCount(); i++) {
			DBOpEntry dbOp = this.getOperationList().get(i);
			this.encodeDBOpEntry(dbOp, dos);
		}
		return baos.toByteArray();
	}
	
	/**
	 * Decode db op entry.
	 *
	 * @param opType the op type
	 * @param tableName the table name
	 * @param dis the dis
	 * @return the dB op entry
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public DBOpEntry decodeDBOpEntry(byte opType, String tableName, DataInputStream dis) throws IOException {
		DBOpEntry dbOpEntry = new DBOpEntry(opType, tableName);
		int primaryKeyCount = dis.readInt();
		Debug.println("Decode " + primaryKeyCount + " primary keys");
		for(int j = 0; j < primaryKeyCount; j++) {
			PrimitiveType pt = this.decodePrimitiveType(dis);
			dbOpEntry.addPrimaryKey(pt);
		}
		int normalAttributeCount = dis.readInt();
		Debug.println("Decode " + normalAttributeCount + "  normal attributes");
		for(int k = 0; k< normalAttributeCount; k++) {
			PrimitiveType pt = this.decodePrimitiveType(dis);
			dbOpEntry.addNormalAttribute(pt);
		}
		return dbOpEntry;
	}
	
	/**
	 * Encode db op entry.
	 *
	 * @param dbOpEntry the db op entry
	 * @param dos the dos
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void encodeDBOpEntry(DBOpEntry dbOpEntry, DataOutputStream dos) throws IOException {
		dos.writeByte(dbOpEntry.getOpType());
		dos.writeUTF(dbOpEntry.getDbTableName());
		int primaryKeyCount = dbOpEntry.getPrimaryKeys().size();
		Debug.println("Encode " + primaryKeyCount + " primary keys");
		dos.writeInt(primaryKeyCount);
		for(int j = 0; j< primaryKeyCount; j++) {
			this.encodePrimitiveType(dbOpEntry.getPrimaryKeys().get(j), dos);
		}
		int normalAttributeCount = dbOpEntry.getNormalAttributes().size();
		Debug.println("Encode " + normalAttributeCount + " normal attributes");
		dos.writeInt(normalAttributeCount);
		for(int k = 0; k < normalAttributeCount; k++) {
			this.encodePrimitiveType(dbOpEntry.getNormalAttributes().get(k), dos);
		}
	}
	
	/**
	 * Decode primitive type.
	 *
	 * @param dis the dis
	 * @return the primitive type
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public PrimitiveType decodePrimitiveType(DataInputStream dis) throws IOException {
		String dataName = dis.readUTF();
		Debug.println("decode the primitive type data " +dataName);
		byte crdtPrimitiveType = dis.readByte();
		switch(crdtPrimitiveType) {
		case CrdtEncodeDecode.LWWDATETIME:
			long ts = dis.readLong();
			return new LwwDateTime(dataName, ts);
		case CrdtEncodeDecode.LWWBOOLEAN:
			boolean boolValue = dis.readBoolean();
			return new LwwBoolean(dataName, boolValue);
		case CrdtEncodeDecode.LWWDOUBLE:
			double doubleValue = dis.readDouble();
			return new LwwDouble(dataName, doubleValue);
		case CrdtEncodeDecode.LWWFLOAT:
			float floatValue = dis.readFloat();
			return new LwwFloat(dataName, floatValue);
		case CrdtEncodeDecode.LWWINTEGER:
			long intValue = dis.readLong();
			return new LwwInteger(dataName, intValue);
		case CrdtEncodeDecode.LWWLONG:
			long longValue = dis.readLong();
			return new LwwLong(dataName, longValue);
		case CrdtEncodeDecode.LWWLOGICTIMESTAMP:
			return null;
		case CrdtEncodeDecode.LWWSTRING:
			return new LwwString(dataName, dis.readUTF());
		case CrdtEncodeDecode.NORMALDATETIME:
			long normalTs = dis.readLong();
			return new NormalDateTime(dataName, normalTs);
		case CrdtEncodeDecode.NORMALBOOLEAN:
			boolean nBoolValue = dis.readBoolean();
			return new NormalBoolean(dataName, nBoolValue);
		case CrdtEncodeDecode.NORMALDOUBLE:
			double nDoubleValue = dis.readDouble();
			return new NormalDouble(dataName, nDoubleValue);
		case CrdtEncodeDecode.NORMALFLOAT:
			float nFloatValue = dis.readFloat();
			return new NormalFloat(dataName, nFloatValue);
		case CrdtEncodeDecode.NORMALINTEGER:
			int nIntValue = dis.readInt();
			return new NormalInteger(dataName, nIntValue);
		case CrdtEncodeDecode.NORMALLONG:
			long longValue1 = dis.readLong();
			return new NormalLong(dataName, longValue1);
		case CrdtEncodeDecode.NORMALSTRING:
			return new NormalString(dataName, dis.readUTF());
		case CrdtEncodeDecode.NUMBERDELTADATETIME:
			long deltaTs = dis.readLong();
			return new NumberDeltaDateTime(dataName, deltaTs);
		case CrdtEncodeDecode.NUMBERDELTADOUBLE:
			double deltaDValue = dis.readDouble();
			return new NumberDeltaDouble(dataName, deltaDValue);
		case CrdtEncodeDecode.NUMBERDELTAFLOAT:
			float deltaFValue = dis.readFloat();
			return new NumberDeltaFloat(dataName, deltaFValue);
		case CrdtEncodeDecode.NUMBERDELTAINTEGER:
			int deltaIValue = dis.readInt();
			return new NumberDeltaInteger(dataName, deltaIValue);
			default:
				throw new RuntimeException("the primitive type is not valid " + crdtPrimitiveType);
		}
	}
	
	/**
	 * Encode primitive type.
	 *
	 * @param pt the pt
	 * @param dos the dos
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void encodePrimitiveType(PrimitiveType pt, DataOutputStream dos) throws IOException {
		dos.writeUTF(pt.getDataName());
		Debug.println("encode the primitive type " + pt.getDataName());
		if(pt instanceof LwwDateTime) {
			dos.writeByte(CrdtEncodeDecode.LWWDATETIME);
			dos.writeLong(((LwwDateTime)pt).getValue().getTime());
		}else if( pt instanceof LwwBoolean) {
			dos.writeByte(CrdtEncodeDecode.LWWBOOLEAN);
			dos.writeBoolean(((LwwBoolean) pt).getValue());
		}else if( pt instanceof LwwDouble) {
			dos.writeByte(CrdtEncodeDecode.LWWDOUBLE);
			dos.writeDouble(((LwwDouble)pt).getValue());
		}else if(pt instanceof LwwFloat) {	
			dos.writeByte(CrdtEncodeDecode.LWWFLOAT);
			dos.writeFloat(((LwwFloat) pt).getValue());
		}else if(pt instanceof LwwInteger) {	
			dos.writeByte(CrdtEncodeDecode.LWWINTEGER);
			dos.writeLong(((LwwInteger) pt).getValue());
		}else if(pt instanceof LwwLong) {
			dos.writeByte(CrdtEncodeDecode.LWWLONG);
			dos.writeLong(((LwwLong) pt).getValue());
		}else if(pt instanceof LwwLogicalTimestamp) {
			throw new RuntimeException("should not be here");
		}else if(pt instanceof LwwString) {	
			dos.writeByte(CrdtEncodeDecode.LWWSTRING);
			dos.writeUTF(((LwwString) pt).getValue());
		}else if(pt instanceof NormalDateTime) {	
			dos.writeByte(CrdtEncodeDecode.NORMALDATETIME);
			dos.writeLong(((NormalDateTime)pt).getValue().getTime());
		}else if(pt instanceof NormalBoolean) {	
			dos.writeByte(CrdtEncodeDecode.NORMALBOOLEAN);
			dos.writeBoolean(((NormalBoolean) pt).getValue());
		}else if(pt instanceof NormalDouble) {	
			dos.writeByte(CrdtEncodeDecode.NORMALDOUBLE);
			dos.writeDouble(((NormalDouble)pt).getValue());
		}else if(pt instanceof NormalFloat) {	
			dos.writeByte(CrdtEncodeDecode.NORMALFLOAT);
			dos.writeFloat(((NormalFloat) pt).getValue());
		}else if(pt instanceof NormalInteger) {	
			dos.writeByte(CrdtEncodeDecode.NORMALINTEGER);
			dos.writeInt(((NormalInteger) pt).getValue());
		}else if(pt instanceof NormalLong) {	
			dos.writeByte(CrdtEncodeDecode.NORMALLONG);
			dos.writeLong(((NormalLong) pt).getValue());
		}else if(pt instanceof NormalString) {	
			dos.writeByte(CrdtEncodeDecode.NORMALSTRING);
			dos.writeUTF(((NormalString) pt).getValue());
		}else if(pt instanceof NumberDeltaDateTime) {	
			dos.writeByte(CrdtEncodeDecode.NUMBERDELTADATETIME);
			dos.writeLong(((NumberDeltaDateTime)pt).getDelta().getTime());
		}else if(pt instanceof NumberDeltaDouble) {	
			dos.writeByte(CrdtEncodeDecode.NUMBERDELTADOUBLE);
			dos.writeDouble(((NumberDeltaDouble)pt).getDelta());
		}else if(pt instanceof NumberDeltaFloat) {	
			dos.writeByte(CrdtEncodeDecode.NUMBERDELTAFLOAT);
			dos.writeFloat(((NumberDeltaFloat) pt).getDelta());
		}else if(pt instanceof NumberDeltaInteger){
			dos.writeByte(CrdtEncodeDecode.NUMBERDELTAINTEGER);
			dos.writeInt(((NumberDeltaInteger) pt).getDelta());
		}else {
				throw new RuntimeException("the primitive type is not valid " + pt.toString());
		}
	}
	
	/**
	 * Adds the operation.
	 *
	 * @param dbOpEntry the db op entry
	 */
	public void addOperation(DBOpEntry dbOpEntry){
		this.operationList.add(dbOpEntry);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	/**
	 * To string.
	 *
	 * @return the string
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String str = " operation count " + this.getOperationCount() + "\n";
		for(int i = 0 ; i < this.getOperationCount(); i++) {
			DBOpEntry dbOp = this.operationList.get(i);
			str += " op " + i + " " + dbOp.toString() + "\n";
		}
		return str;
	}

	/**
	 * Gets the operation list.
	 *
	 * @return the operationList
	 */
	public ArrayList<DBOpEntry> getOperationList() {
		return operationList;
	}

	/**
	 * Sets the operation list.
	 *
	 * @param operationList the operationList to set
	 */
	public void setOperationList(ArrayList<DBOpEntry> operationList) {
		this.operationList = operationList;
	}
	
	/**
	 * Gets the operation count.
	 *
	 * @return the operation count
	 */
	public int getOperationCount() {
		return this.operationList.size();
	}
	
	/**
	 * Clear.
	 */
	public void clear() {
		Debug.println("Clear up a runtime shadow operation");
		this.operationList.clear();
	}
	
	/**
	 * Checks if is empty.
	 *
	 * @return true, if is empty
	 */
	public boolean isEmpty() {
		return (this.operationList.size() == 0);
	}
}
