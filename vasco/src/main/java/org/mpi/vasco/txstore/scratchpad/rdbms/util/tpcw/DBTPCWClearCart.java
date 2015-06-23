package org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IPrimaryExec;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBGenericOperation;
import org.mpi.vasco.util.debug.Debug;


public class DBTPCWClearCart extends DBGenericOperation {
	protected int shopping_id;
	protected Vector<Integer> remove_item_id;

	protected DBTPCWClearCart(byte[] arr) {
		super(arr);
	}

	public static DBTPCWClearCart createOperation(DataInputStream dis)
			throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int shopping_id = dis.readInt();
		dos.writeInt(shopping_id);
		int vector_size = dis.readInt();
		dos.writeInt(vector_size);
		Vector<Integer> remove_id = new Vector<Integer>();
		while(vector_size >0){
			int item_id = dis.readInt();
			remove_id.add(new Integer(item_id));
			dos.writeInt(item_id);
			vector_size--;
		}
		return new DBTPCWClearCart(baos.toByteArray(), shopping_id, remove_id);

	}

	public static DBTPCWClearCart createOperation(int shopping_id, Vector<Integer> remove_item) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeByte(OP_GENERICOP);
		dos.writeByte(OP_GEN_TPCWCLEARCART);
		dos.writeInt(shopping_id);
		dos.writeInt(remove_item.size());//encode size of the remove vector
		for(int i = 0; i < remove_item.size(); i++){
			dos.writeInt(remove_item.get(i).intValue());
		}
		return new DBTPCWClearCart(baos.toByteArray(), shopping_id, remove_item);

	}

	protected DBTPCWClearCart(byte[] arr, int shopping_id, Vector<Integer> remove_item) {
		super(arr);
		this.shopping_id = shopping_id;
		this.remove_item_id = remove_item;
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
		try {
			Debug.print("Primary clear cart execution");
			store.executeUpdate("DELETE FROM shopping_cart_line WHERE scl_sc_id = "+shopping_id);
			return 1;
		} catch (Exception e) {
			System.err
					.println("There was an exception of clearing cart");
			e.printStackTrace();
			return 0;
		}

	}

	@Override
	public void executeShadow(IDefDatabase store, IPrimaryExec exec) {
		try {
			Debug.print("Shadow clear cart execution");
			if(remove_item_id.size() > 0){
			String item_string = " (";
			for(int i = 0; i < remove_item_id.size() - 1; i++){
				item_string += remove_item_id.elementAt(i).intValue() + ",";
			}
			item_string += remove_item_id.elementAt(remove_item_id.size() - 1) + " )";
			String sqlQuery = "UPDATE shopping_cart_line SET scl_qty=0 WHERE scl_sc_id = "+shopping_id ;
			sqlQuery += " and scl_i_id IN " + item_string + ";";
			Debug.println(sqlQuery);
			store.executeUpdate(sqlQuery);
			
			sqlQuery = "DELETE FROM shopping_cart_line WHERE scl_sc_id = "+shopping_id ;
			sqlQuery += " and scl_i_id IN " + item_string + ";";
			Debug.println(sqlQuery);
			store.executeUpdate(sqlQuery);
			}else{
				Debug.print("clear cart empty remove queue");
			}
		} catch (Exception e) {
			System.err
					.println("There was an exception when performing the shadow operation of clearing cart");
			Debug.kill(e);
		}
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(shopping_id);
		dos.writeInt(remove_item_id.size());
		for(int i = 0; i < remove_item_id.size();i++){
			dos.writeInt(remove_item_id.elementAt(i).intValue());
		}
		
	}

}
