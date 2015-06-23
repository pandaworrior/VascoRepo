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

public class DBRUBISShdStoreBid1 extends DBShadowOperation{
	private int id;
	private int user_id;
	private int item_id;
	private int qty;
	private float bid;
	private float max_bid;
	private String date;
	

	public static DBRUBISShdStoreBid1 createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int id = dis.readInt();
		dos.writeInt(id);
		int user_id = dis.readInt();
		dos.writeInt(user_id);
		int item_id = dis.readInt();
		dos.writeInt(item_id);
		int qty = dis.readInt();
		dos.writeInt(qty);
		float bid = dis.readFloat();
		dos.writeFloat(bid);
		float max_bid = dis.readFloat();
		dos.writeFloat(max_bid);
		String date = dis.readUTF();
		dos.writeUTF(date);
		return new DBRUBISShdStoreBid1( baos.toByteArray(), id, user_id, item_id, qty, bid, max_bid, date);
		
	}
	public static DBRUBISShdStoreBid1 createOperation(int id, int user_id, int item_id, int qty, float bid, float max_bid, String date) throws IOException {
		Debug.println("store bid 1 operation\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_RUBIS_STOREBID1);
		dos.writeInt(id);
		dos.writeInt(user_id);
		dos.writeInt(item_id);
		dos.writeInt(qty);
		dos.writeFloat(bid);
		dos.writeFloat(max_bid);
		dos.writeUTF(date);
		Debug.println("store bid 1 operation done\n");
		return new DBRUBISShdStoreBid1( baos.toByteArray(),id, user_id, item_id, qty, bid, max_bid, date);
		
	}
	
	protected DBRUBISShdStoreBid1(byte[] arr) {
		super(arr);
	}
	
	protected DBRUBISShdStoreBid1(byte[] arr, int id, int user_id, int item_id, int qty, float bid, float max_bid, String date) {
		super(arr);
		this.id = id;
		this.user_id = user_id;
		this.item_id = item_id;
		this.qty = qty;
		this.bid = bid;
		this.max_bid = max_bid;
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
		Debug.print("store bid 1 encode\n");
		dos.writeInt(id);
		dos.writeInt(user_id);
		dos.writeInt(item_id);
		dos.writeInt(qty);
		dos.writeFloat(bid);
		dos.writeFloat(max_bid);
		dos.writeUTF(date);
	}
	
	
	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		try {
			Debug.println("Shadow store bid 1 execution " + id);
			iDefDatabase.executeUpdate("INSERT INTO bids(id,user_id,item_id,qty,bid,max_bid,date)" +
		       	  	" VALUES ("+this.id+", \""
		            + this.user_id
		            + "\", \""
		            + this.item_id
		            + "\", \""
		            + qty
		            + "\", \""
		            + bid
		            + "\", \""
		            + this.max_bid
		            + "\", \""
		            + this.date
		            + "\")");
			//update maxbid and nb_bids
			ResultSet rs = iDefDatabase.executeQuery("SELECT max_bid FROM items WHERE id="+this.item_id);
			if(rs.next()){
				float oldMaxBid = rs.getFloat("max_bid");
				if(bid>oldMaxBid){
					iDefDatabase.executeUpdate("UPDATE items SET max_bid="+this.bid+", nb_of_bids=nb_of_bids+1 WHERE id="+this.item_id);
				}else{
					iDefDatabase.executeUpdate("UPDATE items SET nb_of_bids=nb_of_bids+1 WHERE id="+this.item_id);
				}
			}
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow store bid 1 operation");
			e.printStackTrace();
		}
	}

}

