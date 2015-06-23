package org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;



public class DBRUBISShdRegisterItem extends DBShadowOperation{
	
	//FIELDS
	private int id;
	private String name;
	private String description;
	private float initial_price;
	private int quantity;
	private float reserve_price;
	private float buy_now;
	private String start_date;
	private String end_date;
	private int seller;
	private int category;
	
 	//METHODS
	/*
	 * decode the input streams to get all variables and create a new ShadowOperation Object
	 * */
	public static DBRUBISShdRegisterItem createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int id = dis.readInt();
		dos.writeInt(id);
		String name = dis.readUTF();
		dos.writeUTF(name);
		String description = dis.readUTF();
		dos.writeUTF(description);
		float initial_price = dis.readFloat();
		dos.writeFloat(initial_price);
		int quantity = dis.readInt();
		dos.writeInt(quantity);
		float reserve_price = dis.readFloat();
		dos.writeFloat(reserve_price);
		float buy_now = dis.readFloat();
		dos.writeFloat(buy_now);
		String start_date = dis.readUTF();
		dos.writeUTF(start_date);
		String end_date = dis.readUTF();
		dos.writeUTF(end_date);
		int seller = dis.readInt();
		dos.writeInt(seller);
		int category = dis.readInt();
		dos.writeInt(category);
		return new DBRUBISShdRegisterItem( baos.toByteArray(), id, name, description,
				initial_price,  quantity, reserve_price, buy_now,
				start_date, end_date, seller, category);
		
	}
	
	/*
	 * given the parameters, encode them and  create a new shadowoperation object to be shiped 
	 * over the network
	 *  
	 * */
	public static DBRUBISShdRegisterItem createOperation(int id, String name, String description,
			float initial_price,  int quantity, float reserve_price, float buy_now,
			String start_date, String end_date, int seller, int category) throws IOException {
		Debug.println("register item operation\n");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_RUBIS_REGISTERITEM);
		dos.writeInt(id);
		dos.writeUTF(name);
		dos.writeUTF(description);
		dos.writeFloat(initial_price);
		dos.writeInt(quantity);
		dos.writeFloat(reserve_price);
		dos.writeFloat(buy_now);
		dos.writeUTF(start_date);
		dos.writeUTF(end_date);
		dos.writeInt(seller);
		dos.writeInt(category);
		Debug.println("register item operation done\n");
		return new DBRUBISShdRegisterItem( baos.toByteArray(), id, name, description,
				initial_price,  quantity, reserve_price, buy_now,
				start_date, end_date, seller, category);
		
	}
	
	protected DBRUBISShdRegisterItem(byte[] arr) {
		super(arr);
	}
	
	protected DBRUBISShdRegisterItem(byte[] arr, int id, String name, String description,
	float initial_price,  int quantity, float reserve_price, float buy_now,
	String start_date, String end_date, int seller, int category) {
		super(arr);
		this.id = id;
		this.name = name;
		this.description = description;
		this.initial_price = initial_price;
		this.quantity = quantity;
		this.reserve_price = reserve_price;
		this.buy_now = buy_now;
		this.start_date = start_date;
		this.end_date = end_date;
		this.seller = seller;
		this.category = category;
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
		Debug.print("register item encode\n");
		dos.writeInt(id);
		dos.writeUTF(name);
		dos.writeUTF(description);
		dos.writeFloat(initial_price);
		dos.writeInt(quantity);
		dos.writeFloat(reserve_price);
		dos.writeFloat(buy_now);
		dos.writeUTF(start_date);
		dos.writeUTF(end_date);
		dos.writeInt(seller);
		dos.writeInt(category);
	}
	
	
	@Override
	public void executeShadow(IDefDatabase iDefDatabase) {
		// TODO Auto-generated method stub
		try {
			Debug.println("Shadow Register Item execution " + id);
			iDefDatabase.executeUpdate("INSERT INTO items" +
		        	 "(id,name,description,initial_price,quantity,reserve_price," +
		        	 "buy_now,nb_of_bids,max_bid,start_date,end_date,seller,category) " +
		        	 "VALUES ("+this.id+", \""
		              + name
		              + "\", \""
		              + description
		              + "\", \""
		              + this.initial_price
		              + "\", \""
		              + quantity
		              + "\", \""
		              + this.reserve_price
		              + "\", \""
		              + this.buy_now
		              + "\", 0, 0, \""
		              + this.start_date
		              + "\", \""
		              + this.end_date
		              + "\", \""
		              + this.seller
		              + "\", \""
		              + this.category+"\")");
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow register Item operation");
			e.printStackTrace();
		}
	}

}

