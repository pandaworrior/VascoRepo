package org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.util.Vector;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.util.debug.Debug;

/**
 * Red transaction, increased + no new address
 * 
 * @author chengli
 *
 */
public class DBTPCWShdDoBuyConfirm3 extends DBShadowOperation {
	int shopping_id;
	int customer_id;
	//order
	int o_id;
	Date o_date;
	double sc_sub_total;
	double sc_total;
	Date o_ship_date;
	int customer_addr_id;
	int ship_addr_id;
	
	//orderline
	Vector<Integer> it_id_v;
	Vector<Integer> it_q_v;
	Vector<String> it_c_v;//comment
	
	//modify stock
	Vector<Integer> it_s_v; //stock delta, should be positive 
	
	//credit card
	String cc_type;
	long cc_number;
	String cc_name;
	Date cc_expiry;
	Date cc_pay_date;
	String shipping;
	double c_discount;

	public static DBTPCWShdDoBuyConfirm3 createOperation(
			DataInputStream dis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		int shopping_id = dis.readInt();
		dos.writeInt(shopping_id);
		int customer_id = dis.readInt();
		dos.writeInt(customer_id);
		
		//order
		int o_id = dis.readInt();
		dos.writeInt(o_id);
		long o_date = dis.readLong();
		dos.writeLong(o_date);
		double sc_sub_total = dis.readDouble();
		dos.writeDouble(sc_sub_total);
		double sc_total = dis.readDouble();
		dos.writeDouble(sc_total);
		long o_ship_date = dis.readLong();
		dos.writeLong(o_ship_date);
		int customer_addr_id = dis.readInt();
		dos.writeInt(customer_addr_id);
		int ship_addr_id = dis.readInt();
		dos.writeInt(ship_addr_id);
		
		//orderline
		int vector_size = dis.readInt();
		dos.writeInt(vector_size);
		Vector<Integer> it_id_v = new Vector<Integer>();
		while(vector_size >0){
			int item_id = dis.readInt();
			it_id_v.add(new Integer(item_id));
			dos.writeInt(item_id);
			vector_size--;
		}
		vector_size = dis.readInt();
		dos.writeInt(vector_size);
		Vector<Integer> it_q_v = new Vector<Integer>();
		while(vector_size >0){
			int item_id = dis.readInt();
			it_q_v.add(new Integer(item_id));
			dos.writeInt(item_id);
			vector_size--;
		}
		vector_size = dis.readInt();
		dos.writeInt(vector_size);
		Vector<String> it_c_v = new Vector<String>();
		while(vector_size >0){
			String comment = dis.readUTF();
			it_c_v.add(comment);
			dos.writeUTF(comment);
			vector_size--;
		}
		
		//modify stock
		vector_size = dis.readInt();
		dos.writeInt(vector_size);
		Vector<Integer> it_s_v = new Vector<Integer>();
		while(vector_size >0){
			int item_id = dis.readInt();
			it_s_v.add(new Integer(item_id));
			dos.writeInt(item_id);
			vector_size--;
		}
		
		//credit card
		String cc_type = dis.readUTF();
		dos.writeUTF(cc_type);
		long cc_number = dis.readLong();
		dos.writeLong(cc_number);
		String cc_name = dis.readUTF();
		dos.writeUTF(cc_name);
		long cc_expiry = dis.readLong();
		dos.writeLong(cc_expiry);
		long cc_pay_date = dis.readLong();
		dos.writeLong(cc_pay_date);
		String shipping = dis.readUTF();
		dos.writeUTF(shipping);
		double c_discount = dis.readDouble();
		dos.writeDouble(c_discount);
		
		return new DBTPCWShdDoBuyConfirm3(baos.toByteArray(), shopping_id, customer_id, 
				o_id,  o_date,  sc_sub_total,  sc_total, o_ship_date, customer_addr_id, ship_addr_id,
				it_id_v,  it_q_v,  it_c_v,  it_s_v, cc_type,
				cc_number, cc_name, cc_expiry, cc_pay_date, shipping, c_discount);

	}

	public static DBTPCWShdDoBuyConfirm3 createOperation(		int shopping_id, int customer_id, 
			int o_id, long o_date, double sc_sub_total, double sc_total, long o_ship_date, int customer_addr_id, int ship_addr_id,
			Vector<Integer> it_id_v, Vector<Integer> it_q_v, Vector<String> it_c_v, Vector<Integer> it_s_v, String cc_type,
			long cc_number, String cc_name, long cc_expiry, long cc_pay_date, String shipping, double c_discount) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeByte(OP_SHADOWOP);
		dos.writeByte(OP_SHD_TPCW_DOBUYCONFIRM3);
		dos.writeInt(shopping_id);
		dos.writeInt(customer_id);
		
		//order
		dos.writeInt(o_id);
		dos.writeLong(o_date);
		dos.writeDouble(sc_sub_total);
		dos.writeDouble(sc_total);
		dos.writeLong(o_ship_date);
		dos.writeInt(customer_addr_id);
		dos.writeInt(ship_addr_id);
		
		//orderline
		dos.writeInt(it_id_v.size());
		for(int i = 0; i < it_id_v.size();i++){
			dos.writeInt(it_id_v.get(i));
		}
		dos.writeInt(it_q_v.size());
		for(int i = 0; i < it_q_v.size();i++){
			dos.writeInt(it_q_v.get(i));
		}
		dos.writeInt(it_c_v.size());
		for(int i = 0; i < it_c_v.size();i++){
			dos.writeUTF(it_c_v.get(i));
		}
		
		//modify stock
		dos.writeInt(it_s_v.size());
		for(int i = 0; i < it_s_v.size();i++){
			dos.writeInt(it_s_v.get(i));
		}
		
		//credit card
		dos.writeUTF(cc_type);
		dos.writeLong(cc_number);
		dos.writeUTF(cc_name);
		dos.writeLong(cc_expiry);
		dos.writeLong(cc_pay_date);
		dos.writeUTF(shipping);
		dos.writeDouble(c_discount);
		
		return new DBTPCWShdDoBuyConfirm3(baos.toByteArray(), shopping_id, customer_id, 
				o_id,  o_date,  sc_sub_total,  sc_total, o_ship_date, customer_addr_id, ship_addr_id,
				it_id_v,  it_q_v,  it_c_v,  it_s_v, cc_type,
				cc_number, cc_name, cc_expiry, cc_pay_date, shipping, c_discount);

	}

	protected DBTPCWShdDoBuyConfirm3(byte[] arr, 	int shopping_id, int customer_id, 
			int o_id, long o_date, double sc_sub_total, double sc_total, long o_ship_date, int customer_addr_id, int ship_addr_id,
			Vector<Integer> it_id_v, Vector<Integer> it_q_v, Vector<String> it_c_v, Vector<Integer> it_s_v, String cc_type,
			long cc_number, String cc_name, long cc_expiry, long cc_pay_date, String shipping, double c_discount) {
		super(arr);
		
		this.shopping_id = shopping_id;
		this.customer_id = customer_id;
		//order
		this.o_id = o_id;
		this.o_date = new Date(o_date);
		this.sc_sub_total = sc_sub_total;
		this.sc_total = sc_total;
		this.o_ship_date = new Date(o_ship_date);
		this.customer_addr_id = customer_addr_id;
		this.ship_addr_id = ship_addr_id;
		
		//orderline
		this.it_id_v = it_id_v;
		this.it_q_v = it_q_v;
		this.it_c_v = it_c_v;//comment
		
		//modify stock
		this.it_s_v = it_s_v; //stock delta
		
		//credit card
		this.cc_type = cc_type;
		this.cc_number = cc_number;
		this.cc_name = cc_name;
		this.cc_expiry = new Date(cc_expiry);
		this.cc_pay_date = new Date(cc_pay_date);
		this.shipping = shipping;
		this.c_discount = c_discount;
	}

	public DBTPCWShdDoBuyConfirm3(byte[] arr) {
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

		//insert into orders
		String orderQuery = "INSERT into orders (o_id, o_c_id, o_date, o_sub_total, " + 
		 "o_tax, o_total, o_ship_type, o_ship_date, " + 
		 "o_bill_addr_id, o_ship_addr_id, o_status) " + 
		 "VALUES ("+this.o_id+", "+this.customer_id+", '"+this.o_date+"', " +
		 		this.sc_sub_total+", 8.25, "+this.sc_total+", '"+this.shipping+"', '"+this.o_ship_date+
		 		"', "+this.customer_addr_id+", "+this.ship_addr_id+", 'Pending')";
		
		try {
			iDefDatabase.executeUpdate(orderQuery);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
			e.printStackTrace();
		}
		
		//insert into orderline
		//update stock
		for(int i = 0; i < this.it_id_v.size(); i++){
			String orderlineQuery ="INSERT into order_line (ol_id, ol_o_id, ol_i_id, ol_qty, ol_discount, ol_comments) " +
		 "VALUES ("+i+", "+this.o_id+", "+this.it_id_v.get(i).intValue()+", "+this.it_q_v.get(i).intValue()+", "+this.c_discount+", '"+this.it_c_v.get(i)+"')";
			int stockDelta = this.it_s_v.get(i).intValue();
			String stockQuery = "";
			stockQuery="UPDATE item SET i_stock = i_stock+"+stockDelta+" WHERE i_id = "+this.it_id_v.get(i).intValue();
			try {
				iDefDatabase.executeUpdate(orderlineQuery);
				iDefDatabase.executeOp(stockQuery);
			} catch( Exception e) {
				System.err.println("There was an exception when performing the shadow operation");
				e.printStackTrace();
			}
		}
		
		//insert credit card
		String creditQuery = "INSERT into cc_xacts (cx_o_id, cx_type, cx_num, cx_name, cx_expire, cx_xact_amt, cx_xact_date, cx_co_id) " + 
		 "VALUES ("+this.o_id+", '"+this.cc_type+"', "+this.cc_number+", '"+this.cc_name+"', '"+this.cc_expiry+"', "+this.sc_total+
		 ", '"+this.cc_pay_date+"', " + "(SELECT co_id FROM address, country WHERE addr_id = "+this.ship_addr_id+" AND addr_co_id = co_id))";
		try {
			iDefDatabase.executeUpdate(creditQuery);
		} catch( Exception e) {
			System.err.println("There was an exception when performing the shadow operation");
			e.printStackTrace();
		}
		//clear cart
		if(it_id_v.size() > 0){
			for(int i = 0; i < it_id_v.size(); i++){
				String sqlQuery = "DELETE FROM shopping_cart_line WHERE scl_sc_id = "+shopping_id ;
				sqlQuery += " and scl_i_id = " + it_id_v.elementAt(i).intValue() + ";";
				Debug.println(sqlQuery);
				try {
					iDefDatabase.executeUpdate(sqlQuery);
				} catch( Exception e) {
					System.err.println("There was an exception when performing the shadow operation");
				}
			}
		}else{
			Debug.print("clear cart empty remove queue");
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
