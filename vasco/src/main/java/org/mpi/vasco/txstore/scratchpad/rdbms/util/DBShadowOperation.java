package org.mpi.vasco.txstore.scratchpad.rdbms.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;

import org.mpi.vasco.txstore.scratchpad.rdbms.IDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDefDatabase;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.micro.DBMICROEMPTY;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.micro.DBMICROTX1;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.micro.DBMICROTX3;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.micro.DBMICROTX4;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.micro.DBMICROTX5;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.micro.DBMICROTXUNIQUETEST;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.quoddy.DBQUODDYShdAddToFollow;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.quoddy.DBQUODDYShdAddToFriend;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.quoddy.DBQUODDYShdConfirmFriend;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.quoddy.DBQUODDYShdEmpty;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.quoddy.DBQUODDYShdUpdateStatus;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis.DBRUBISShdEmpty;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis.DBRUBISShdRegisterItem;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis.DBRUBISShdRegisterUser1;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis.DBRUBISShdStoreBid1;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis.DBRUBISShdStoreBid2;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis.DBRUBISShdStoreBid3;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis.DBRUBISShdStoreBuyNow1;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis.DBRUBISShdStoreBuyNow2;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis.DBRUBISShdStoreComment1;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.rubis.DBRUBISShdStoreComment2;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdAdminUpdate;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdCreateEmptyCart;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdCreateNewCustomer1;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdCreateNewCustomer2;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdDoBuyConfirm1;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdDoBuyConfirm2;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdDoBuyConfirm3;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdDoBuyConfirm4;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdDoBuyConfirm5;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdDoBuyConfirm6;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdDoCart1;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdDoCart2;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdDoCart3;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdDoCart4;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdDoCart5;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdEmpty;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWShdRefreshSession;
import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSifterShd;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.TimeStamp;

public abstract class DBShadowOperation  extends DBOperation  {
	
	LogicalClock lc;
	TimeStamp ts;
	DateFormat dateFormat;
	

	public DBShadowOperation(byte[] arr) {
		super(arr);
		// TODO Auto-generated constructor stub
	}
	
	public void setTimestamp(LogicalClock l, TimeStamp t) {
		this.lc = l;
		this.ts = t;
	}
	
	public void setDateFormat(DateFormat dformat) {
		this.dateFormat = dformat;
	}
	
	
	public static final byte OP_SHD_TPCW_CREATEEMPTYCART = 80;
	public static final byte OP_SHD_TPCW_ADMINUPDATE = 81;
	public static final byte OP_SHD_TPCW_REFRESHSESSION = 82;
	public static final byte OP_SHD_TPCW_CREATENEWCUSTOMER1= 83;
	public static final byte OP_SHD_TPCW_CREATENEWCUSTOMER2= 84;
	public static final byte OP_SHD_TPCW_DOCART1= 85;
	public static final byte OP_SHD_TPCW_DOCART2= 86;
	public static final byte OP_SHD_TPCW_DOCART3= 87;
	public static final byte OP_SHD_TPCW_DOCART4= 88;
	public static final byte OP_SHD_TPCW_DOCART5= 89;
	public static final byte OP_SHD_TPCW_DOBUYCONFIRM1= 90;
	public static final byte OP_SHD_TPCW_DOBUYCONFIRM2= 91;
	public static final byte OP_SHD_TPCW_DOBUYCONFIRM3= 92;
	public static final byte OP_SHD_TPCW_DOBUYCONFIRM4= 93;
	public static final byte OP_SHD_TPCW_DOBUYCONFIRM5= 94;
	public static final byte OP_SHD_TPCW_DOBUYCONFIRM6= 95;
	public static final byte OP_SHD_TPCW_NONE = 96;
	public static final byte OP_SHD_MICRO1 = 97;
	public static final byte OP_SHD_MICRO2 = 98;
	public static final byte OP_SHD_MICRO3 = 99;
	public static final byte OP_SHD_MICRO4 = 100;
	public static final byte OP_SHD_NONE = 101;
	
	//TODO:could be removed
	public static final byte OP_SHD_TEST = 118;
	public static final byte OP_SHD_UNIQUE_TEST = 66;
	
	//RUBIS shadow operation type code
	public static final byte OP_SHD_RUBIS_NONE = 102;
	public static final byte OP_SHD_RUBIS_REGISTERUSER1 = 103;
	public static final byte OP_SHD_RUBIS_STORECOMMENT1 = 104;
	public static final byte OP_SHD_RUBIS_STORECOMMENT2 = 105;
	public static final byte OP_SHD_RUBIS_STOREBUYNOW1 = 106;
	public static final byte OP_SHD_RUBIS_STOREBUYNOW2 = 107;
	public static final byte OP_SHD_RUBIS_REGISTERITEM = 108;
	public static final byte OP_SHD_RUBIS_STOREBID1 = 109;
	public static final byte OP_SHD_RUBIS_STOREBID2 = 110;
	public static final byte OP_SHD_RUBIS_STOREBID3 = 111;
	
	//QUODDY shadow operation type code
	public static final byte OP_SHD_QUODDY_NONE = 112;
	public static final byte OP_SHD_QUODDY_UPDATESTATUS = 113;
	public static final byte OP_SHD_QUODDY_ADDTOFRIENDS = 114;
	public static final byte OP_SHD_QUODDY_COMFIRMFRIEND = 115;
	public static final byte OP_SHD_QUPDDY_FOLLOWSOMEBODY = 116;
	
	public static final byte OP_SHD_SIFTER = 124;
	
	public abstract boolean isQuery();

	/**
	 * Should return true if individual sql operations must be registered for
	 * reexecution. Otherwise, big operation is registered.
	 * 
	 * @return
	 */
	public abstract boolean registerIndividualOperations();

	/**
	 * Execute the code of the operations as primary execution
	 */
	public abstract int execute(IDatabase store);

	/**
	 * Execute the code of the operations as primary execution
	 */
	public abstract void executeShadow(IDefDatabase iDefDatabase) throws SQLException;

	public abstract void encode(DataOutputStream dos) throws IOException;

	public static DBOperation decodeShadow(DataInputStream dis)
			throws IOException {
		byte b = dis.readByte();
		Debug.println("decode shadow operation\n");
		if(b==OP_SHD_TPCW_CREATEEMPTYCART ){
			return DBTPCWShdCreateEmptyCart.createOperation(dis);
		}else if(b==OP_SHD_TPCW_ADMINUPDATE){
			return DBTPCWShdAdminUpdate.createOperation(dis);
		}else if(b==OP_SHD_TPCW_REFRESHSESSION){
			return DBTPCWShdRefreshSession.createOperation(dis);
		}else if(b==OP_SHD_TPCW_CREATENEWCUSTOMER1){
			return DBTPCWShdCreateNewCustomer1.createOperation(dis);
		}else if(b==OP_SHD_TPCW_CREATENEWCUSTOMER2){
			return DBTPCWShdCreateNewCustomer2.createOperation(dis);
		}else if(b==OP_SHD_TPCW_DOCART1){
			return DBTPCWShdDoCart1.createOperation(dis);
		}else if(b==OP_SHD_TPCW_DOCART2){
			return DBTPCWShdDoCart2.createOperation(dis);
		}else if(b==OP_SHD_TPCW_DOCART3){
			return DBTPCWShdDoCart3.createOperation(dis);
		}else if(b==OP_SHD_TPCW_DOCART4){
			return DBTPCWShdDoCart4.createOperation(dis);
		}else if(b==OP_SHD_TPCW_DOCART5){
			return DBTPCWShdDoCart5.createOperation(dis);
		}else if(b==OP_SHD_TPCW_DOBUYCONFIRM1){
			return DBTPCWShdDoBuyConfirm1.createOperation(dis);
		}else if(b==OP_SHD_TPCW_DOBUYCONFIRM2){
			return DBTPCWShdDoBuyConfirm2.createOperation(dis);
		}else if(b==OP_SHD_TPCW_DOBUYCONFIRM3){
			return DBTPCWShdDoBuyConfirm3.createOperation(dis);
		}else if(b==OP_SHD_TPCW_DOBUYCONFIRM4){
			return DBTPCWShdDoBuyConfirm4.createOperation(dis);
		}else if(b==OP_SHD_TPCW_DOBUYCONFIRM5){
			return DBTPCWShdDoBuyConfirm5.createOperation(dis);
		}else if(b==OP_SHD_TPCW_DOBUYCONFIRM6){
			return DBTPCWShdDoBuyConfirm6.createOperation(dis);
		}else if(b==OP_SHD_TPCW_NONE){
			return DBTPCWShdEmpty.createOperation(dis);
		}else if (b == OP_SHD_MICRO1) {
			return DBMICROTX1.createOperation(dis);
		} else if (b == OP_SHD_MICRO3){
			return DBMICROTX3.createOperation(dis);
		}else if (b == OP_SHD_MICRO4){
			return DBMICROTX4.createOperation(dis);
		}else if (b == OP_SHD_NONE){
			return DBMICROEMPTY.createOperation(dis);
		}else if(b == OP_SHD_RUBIS_NONE){
			return DBRUBISShdEmpty.createOperation(dis);
		}else if (b == OP_SHD_RUBIS_REGISTERUSER1){
			return DBRUBISShdRegisterUser1.createOperation(dis);
		}else if (b == OP_SHD_RUBIS_STORECOMMENT1){
			return DBRUBISShdStoreComment1.createOperation(dis);
		}else if (b == OP_SHD_RUBIS_STORECOMMENT2){
			return DBRUBISShdStoreComment2.createOperation(dis);
		}else if(b == OP_SHD_RUBIS_STOREBUYNOW1){
			return DBRUBISShdStoreBuyNow1.createOperation(dis);
		}else if(b == OP_SHD_RUBIS_STOREBUYNOW2){
			return DBRUBISShdStoreBuyNow2.createOperation(dis);
		}else if (b == OP_SHD_RUBIS_REGISTERITEM){
			return DBRUBISShdRegisterItem.createOperation(dis);
		}else if (b == OP_SHD_RUBIS_STOREBID1){
			return DBRUBISShdStoreBid1.createOperation(dis);
		}else if (b == OP_SHD_RUBIS_STOREBID2){
			return DBRUBISShdStoreBid2.createOperation(dis);
		}else if (b == OP_SHD_RUBIS_STOREBID3){
			return DBRUBISShdStoreBid3.createOperation(dis);
		}else if (b == OP_SHD_QUODDY_NONE){
			return DBQUODDYShdEmpty.createOperation(dis);
		}else if (b == OP_SHD_QUODDY_ADDTOFRIENDS){
			return DBQUODDYShdAddToFriend.createOperation(dis);
		}else if (b == OP_SHD_QUPDDY_FOLLOWSOMEBODY){
			return DBQUODDYShdAddToFollow.createOperation(dis);
		}else if (b == OP_SHD_QUODDY_COMFIRMFRIEND){
			return DBQUODDYShdConfirmFriend.createOperation(dis);
		}else if (b == OP_SHD_QUODDY_UPDATESTATUS){
			return DBQUODDYShdUpdateStatus.createOperation(dis);
		}else if (b == OP_SHD_TEST){
			return DBMICROTX5.createOperation(dis);
		}else if (b == OP_SHD_UNIQUE_TEST){
			return DBMICROTXUNIQUETEST.createOperation(dis);
		}else if(b == OP_SHD_SIFTER) {
			return DBSifterShd.createOperation(dis);
		}else
			throw new RuntimeException("Cannot encode type " + b);
	}

	public static void encodeShadow(DBOperation op0, DataOutputStream dos)
			throws IOException {
		Debug.println("encode shadow operation\n");
		if(op0 instanceof DBTPCWShdCreateEmptyCart ){
			dos.writeByte(OP_SHD_TPCW_CREATEEMPTYCART);
			((DBTPCWShdCreateEmptyCart) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdAdminUpdate){
			dos.writeByte(OP_SHD_TPCW_ADMINUPDATE);
			((DBTPCWShdAdminUpdate) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdRefreshSession){
			dos.writeByte(OP_SHD_TPCW_REFRESHSESSION);
			((DBTPCWShdRefreshSession) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdCreateNewCustomer1){
			dos.writeByte(OP_SHD_TPCW_CREATENEWCUSTOMER1);
			((DBTPCWShdCreateNewCustomer1) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdCreateNewCustomer2){
			dos.writeByte(OP_SHD_TPCW_CREATENEWCUSTOMER2);
			((DBTPCWShdCreateNewCustomer2) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdDoCart1){
			dos.writeByte(OP_SHD_TPCW_DOCART1);
			((DBTPCWShdDoCart1) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdDoCart2){
			dos.writeByte(OP_SHD_TPCW_DOCART2);
			((DBTPCWShdDoCart2) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdDoCart3){
			dos.writeByte(OP_SHD_TPCW_DOCART3);
			((DBTPCWShdDoCart3) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdDoCart4){
			dos.writeByte(OP_SHD_TPCW_DOCART4);
			((DBTPCWShdDoCart4) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdDoCart5){
			dos.writeByte(OP_SHD_TPCW_DOCART5);
			((DBTPCWShdDoCart5) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdDoBuyConfirm1){
			dos.writeByte(OP_SHD_TPCW_DOBUYCONFIRM1);
			((DBTPCWShdDoBuyConfirm1) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdDoBuyConfirm2){
			dos.writeByte(OP_SHD_TPCW_DOBUYCONFIRM2);
			((DBTPCWShdDoBuyConfirm2) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdDoBuyConfirm3){
			dos.writeByte(OP_SHD_TPCW_DOBUYCONFIRM3);
			((DBTPCWShdDoBuyConfirm3) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdDoBuyConfirm4){
			dos.writeByte(OP_SHD_TPCW_DOBUYCONFIRM4);
			((DBTPCWShdDoBuyConfirm4) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdDoBuyConfirm5){
			dos.writeByte(OP_SHD_TPCW_DOBUYCONFIRM5);
			((DBTPCWShdDoBuyConfirm5) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdDoBuyConfirm6){
			dos.writeByte(OP_SHD_TPCW_DOBUYCONFIRM6);
			((DBTPCWShdDoBuyConfirm6) op0).encode(dos);
		}else if(op0 instanceof DBTPCWShdEmpty){
			dos.writeByte(OP_SHD_TPCW_NONE);
			((DBTPCWShdEmpty) op0).encode(dos);
		}else if (op0 instanceof DBMICROTX1) {
			dos.writeByte(OP_SHD_MICRO1);
			((DBMICROTX1) op0).encode(dos);
		} else if (op0 instanceof DBMICROTX3){
			dos.writeByte(OP_SHD_MICRO3);
			((DBMICROTX3) op0).encode(dos);
		}else if (op0 instanceof DBMICROTX4){
			dos.writeByte(OP_SHD_MICRO4);
			((DBMICROTX4) op0).encode(dos);
		}else if  (op0 instanceof DBMICROEMPTY) {
			dos.writeByte(OP_SHD_NONE);
			((DBMICROEMPTY) op0).encode(dos);
		}else if (op0 instanceof DBRUBISShdEmpty){
			dos.writeByte(OP_SHD_RUBIS_NONE);
			((DBRUBISShdEmpty) op0).encode(dos);
		}else if (op0 instanceof DBRUBISShdRegisterUser1){
			dos.writeByte(OP_SHD_RUBIS_REGISTERUSER1);
			((DBRUBISShdRegisterUser1) op0).encode(dos);
		}else if (op0 instanceof DBRUBISShdStoreComment1){
			dos.writeByte(OP_SHD_RUBIS_STORECOMMENT1);
			((DBRUBISShdStoreComment1) op0).encode(dos);
		}else if (op0 instanceof DBRUBISShdStoreComment2){
			dos.writeByte(OP_SHD_RUBIS_STORECOMMENT2);
			((DBRUBISShdStoreComment2) op0).encode(dos);
		}else if (op0 instanceof DBRUBISShdStoreBuyNow1){
			dos.writeByte(OP_SHD_RUBIS_STOREBUYNOW1);
			((DBRUBISShdStoreBuyNow1) op0).encode(dos);
		}else if (op0 instanceof DBRUBISShdStoreBuyNow2){
			dos.writeByte(OP_SHD_RUBIS_STOREBUYNOW2);
			((DBRUBISShdStoreBuyNow2) op0).encode(dos);
		}else if (op0 instanceof DBRUBISShdRegisterItem){
			dos.writeByte(OP_SHD_RUBIS_REGISTERITEM);
			((DBRUBISShdRegisterItem) op0).encode(dos);
		}else if (op0 instanceof DBRUBISShdStoreBid1){
			dos.writeByte(OP_SHD_RUBIS_STOREBID1);
			((DBRUBISShdStoreBid1) op0).encode(dos);
		}else if (op0 instanceof DBRUBISShdStoreBid2){
			dos.writeByte(OP_SHD_RUBIS_STOREBID2);
			((DBRUBISShdStoreBid2) op0).encode(dos);
		}else if (op0 instanceof DBRUBISShdStoreBid3){
			dos.writeByte(OP_SHD_RUBIS_STOREBID3);
			((DBRUBISShdStoreBid3) op0).encode(dos);
		}else if (op0 instanceof DBQUODDYShdEmpty){
			dos.writeByte(OP_SHD_QUODDY_NONE);
			((DBRUBISShdStoreBid3) op0).encode(dos);
		}else if (op0 instanceof DBQUODDYShdAddToFriend){
			dos.writeByte(OP_SHD_QUODDY_ADDTOFRIENDS);
			((DBQUODDYShdAddToFriend) op0).encode(dos);
		}else if (op0 instanceof DBQUODDYShdAddToFollow){
			dos.writeByte(OP_SHD_QUPDDY_FOLLOWSOMEBODY);
			((DBQUODDYShdAddToFollow) op0).encode(dos);
		}else if (op0 instanceof DBQUODDYShdConfirmFriend){
			dos.writeByte(OP_SHD_QUODDY_COMFIRMFRIEND);
			((DBQUODDYShdConfirmFriend) op0).encode(dos);
		}else if (op0 instanceof DBQUODDYShdUpdateStatus){
			dos.writeByte(OP_SHD_QUODDY_UPDATESTATUS);
			((DBQUODDYShdUpdateStatus) op0).encode(dos);
		}else if (op0 instanceof DBMICROTX5){
			dos.writeByte(OP_SHD_TEST);
			((DBMICROTX5) op0).encode(dos);
		}else if (op0 instanceof DBMICROTXUNIQUETEST){
			dos.writeByte(OP_SHD_UNIQUE_TEST);
			((DBMICROTXUNIQUETEST) op0).encode(dos);
		}else if(op0 instanceof DBSifterShd){
			dos.write(OP_SHD_SIFTER);
			((DBSifterShd) op0).encode(dos);
		}else
			throw new RuntimeException("Cannot encode type " + op0);
	}

}
