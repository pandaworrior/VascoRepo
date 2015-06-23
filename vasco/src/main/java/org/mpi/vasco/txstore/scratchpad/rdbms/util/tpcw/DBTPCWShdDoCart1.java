/**
 * only insert
 */
package org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

public class DBTPCWShdDoCart1 extends DBShadowOperation {
	int shopping_id;
	int insert_i_id;
	Date access_time;

	public static DBTPCWShdDoCart1 createOperation(
			DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int shopping_id = dis.readInt();
		dos.writeInt(shopping_id);
		int insert_i_id = dis.readInt();
		dos.writeInt(insert_i_id);
		long access_time = dis.readLong();
		dos.writeLong(access_time);
		return new DBTPCWShdDoCart1(baos.toByteArray(), shopping_id, insert_i_id,access_time);

	}

	public static DBTPCWShdDoCart1 createOperation(int shopping_id,
			int insert_i_id, long access_time) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_TPCW_DOCART1);
		dos.writeInt(shopping_id);
		dos.writeInt(insert_i_id);
		dos.writeLong(access_time);
		return new DBTPCWShdDoCart1(baos.toByteArray(), shopping_id, insert_i_id,access_time);

	}

	protected DBTPCWShdDoCart1(byte[] arr, int shopping_id,
			int insert_i_id, long access_time) {
		super(arr);
		this.shopping_id =shopping_id ; 
		this.insert_i_id = insert_i_id;
		this.access_time = new Date(access_time);

	}

	public DBTPCWShdDoCart1(byte[] arr) {
		super(arr);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public int execute(IDatabase store) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		//read cart from database
		String updateStr = "";
		ResultSet res = null;
		try {
			//res = iDefDatabase.executeQuery("SELECT scl_i_id FROM shopping_cart_line WHERE scl_sc_id = "+shopping_id + " AND _SP_del = 0 ");
			res = iDefDatabase.executeQuery("SELECT scl_i_id FROM shopping_cart_line WHERE scl_sc_id = "+shopping_id + " AND scl_i_id = " +insert_i_id);
			if(res.next()){
				updateStr = "UPDATE shopping_cart_line SET scl_qty = "+1+" WHERE scl_sc_id = "+shopping_id+" " +
				"AND scl_i_id = "+this.insert_i_id+" ";
			}else{
				Debug.println("item doesn't exist " + this.insert_i_id);
				updateStr = "INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES ("+shopping_id+","+1+","+this.insert_i_id+")";
			}
			res.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			Debug.println("select from shopping cart has some trouble");
			e1.printStackTrace();
		}
		
		try {
			iDefDatabase.executeUpdate(updateStr);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ScratchpadException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			iDefDatabase.executeUpdate("UPDATE shopping_cart SET sc_time = '"+access_time+"' WHERE sc_id = "+shopping_id);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
		}
	}

	@Override
	public boolean isQuery() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean registerIndividualOperations() {
		// TODO Auto-generated method stub
		return false;
	}

}
