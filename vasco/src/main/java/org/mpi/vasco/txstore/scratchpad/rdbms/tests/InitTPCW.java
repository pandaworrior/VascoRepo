package org.mpi.vasco.txstore.scratchpad.rdbms.tests;
import org.mpi.vasco.util.debug.Debug;

import java.io.IOException;
import java.sql.*;
import java.util.Date;

import org.mpi.vasco.txstore.scratchpad.*;
import org.mpi.vasco.txstore.scratchpad.rdbms.DBScratchpad;

public class InitTPCW
{
	protected static Connection con;
	protected Statement stat;
	protected String url, user, pwd;
	
	public InitTPCW( String url, String user, String pwd) throws SQLException {
		this.url = url;
		this.user = user;
		this.pwd = pwd;
		init();
	}
	
	protected void init() throws SQLException {
		con = DriverManager.getConnection( url, user, pwd);
		con.setAutoCommit(false);
		stat = con.createStatement();
		
	}
	
    private void deleteTables(){
    	int i;
    	String[] tables = {"SCRATCHPAD","address", "author", "cc_xacts",
    			   "country", "customer", "item",
    			   "order_line", "orders",
    			   "shopping_cart", 
    			   "shopping_cart_line"};
    	int numTables = 11;

    	for(i = 0; i < numTables; i++){
    	    try {
    		//Delete each table listed in the tables array
    		PreparedStatement statement = con.prepareStatement
    		    ("DROP TABLE " + tables[i]);
    		statement.executeUpdate();
    		con.commit();
    		System.out.println("Dropped table " + tables[i]);
    	    } catch (java.lang.Exception ex) {
    		System.out.println("Already dropped table " + tables[i]);
    	    }
    	    
    	}
           System.out.println("Done deleting tables!");
        }
    private void createTables(){

    	try {
    	    PreparedStatement statement = con.prepareStatement
    		("CREATE TABLE address ( addr_id int not null, addr_street1 varchar(40), addr_street2 varchar(40), addr_city varchar(30), addr_state varchar(20), addr_zip varchar(10), addr_co_id int, PRIMARY KEY(addr_id), " +
    				"_SP_del bool default false," +
    				"_SP_ts int default 0," +
    				"_SP_clock varchar(100)" +
    				")");
    	    statement.executeUpdate();
    	    con.commit();
    	    System.out.println("Created table ADDRESS");
    	} catch (java.lang.Exception ex) {
    	    System.out.println("Unable to create table: ADDRESS");
    	    ex.printStackTrace();
    	    System.exit(1);
    	}

    	try {
    	    PreparedStatement statement = con.prepareStatement
    		("CREATE TABLE author ( a_id int not null, a_fname varchar(20), a_lname varchar(20), a_mname varchar(20), a_dob date, a_bio varchar(500), PRIMARY KEY(a_id), " +
    				"_SP_del bool default false," +
    				"_SP_ts int default 0," +
    				"_SP_clock varchar(100)" +
    				")");
    	    statement.executeUpdate();
    	    con.commit();
    	    System.out.println("Created table AUTHOR");
    	} catch (java.lang.Exception ex) {
    	    System.out.println("Unable to create table: AUTHOR");
    	    ex.printStackTrace();
    	    System.exit(1);
    	}
    	
    	try {
    	    PreparedStatement statement = con.prepareStatement
    		("CREATE TABLE cc_xacts ( cx_o_id int not null, cx_type varchar(10), cx_num varchar(20), cx_name varchar(30), cx_expire date, cx_auth_id char(15), cx_xact_amt double, cx_xact_date date, cx_co_id int, PRIMARY KEY(cx_o_id), " +
    				"_SP_del bool default false," +
    				"_SP_ts int default 0," +
    				"_SP_clock varchar(100)" +
    				")");
    	    statement.executeUpdate();
    	    con.commit();
    	    System.out.println("Created table CC_XACTS");
    	} catch (java.lang.Exception ex) {
    	    System.out.println("Unable to create table: CC_XACTS");
    	    ex.printStackTrace();
    	    System.exit(1);
    	}

    	try {
    	    PreparedStatement statement = con.prepareStatement
    		("CREATE TABLE country ( co_id int not null, co_name varchar(50), co_exchange double, co_currency varchar(18), PRIMARY KEY(co_id), " +
    				"_SP_del bool default false," +
    				"_SP_ts int default 0," +
    				"_SP_clock varchar(100)" +
    				")");
    	    statement.executeUpdate();
    	    con.commit();
    	    System.out.println("Created table COUNTRY");
    	} catch (java.lang.Exception ex) {
    	    System.out.println("Unable to create table: COUNTRY");
    	    ex.printStackTrace();
    	    System.exit(1);
    	}
    	try {
    	    PreparedStatement statement = con.prepareStatement
    		("CREATE TABLE customer ( c_id int not null, c_uname varchar(20), c_passwd varchar(20), c_fname varchar(17), c_lname varchar(17), c_addr_id int, c_phone varchar(18), c_email varchar(50), c_since date, c_last_login date, c_login timestamp, c_expiration timestamp, c_discount real, c_balance double, c_ytd_pmt double, c_birthdate date, c_data varchar(500), PRIMARY KEY(c_id), " +
    				"_SP_del bool default false," +
    				"_SP_ts int default 0," +
    				"_SP_clock varchar(100)" +
    				")");
    	    statement.executeUpdate();
    	    con.commit();
    	    System.out.println("Created table CUSTOMER");
    	} catch (java.lang.Exception ex) {
    	    System.out.println("Unable to create table: CUSTOMER");
    	    ex.printStackTrace();
    	    System.exit(1);
    	}

    	try {
    	    PreparedStatement statement = con.prepareStatement
    		("CREATE TABLE item ( i_id int not null, i_title varchar(60), i_a_id int, i_pub_date date, i_publisher varchar(60), i_subject varchar(60), i_desc varchar(500), i_related1 int, i_related2 int, i_related3 int, i_related4 int, i_related5 int, i_thumbnail varchar(40), i_image varchar(40), i_srp double, i_cost double, i_avail date, i_stock int, i_isbn char(13), i_page int, i_backing varchar(15), i_dimensions varchar(25), PRIMARY KEY(i_id), " +
    				"_SP_del bool default false," +
    				"_SP_ts int default 0," +
    				"_SP_clock varchar(100)" +
    				")");
    	    statement.executeUpdate();
    	    con.commit();
    	    System.out.println("Created table ITEM");
    	} catch (java.lang.Exception ex) {
    	    System.out.println("Unable to create table: ITEM");
    	    ex.printStackTrace();
    	    System.exit(1);
    	}

    	try {
    	    PreparedStatement statement = con.prepareStatement
    		("CREATE TABLE order_line ( ol_id int not null, ol_o_id int not null, ol_i_id int, ol_qty int, ol_discount double, ol_comments varchar(110), PRIMARY KEY(ol_id, ol_o_id), " +
    				"_SP_del bool default false," +
    				"_SP_ts int default 0," +
    				"_SP_clock varchar(100)" +
    				")");
    	    statement.executeUpdate();
    	    con.commit();
    	    System.out.println("Created table ORDER_LINE");
    	} catch (java.lang.Exception ex) {
    	    System.out.println("Unable to create table: ORDER_LINE");
    	    ex.printStackTrace();
    	    System.exit(1);
    	}

    	try {
    	    PreparedStatement statement = con.prepareStatement
    		("CREATE TABLE orders ( o_id int not null, o_c_id int, o_date date, o_sub_total double, o_tax double, o_total double, o_ship_type varchar(10), o_ship_date date, o_bill_addr_id int, o_ship_addr_id int, o_status varchar(15), PRIMARY KEY(o_id), " +
    				"_SP_del bool default false," +
    				"_SP_ts int default 0," +
    				"_SP_clock varchar(100)" +
    				")");
    	    statement.executeUpdate();
    	    con.commit();
    	    System.out.println("Created table ORDERS");
    	} catch (java.lang.Exception ex) {
    	    System.out.println("Unable to create table: ORDERS");
    	    ex.printStackTrace();
    	    System.exit(1);
    	}

    	try {
    	    PreparedStatement statement = con.prepareStatement
    		("CREATE TABLE shopping_cart ( sc_id int not null, sc_time timestamp, PRIMARY KEY(sc_id), " +
    				"_SP_del bool default false," +
    				"_SP_ts int default 0," +
    				"_SP_clock varchar(100)" +
    				")");
    	    statement.executeUpdate();
    	    con.commit();
    	    System.out.println("Created table SHOPPING_CART");
    	} catch (java.lang.Exception ex) {
    	    System.out.println("Unable to create table: SHOPPING_CART");
    	    ex.printStackTrace();
    	    System.exit(1);
    	}
    	try {
    	    PreparedStatement statement = con.prepareStatement
    		("CREATE TABLE shopping_cart_line ( scl_sc_id int not null, scl_qty int, scl_i_id int not null, PRIMARY KEY(scl_sc_id, scl_i_id), " +
    				"_SP_del bool default false," +
    				"_SP_ts int default 0," +
    				"_SP_clock varchar(100)" +
    				")");
    	    statement.executeUpdate();
    	    con.commit();
    	    System.out.println("Created table SHOPPING_CART_LINE");
    	} catch (java.lang.Exception ex) {
    	    System.out.println("Unable to create table: SHOPPING_CART_LINE");
    	    ex.printStackTrace();
    	    System.exit(1);
    	}
    	
            System.out.println("Done creating tables!");

        }

    public void loaddata(String file) throws IOException{
    	String str = "mysql -u sa -h localhost testtpcw <"+file;
    	String str2 = "/var/tmp/dcfp/mysql-5.5.15/bin/mysql --defaults-file=/var/tmp/dcfp/mysql-5.5.15/mysql-test/include/default_mysqld.cnf -u sa";
//    	Runtime rt = Runtime.getRuntime();
//    	Process pr = rt.exec(str);
    	System.out.println("populate database: "+str+"\n"+str2);
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://localhost:53306/testtpcw";
		String user = "sa";
		String pwd = "";
		String file = "";
		if( args.length > 0)
			file = args[0];
		
		InitTPCW db = new InitTPCW( url, user, pwd); 
		db.deleteTables();
		db.createTables();
		db.loaddata(file);
		} catch( Exception e) {
			e.printStackTrace();
		}

	}

}

