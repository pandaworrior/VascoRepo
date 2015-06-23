/***************************************************************
Project name: georeplication
Class file name: DBSifterShd.java
Created at 11:05:43 AM by chengli

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
****************************************************************/

package org.mpi.vasco.txstore.scratchpad.rdbms.util;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.mpi.vasco.txstore.scratchpad.rdbms.DBCommitScratchpad;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.util.debug.Debug;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.DBOpEntry;
import org.mpi.vasco.sieve.runtimelogic.shadowoperationcreator.shadowoperation.ShadowOperation;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LogicalTimestamp;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LwwInteger;
import org.mpi.vasco.util.crdtlib.datatypes.primitivetypes.LwwLogicalTimestamp;
import org.mpi.vasco.util.crdtlib.dbannotationtypes.DatabaseDef;

/**
 * @author chengli
 *
 */
public class DBSifterShd extends DBShadowOperation{
	
	ShadowOperation shdOp;
	
	public static DBSifterShd createOperation(
			DataInputStream dis) throws IOException {
		//decode here
		Debug.println("Decode an updating SIEVE shadow operation");
		ShadowOperation tempOp = new ShadowOperation(dis);
		return new DBSifterShd(null, tempOp);
	}
	
	protected DBSifterShd(byte[] arr, ShadowOperation tempOp) {
		super(arr);
		shdOp = tempOp;
	}
	
	public static DBSifterShd createOperation(ShadowOperation op) throws IOException {
		Debug.println("Create an updating SIEVE shadow operation");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_SIFTER);
		byte[] arr = op.encodeShadowOperation();
		dos.write(arr);
		return new DBSifterShd(baos.toByteArray(), op);
	}

	/**
	 * @param arr
	 */
	public DBSifterShd(byte[] arr) {
		super(arr);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation#encode(java.io.DataOutputStream)
	 */
	@Override
	public void encode(DataOutputStream dos) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("should not come here");
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation#execute(org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase)
	 */
	@Override
	public int execute(IDatabase store) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation#executeShadow(org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase)
	 */
	@Override
	public void executeShadow(IDefDatabase iDefDatabase) throws SQLException {
		//get the right form of logicalclock and timestamp
		LwwLogicalTimestamp lwwLogicalTs = new LwwLogicalTimestamp(DBCommitScratchpad.SCRATCHPAD_COL_VV,
				new LogicalTimestamp(this.lc));
		LwwInteger lwwTs = new LwwInteger(DBCommitScratchpad.SCRATCHPAD_COL_TS,
				this.ts.getCount());
		
		//iterate all operations
		ArrayList<DBOpEntry> opList = this.shdOp.getOperationList();
		for(int listIndex = 0; listIndex < opList.size(); listIndex++) {
			DBOpEntry dbOp = opList.get(listIndex);
			switch(dbOp.getOpType()){
			case DatabaseDef.INSERT:
				if (!isRecordExist(iDefDatabase, dbOp)) {
					//execute insert
					String insertStr = dbOp.getInsertQuery(this.dateFormat, lwwTs, lwwLogicalTs);
					this.executeSql(iDefDatabase, insertStr);
				}else {
					//execute update
					String[] updateStrs = dbOp.getUpdateQuery(this.dateFormat, lwwTs, lwwLogicalTs);
					for(int i = 0; i < updateStrs.length; i++) {
						this.executeSql(iDefDatabase, updateStrs[i]);
					}
				}
				break;
			case DatabaseDef.UNIQUEINSERT:
				String uInsertStr = dbOp.getInsertQuery(this.dateFormat, lwwTs, lwwLogicalTs);
				this.executeSql(iDefDatabase, uInsertStr);
				break;
			case DatabaseDef.UPDATE:
				String[] updateStrs = dbOp.getUpdateQuery(this.dateFormat, lwwTs, lwwLogicalTs);
				for(int i = 0; i < updateStrs.length; i++) {
					this.executeSql(iDefDatabase, updateStrs[i]);
				}
				break;
			case DatabaseDef.DELETE:
				String deleteStr = dbOp.getDeleteQuery(this.dateFormat, lwwTs, lwwLogicalTs);
				this.executeSql(iDefDatabase, deleteStr);
				break;
				default:
					System.out.println("No such type of DBOpEntry " +dbOp.getOpType() );
					System.exit(-1);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation#isQuery()
	 */
	@Override
	public boolean isQuery() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation#registerIndividualOperations()
	 */
	@Override
	public boolean registerIndividualOperations() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private boolean isRecordExist(IDefDatabase iDefDatabase, DBOpEntry dbOp) {
		ResultSet res = null;
		String queryStr = dbOp.getSelectQuery(this.dateFormat);
		try {
			res = iDefDatabase.executeQuery(queryStr);
			if(res.next()){
				Debug.println("Record exists for "+ queryStr);
				return true;
			}
			res.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			Debug.println("select from table has some trouble " + queryStr);
			e1.printStackTrace();
		}
		Debug.println("Record doesn't exist for "+ queryStr);
		return false;
	}
	
	private void executeSql(IDefDatabase iDefDatabase, String updateStr) {
		try {
			iDefDatabase.executeOp(updateStr);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
			e.printStackTrace();
		}
	}

}
