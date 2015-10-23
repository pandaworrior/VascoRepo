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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.mpi.vasco.sieve.staticanalysis.templatecreator.template.Operation;
import org.mpi.vasco.util.commonfunc.StringOperations;
import org.mpi.vasco.util.crdtlib.datatypes.CrdtEncodeDecode;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LwwInteger;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LwwLogicalTimestamp;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.PrimitiveType;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.DatabaseDef;
import org.mpi.vasco.util.debug.Debug;

// TODO: Auto-generated Javadoc
/**
 * The Class DBOpEntry. This class stores a pair of
 * the type of DB op like update/insert/delete, and the
 * data parameter list, which is crdt objects.
 *
 * @author chengli
 */
public class DBOpEntry{
	
	/** The op type. */
	private byte opType;
	
	/** The db table name. */
	private String dbTableName;
	
	/** The primary keys. */
	private List<PrimitiveType> primaryKeys;
	
	/** The normal attributes. */
	private List<PrimitiveType> normalAttributes;
	
	/** The lww deleted flag. */
	private static String lwwDeletedFlag = "_SP_del";
	
	/**
	 * Instantiates a new dB op entry.
	 *
	 * @param opType the op type
	 * @param dbTableName the db table name
	 */
	public DBOpEntry(byte opType , String dbTableName) {
		this.setOpType(opType);
		this.setDbTableName(dbTableName);
		this.primaryKeys = new ArrayList<PrimitiveType>();
		this.normalAttributes = new ArrayList<PrimitiveType>();
	}

	/**
	 * Gets the op type.
	 *
	 * @return the opType
	 */
	public byte getOpType() {
		return opType;
	}

	/**
	 * Sets the op type.
	 *
	 * @param opType the opType to set
	 */
	public void setOpType(byte opType) {
		this.opType = opType;
	}

	/**
	 * Gets the db table name.
	 *
	 * @return the dbTableName
	 */
	public String getDbTableName() {
		return dbTableName;
	}

	/**
	 * Sets the db table name.
	 *
	 * @param dbTableName the dbTableName to set
	 */
	public void setDbTableName(String dbTableName) {
		this.dbTableName = dbTableName;
	}

	/**
	 * Gets the primary keys.
	 *
	 * @return the primaryKeys
	 */
	public List<PrimitiveType> getPrimaryKeys() {
		return primaryKeys;
	}

	/**
	 * Sets the primary keys.
	 *
	 * @param primaryKeys the primaryKeys to set
	 */
	public void setPrimaryKeys(List<PrimitiveType> primaryKeys) {
		this.primaryKeys = primaryKeys;
	}

	/**
	 * Gets the normal attributes.
	 *
	 * @return the normalAttributes
	 */
	public List<PrimitiveType> getNormalAttributes() {
		return normalAttributes;
	}

	/**
	 * Sets the normal attributes.
	 *
	 * @param normalAttributes the normalAttributes to set
	 */
	public void setNormalAttributes(List<PrimitiveType> normalAttributes) {
		this.normalAttributes = normalAttributes;
	}
	
	/**
	 * Adds the primary key.
	 *
	 * @param pt the pt
	 */
	public void addPrimaryKey(PrimitiveType pt) {
		this.primaryKeys.add(pt);
	}
	
	/**
	 * Adds the normal attribute.
	 *
	 * @param pt the pt
	 */
	public void addNormalAttribute(PrimitiveType pt) {
		this.normalAttributes.add(pt);
	}
	
	/**
	 * Gets the select query.
	 *
	 * @return the select query
	 */
	public String getSelectQuery(DateFormat dateFormat) {
		String _str = "SELECT * FROM " + this.getDbTableName() + getWhereClause(dateFormat);
		Debug.println("CRDT TRANS -> SELECT " + _str);
		return _str;
	}
	
	/**
	 * Gets the insert query.
	 *
	 * @param ts the ts
	 * @param lwwts the lwwts
	 * @return the insert query
	 */
	public String getInsertQuery(DateFormat dateFormat, LwwInteger ts, LwwLogicalTimestamp lwwts) {
		StringBuilder queryBuilder = new StringBuilder( "INSERT INTO ");
		queryBuilder.append(this.getDbTableName());
		queryBuilder.append("(");
		StringBuilder valueStrBuilder = new StringBuilder(" VALUES( ");
		Debug.println("Assembled string so far: " + queryBuilder.toString());
		for(int i = 0; i < this.getPrimaryKeys().size(); i++) {
			PrimitiveType pt = this.getPrimaryKeys().get(i);
			Debug.println("data name : " + pt.getDataName());
			if(i == 0){
				queryBuilder.append(pt.getDataName());
				valueStrBuilder.append(CrdtEncodeDecode.getValueString(dateFormat, pt));
			}else{
				queryBuilder.append(",");
				queryBuilder.append(pt.getDataName());
				valueStrBuilder.append(",");
				valueStrBuilder.append(CrdtEncodeDecode.getValueString(dateFormat, pt));
			}
		}
		
		for(int i = 0; i < this.getNormalAttributes().size(); i++) {
			PrimitiveType pt = this.getNormalAttributes().get(i);
			queryBuilder.append(",");
			queryBuilder.append(pt.getDataName());
			valueStrBuilder.append(",");
			valueStrBuilder.append(CrdtEncodeDecode.getValueString(dateFormat, pt));
		}
		queryBuilder.append(",");
		queryBuilder.append(lwwDeletedFlag);
		valueStrBuilder.append(", false");
		queryBuilder.append(",");
		queryBuilder.append(ts.getDataName());
		valueStrBuilder.append(",");
		valueStrBuilder.append(ts.getValue());
		queryBuilder.append(",");
		queryBuilder.append(lwwts.getDataName());
		valueStrBuilder.append(",");
		valueStrBuilder.append(CrdtEncodeDecode.getValueString(dateFormat, lwwts));
		queryBuilder.append(")");
		valueStrBuilder.append(")");
		String _str = queryBuilder.append(valueStrBuilder.toString()).toString();
		Debug.println("CRDT TRANS -> Insert " + _str);
		return _str;
	}
	
	/**
	 * Gets the update query.
	 *
	 * @param ts the ts
	 * @param lwwts the lwwts
	 * @return the update query
	 */
	public String[] getUpdateQuery(DateFormat dateFormat, LwwInteger ts, LwwLogicalTimestamp lwwts) {
		String[] queryStrs;
		StringBuilder lwwStrBl1 = new StringBuilder("UPDATE ");
		lwwStrBl1.append(this.getDbTableName());
		lwwStrBl1.append(" SET ");//last writer win
		StringBuilder normalStrBl2 = new StringBuilder("UPDATE ");
		normalStrBl2.append(this.getDbTableName());
		normalStrBl2.append(" SET ");//normal
		
		String whereStr = getWhereClause(dateFormat);
		
		boolean isContainedNormalAttribute = false;
		//split the normal attributes into two parts, one is the normal and one is the lww
		for(int i = 0; i < this.normalAttributes.size(); i++) {
			PrimitiveType pt = this.normalAttributes.get(i);
			if(CrdtEncodeDecode.isLastWriterWin(pt)) {
				lwwStrBl1.append(pt.getDataName());
				lwwStrBl1.append(" = ");
				lwwStrBl1.append(CrdtEncodeDecode.getValueString(dateFormat, pt));
				lwwStrBl1.append(" , "); 
			}else {
				if(!isContainedNormalAttribute){
					isContainedNormalAttribute = true;
				}
				normalStrBl2.append(pt.getDataName());
				// delta or not delta
				if(CrdtEncodeDecode.isNumberDelta(pt)) {
					normalStrBl2.append(" = ");
					normalStrBl2.append(pt.getDataName());
					if(CrdtEncodeDecode.isPositiveNum(pt)) {
						normalStrBl2.append(" + "); 
					}
				}else {
					normalStrBl2.append(" = "); 
				}
				normalStrBl2.append(CrdtEncodeDecode.getValueString(dateFormat, pt));
				normalStrBl2.append(",");
			}
		}
		
		lwwStrBl1.append(ts.getDataName());
		lwwStrBl1.append(" = ");
		lwwStrBl1.append(ts.getValue());
		lwwStrBl1.append(",");	
		lwwStrBl1.append(lwwts.getDataName());
		lwwStrBl1.append(" = ");
		lwwStrBl1.append(CrdtEncodeDecode.getValueString(dateFormat, lwwts));
		lwwStrBl1.append(",");
		lwwStrBl1.append(lwwDeletedFlag);
		lwwStrBl1.append(" = false ");
		lwwStrBl1.append(whereStr);
		lwwStrBl1.append(" AND ");
		lwwStrBl1.append(ts.getDataName());
		lwwStrBl1.append(" <= ");
		lwwStrBl1.append(ts.getValue()) ;
		
		if(isContainedNormalAttribute) {
			normalStrBl2.deleteCharAt(normalStrBl2.length()-1);
			normalStrBl2.append(whereStr);
			queryStrs = new String[2];
			queryStrs[0] = lwwStrBl1.toString();
			Debug.println("CRDT TRANS -> update lww " + queryStrs[0] );
			queryStrs[1] = normalStrBl2.toString();
			Debug.println("CRDT TRANS -> update normal " + queryStrs[1] );
		}else {
			queryStrs = new String[1];
			queryStrs[0] = lwwStrBl1.toString();
			Debug.println("CRDT TRANS -> update lww " + queryStrs[0] );
		}
		return queryStrs;
	}
	
	/**
	 * Gets the where clause.
	 *
	 * @return the where clause
	 */
	public String getWhereClause(DateFormat dateFormat) {
		StringBuilder whereBuilder = new StringBuilder(" WHERE ");
		for(int i = 0 ; i < this.getPrimaryKeys().size(); i++) {
			PrimitiveType pt = this.getPrimaryKeys().get(i);
			if(i == 0){
				whereBuilder.append(pt.getDataName());
				whereBuilder.append(" = ");
				whereBuilder.append(CrdtEncodeDecode.getValueString(dateFormat, pt));
			}else{
				whereBuilder.append(" AND ");
				whereBuilder.append(pt.getDataName());
				whereBuilder.append(" = ");
				whereBuilder.append(CrdtEncodeDecode.getValueString(dateFormat, pt));
			}
		}
		String _str = whereBuilder.toString();
		Debug.println("CRDT TRANS -> where " + _str);
		return _str;
	}
	
	/**
	 * Gets the delete query.
	 *
	 * @param ts the ts
	 * @param lwwts the lwwts
	 * @return the delete query
	 */
	public String getDeleteQuery(DateFormat dateFormat, LwwInteger ts, LwwLogicalTimestamp lwwts) {
		StringBuilder queryStrBuilder = new StringBuilder("UPDATE ");
		queryStrBuilder.append(this.getDbTableName());
		queryStrBuilder.append(" SET ");
		queryStrBuilder.append(ts.getDataName());
		queryStrBuilder.append(" = ");
		queryStrBuilder.append(ts.getValue());
		queryStrBuilder.append(",");	
		queryStrBuilder.append(lwwts.getDataName());
		queryStrBuilder.append(" = ");
		queryStrBuilder.append(CrdtEncodeDecode.getValueString(dateFormat, lwwts));
		queryStrBuilder.append(",") ;
		queryStrBuilder.append(lwwDeletedFlag);
		queryStrBuilder.append(" = true ");
		queryStrBuilder.append(getWhereClause(dateFormat));
		queryStrBuilder.append(" AND ");
		queryStrBuilder.append(ts.getDataName());
		queryStrBuilder.append(" <= ");
		queryStrBuilder.append(ts.getValue()) ;
		String queryStr = queryStrBuilder.toString();
		Debug.println("CRDT TRANS -> delete " + queryStr );
		return queryStr;
	}
	
	/**
	 * Gets the attribute by given name.
	 *
	 * @param fieldName the field name
	 * @return the attribute by given name
	 */
	public PrimitiveType getAttributeByGivenName(String fieldName) {
		for(PrimitiveType pt : this.getPrimaryKeys()) {
			if(pt.getDataName().equals(fieldName)) {
				return pt;
			}
		}
		for(PrimitiveType pt : this.getNormalAttributes()) {
			if(pt.getDataName().equals(fieldName)) {
				return pt;
			}
		}
		throw new RuntimeException("You cannot find a pt named by " + fieldName + " in DBOpEntry " + this.toString());
	}
	
	/**
	 * Compute finger print.
	 *
	 * @param md the md
	 * @return the string
	 */
	public String computeFingerPrint(MessageDigest md) {
		StringBuilder idStrBuilder = new StringBuilder(this.getDbTableName());
		idStrBuilder.append(".");
		idStrBuilder.append(DatabaseDef.getDBOpName(this.getOpType()));
		switch(this.getOpType()) {
		case DatabaseDef.DELETE:
		case DatabaseDef.INSERT:
		case DatabaseDef.UNIQUEINSERT:
			break;
		case DatabaseDef.UPDATE:
			for(int i = 0; i < this.getNormalAttributes().size(); i++) {
				idStrBuilder.append(".");
				idStrBuilder.append(this.getNormalAttributes().get(i).getDataName());
			}
			break;
			default:
				System.out.println("No such database operation type exists " + this.getOpType());
				System.exit(-1);
		}
		try {
			String resultStr = idStrBuilder.toString();
			byte[] identifierStrInBytes = resultStr.getBytes("UTF-8");
			md.update(identifierStrInBytes);
			byte[] res = md.digest();
			return StringOperations.bytesToHex(res);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("You have trouble to generate a runtime identifier");
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String _str = this.getDbTableName() + " " + DatabaseDef.getDBOpName(this.getOpType()) + " \n";
		_str += "primary keys :\n";
		for(PrimitiveType pt : this.getPrimaryKeys()) {
			_str += pt.getDataName() + " ";
			_str += CrdtEncodeDecode.getString(pt) + "\n";
		}
		
		_str += "normal attributes : \n";
		for(PrimitiveType pt : this.getNormalAttributes()) {
			_str += pt.getDataName() + " ";
			_str += CrdtEncodeDecode.getString(pt) + "\n";
		}
		return _str;
	}
}
