package org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

public class DBRUBISShdStoreBuyNow2 extends DBShadowOperation{
	private int id;
	private int buyer_id;
	private int item_id;
	private int qty;
	private int currentQty;
	private String date;
	

	public static DBRUBISShdStoreBuyNow2 createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int id = dis.readInt();
		dos.writeInt(id);
		int buyer_id = dis.readInt();
		dos.writeInt(buyer_id);
		int item_id = dis.readInt();
		dos.writeInt(item_id);
		int qty = dis.readInt();
		dos.writeInt(qty);
		int currentQty = dis.readInt();
		dos.writeInt(currentQty);
		String date = dis.readUTF();
		dos.writeUTF(date);
		return new DBRUBISShdStoreBuyNow2( baos.toByteArray(), id, buyer_id,item_id, qty, currentQty, date);
		
	}
	public static DBRUBISShdStoreBuyNow2 createOperation(int id, int buyer_id, int item_id, int qty, int currentQty, String date) throws IOException {
		Debug.println("store buyNow 2 operation\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_RUBIS_STOREBUYNOW2);
		dos.writeInt(id);
		dos.writeInt(buyer_id);
		dos.writeInt(item_id);
		dos.writeInt(qty);
		dos.writeInt(currentQty);
		dos.writeUTF(date);
		Debug.println("store buyNow 2 operation done\n");
		return new DBRUBISShdStoreBuyNow2( baos.toByteArray(),id, buyer_id,item_id, qty, currentQty,date);
		
	}
	
	protected DBRUBISShdStoreBuyNow2(byte[] arr) {
		super(arr);
	}
	
	protected DBRUBISShdStoreBuyNow2(byte[] arr, int id, int buyer_id, int item_id, int qty, int currentQty, String date) {
		super(arr);
		this.id = id;
		this.buyer_id = buyer_id;
		this.item_id = item_id;
		this.qty = qty;
		this.currentQty = currentQty;
		this.date = date;
	}
	
	
	@Override
	public boolean isQuery() {
		return false;
	}

	@Override
	public boolean registerIndividualOperations() {
		return false;
	}

	@Override
	public int execute(IDatabase store) {
		return 0;
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		Debug.print("store buyNow 2 encode\n");
		dos.writeInt(id);
		dos.writeInt(buyer_id);
		dos.writeInt(item_id);
		dos.writeInt(qty);
		dos.writeInt(currentQty);
		dos.writeUTF(date);
	}
	
	
	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		try {
			Debug.println("Shadow store buyNow 2 execution " + id);
			iDefDatabase.executeOp("UPDATE items SET quantity=quantity - "+this.qty+" WHERE id="+this.item_id);
			iDefDatabase.executeUpdate("INSERT INTO buy_now(id,buyer_id,item_id,qty,date)" +
         	" VALUES ("+this.id+", \""
            + this.buyer_id
            + "\", \""
            + this.item_id
            + "\", \""
            + qty
            + "\", \""
            + this.date
            + "\")");
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow store buyNow 2 operation");
			e.printStackTrace();
		}
	}

}

