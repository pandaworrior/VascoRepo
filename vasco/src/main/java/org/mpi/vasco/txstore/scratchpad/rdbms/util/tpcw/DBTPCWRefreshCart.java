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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;



public class DBTPCWRefreshCart extends DBGenericOperation{
	int shoppingcart_id;
	Vector<Integer> update_ids;
    Vector<Integer> update_quantities;
    Vector<Integer> remove_ids;
	
	protected DBTPCWRefreshCart(byte[] arr) {
		super(arr);
	}
	public static DBTPCWRefreshCart createOperation(DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		int shopping_id = dis.readInt();
		dos.writeInt(shopping_id);
		int u_id_size = dis.readInt();
		dos.writeInt(u_id_size);
		Vector<Integer> u_ids = new Vector<Integer>();
		while(u_id_size > 0){
			int tmp_id = dis.readInt();
			u_ids.add(new Integer(tmp_id));
			dos.writeInt(tmp_id);
			u_id_size--;
		}
		
		int u_qu_size = dis.readInt();
		dos.writeInt(u_qu_size);
		Vector<Integer> u_qus = new Vector<Integer>();
		while(u_qu_size > 0){
			int tmp_qu = dis.readInt();
			u_qus.add(new Integer(tmp_qu));
			dos.writeInt(tmp_qu);
			u_qu_size--;
		}
		
		//remove ids
		int r_id_size = dis.readInt();
		dos.writeInt(r_id_size);
		Vector<Integer> r_ids = new Vector<Integer>();
		while(r_id_size > 0){
			int tmp_id = dis.readInt();
			r_ids.add(new Integer(tmp_id));
			dos.writeInt(tmp_id);
			r_id_size--;
		}
		return new DBTPCWRefreshCart( baos.toByteArray(), shopping_id, u_ids, u_qus, r_ids);
		
	}
	public static DBTPCWRefreshCart createOperation(int shopping_id, Vector<Integer> u_ids, Vector<Integer> u_qus, Vector<Integer> r_ids) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream( baos);
		dos.writeByte(OP_GENERICOP);
		dos.writeByte(OP_GEN_TPCWREFRESHCART);
		
		dos.writeInt(shopping_id);
		//update ids
		dos.writeInt(u_ids.size());
		for(int i =0; i< u_ids.size(); i++)
			dos.writeInt(u_ids.elementAt(i).intValue());
		
		//update_qus
		dos.writeInt(u_qus.size());
		for(int i=0; i <u_qus.size();i++)
			dos.writeInt(u_qus.elementAt(i).intValue());
		
		//remove_ids
		dos.writeInt(r_ids.size());
		for(int i=0; i<r_ids.size();i++)
			dos.writeInt(r_ids.elementAt(i).intValue());
		return new DBTPCWRefreshCart( baos.toByteArray(), shopping_id, u_ids, u_qus, r_ids);
		
	}
	protected DBTPCWRefreshCart(byte[] arr, int shopping_id, Vector<Integer> u_ids, Vector<Integer> u_qus, Vector<Integer> r_ids) {
		super(arr);
		this.shoppingcart_id = shopping_id;
		this.update_ids = u_ids;
		this.update_quantities = u_qus;
		this.remove_ids = r_ids;
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
		Debug.println("Primary execution of refreshing cart");
		for(int i =0 ; i< this.update_ids.size(); i++){
			int tmp_id = this.update_ids.elementAt(i).intValue();
			int tmp_qu = this.update_quantities.elementAt(i).intValue();
			try {
					store.executeUpdate("UPDATE shopping_cart_line SET scl_qty = "+tmp_qu+" WHERE scl_sc_id = "+shoppingcart_id+" AND scl_i_id = "+tmp_id);
			} catch( Exception e) {
				System.err.println("Exception of primary refreshing cart update");
				e.printStackTrace();
				return 0;
			}
		}
		
		for(int i=0 ; i< this.remove_ids.size(); i++){
			int tmp_id = this.remove_ids.elementAt(i).intValue(); //can be optimized by groupping/batching commits
			try {
					store.executeUpdate("DELETE FROM shopping_cart_line WHERE scl_sc_id = "+shoppingcart_id+" AND scl_i_id = "+tmp_id);
			} catch( Exception e) {
				System.err.println("Exception of primary refreshing cart delete");
				e.printStackTrace();
				return 0;
			}
		}
		return 1;
	}

	@Override
	public void executeShadow(IDefDatabase store, IPrimaryExec exec) {
		Debug.println("Shadow execution of refreshing cart");
		for(int i =0 ; i< this.update_ids.size(); i++){
			int tmp_id = this.update_ids.elementAt(i).intValue();
			int tmp_qu = this.update_quantities.elementAt(i).intValue();
			try {
					store.executeUpdate("UPDATE shopping_cart_line SET scl_qty = "+tmp_qu+" WHERE scl_sc_id = "+shoppingcart_id+" AND scl_i_id = "+tmp_id);
			} catch( Exception e) {
				System.err.println("Exception of shadow refreshing cart operation update");
				e.printStackTrace();
				Debug.kill(e);
			}
		}
		
		for(int i=0 ; i< this.remove_ids.size(); i++){
			int tmp_id = this.remove_ids.elementAt(i).intValue(); //can be optimized by groupping/batching commits
			try {
				    store.executeUpdate("UPDATE shopping_cart_line SET scl_qty = "+0+" WHERE scl_sc_id = "+shoppingcart_id+" AND scl_i_id = "+tmp_id);
					store.executeUpdate("DELETE FROM shopping_cart_line WHERE scl_sc_id = "+shoppingcart_id+" AND scl_i_id = "+tmp_id);
			} catch( Exception e) {
				System.err.println("Exception of shadow refreshing cart operation delete");
				e.printStackTrace();
				Debug.kill(e);
			}
		}
	}

	@Override
	public void encode(DataOutputStream dos) throws IOException {
		dos.writeInt(shoppingcart_id);
		//update ids
		dos.writeInt(this.update_ids.size());
		for(int i =0; i< this.update_ids.size(); i++)
			dos.writeInt(this.update_ids.elementAt(i).intValue());
		
		//update_qus
		dos.writeInt(this.update_quantities.size());
		for(int i=0; i <this.update_quantities.size();i++)
			dos.writeInt(this.update_quantities.elementAt(i).intValue());
		
		//remove_ids
		dos.writeInt(this.remove_ids.size());
		for(int i=0; i<this.remove_ids.size();i++)
			dos.writeInt(this.remove_ids.elementAt(i).intValue());
		
	}

}
