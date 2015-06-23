package org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IPrimaryExec;
import org.mpi.vasco.txstore.scratchpad.rdbms.tests.DBTestCountOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBGenericOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSelectResult;
import org.mpi.vasco.util.debug.Debug;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;



public class DBTPCWAddItem extends DBGenericOperation{
	int shoppingcart_id;
	int item_id;
	int quantity;// can be delta or current value
	boolean incremental;
	
	
	protected DBTPCWAddItem(byte[] arr) {
		super(arr);
	}
	public static DBTPCWAddItem createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int shopping_id = dis.readInt();
		dos.writeInt(shopping_id);
		int item_id = dis.readInt();
		dos.writeInt(item_id);
		int item_quantity = dis.readInt();
		dos.writeInt(item_quantity);
		boolean isIncremental = dis.readBoolean();
		dos.writeBoolean(isIncremental);

		return new DBTPCWAddItem( baos.toByteArray(), shopping_id, item_id,item_quantity,isIncremental);
		
	}
	public static DBTPCWAddItem createOperation(int shopping_id, int item_id, int item_quantity, boolean flag) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_GENERICOP);
		dos.writeByte(OP_GEN_TPCWADDITEM);
		dos.writeInt(shopping_id);
		dos.writeInt(item_id);
		dos.writeInt(item_quantity);
		dos.writeBoolean(flag);
		return new DBTPCWAddItem( baos.toByteArray(), shopping_id, item_id,item_quantity,flag);
		
	}
	protected DBTPCWAddItem(byte[] arr, int shopping_id, int item_id, int item_quantity, boolean flag) {
		super(arr);
		this.shoppingcart_id = shopping_id;
		this.item_id = item_id;
		this.quantity=item_quantity;
		this.incremental = flag;
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
		Debug.print("Primary execution of adding item");
		try{
			if(this.incremental){
				store.executeUpdate("UPDATE shopping_cart_line SET scl_qty = scl_qty +"+quantity+" WHERE scl_sc_id = "+shoppingcart_id+"  AND scl_i_id = "+item_id);
			}else{
				store.executeUpdate("INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES ("+shoppingcart_id+","+quantity+","+item_id+")");
			}
			return 1;
	    }catch(Exception e){
	    	System.err.println("Exception from primary operation of adding item");
	    	e.printStackTrace();
	    	return 0;
	    }
	}

	@Override
	public void executeShadow(IDefDatabase store, IPrimaryExec exec) {
		Debug.print("Shadow execution of adding item");
		try{
			//DBSelectResult res = (DBSelectResult) store.executeQuery("SELECT * FROM shopping_cart_line WHERE scl_sc_id = "+shoppingcart_id+" AND scl_i_id = "+item_id);
			ResultSet res = store.executeQuery("SELECT scl_i_id FROM shopping_cart_line WHERE scl_sc_id = "+shoppingcart_id+" AND scl_i_id = "+item_id);
			if(res.next()){
				if(this.incremental){
					store.executeUpdate("UPDATE shopping_cart_line SET scl_qty = scl_qty +"+quantity+" WHERE scl_sc_id = "+shoppingcart_id+"  AND scl_i_id = "+item_id);
				}else{//lww
					store.executeUpdate("UPDATE shopping_cart_line SET scl_qty = "+quantity+" WHERE scl_sc_id = "+shoppingcart_id+"  AND scl_i_id = "+item_id);
				}
			}
			else{//new insert
				store.executeUpdate("INSERT into shopping_cart_line (scl_sc_id, scl_qty, scl_i_id) VALUES ("+shoppingcart_id+","+quantity+","+item_id+")");
			}
	    }catch(Exception e){
	    	System.err.println("Exception from shadow operation of adding item");
	    	e.printStackTrace();
	    	Debug.kill(e);
	    }

	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(shoppingcart_id);
		dos.writeInt(item_id);
		dos.writeInt(quantity);
		dos.writeBoolean(incremental);
		
	}

}
