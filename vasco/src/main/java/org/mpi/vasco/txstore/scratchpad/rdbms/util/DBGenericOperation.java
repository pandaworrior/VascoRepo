package org.mpi.vasco.txstore.scratchpad.rdbms.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.mpi.vasco.txstore.scratchpad.rdbms.*;
import org.mpi.vasco.txstore.scratchpad.rdbms.tests.DBTestCountOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWAddItem;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWAddOrderLine;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWAdminUpdate;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWAdminUpdateRelated;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWClearCart;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWCreateEmptyCart;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWEnterAddress;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWEnterCCXact;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWEnterOrder;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWRefreshCart;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWResetCartTime;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.tpcw.DBTPCWSetStock;
import org.mpi.vasco.txstore.util.*;

public abstract class DBGenericOperation extends DBOperation {
	public static final byte OP_GEN_TESTCOUNT = 40;
	public static final byte OP_GEN_TPCWCREATECART = 50;
	public static final byte OP_GEN_TPCWADDITEM = 51;
	public static final byte OP_GEN_TPCWREFRESHCART = 52;
	public static final byte OP_GEN_TPCWRESETCARTTIME = 53;
	public static final byte OP_GEN_TPCWADDORDERLINE = 54;
	public static final byte OP_GEN_TPCWADMINUPDATE = 55;
	public static final byte OP_GEN_TPCWADMINUPDATERELATED = 56; 
	public static final byte OP_GEN_TPCWCLEARCART = 57;
	public static final byte OP_GEN_TPCWENTERADDRESS = 58;
	public static final byte OP_GEN_TPCWENTERCCXACT = 59;
	public static final byte OP_GEN_TPCWENTERORDER = 60;
	public static final byte OP_GEN_TPCWSETSTOCK = 61;

	public DBGenericOperation(byte[] arr) {
		super(arr);
	}

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
	public abstract void executeShadow(IDefDatabase iDefDatabase,
			IPrimaryExec exec);

	public abstract void encode(DataOutputStream dos) throws IOException;

	public static DBOperation decodeGeneric(DataInputStream dis)
			throws IOException {
		byte b = dis.readByte();
		if (b == OP_GEN_TESTCOUNT) {
			return DBTestCountOperation.createOperation(dis);
		} else if (b == OP_GEN_TPCWCREATECART) {
			return DBTPCWCreateEmptyCart.createOperation(dis);
		} else if (b == OP_GEN_TPCWADDITEM) {
			return DBTPCWAddItem.createOperation(dis);
		} else if (b == OP_GEN_TPCWREFRESHCART) {
			return DBTPCWRefreshCart.createOperation(dis);
		} else if (b == OP_GEN_TPCWRESETCARTTIME) {
			return DBTPCWResetCartTime.createOperation(dis);
		}else if (b ==OP_GEN_TPCWADDORDERLINE ){
			return DBTPCWAddOrderLine.createOperation(dis);
		}else if (b ==OP_GEN_TPCWADMINUPDATE ){
			return DBTPCWAdminUpdate.createOperation(dis);
		}
		else if (b ==OP_GEN_TPCWADMINUPDATERELATED ){
			return DBTPCWAdminUpdateRelated.createOperation(dis);
		}
		else if (b ==OP_GEN_TPCWCLEARCART ){
			return DBTPCWClearCart.createOperation(dis);
		}
		else if (b ==OP_GEN_TPCWENTERADDRESS){
			return DBTPCWEnterAddress.createOperation(dis);
		}
		else if (b ==OP_GEN_TPCWENTERCCXACT ){
			return DBTPCWEnterCCXact.createOperation(dis);
		}
		else if (b ==OP_GEN_TPCWENTERORDER ){
			return DBTPCWEnterOrder.createOperation(dis);
		}
		else if (b ==OP_GEN_TPCWSETSTOCK) {
			return DBTPCWSetStock.createOperation(dis);
		} else
			throw new RuntimeException("Cannot encode type " + b);
	}

	public static void encodeGeneric(DBOperation op0, DataOutputStream dos)
			throws IOException {
		if (op0 instanceof DBTestCountOperation) {
			dos.writeByte(OP_GEN_TESTCOUNT);
			((DBTestCountOperation) op0).encode(dos);
		} else if (op0 instanceof DBTPCWCreateEmptyCart) {
			dos.writeByte(OP_GEN_TPCWCREATECART);
			((DBTPCWCreateEmptyCart) op0).encode(dos);
		} else if (op0 instanceof DBTPCWAddItem) {
			dos.writeByte(OP_GEN_TPCWADDITEM);
			((DBTPCWAddItem) op0).encode(dos);
		} else if (op0 instanceof DBTPCWRefreshCart) {
			dos.writeByte(OP_GEN_TPCWREFRESHCART);
			((DBTPCWRefreshCart) op0).encode(dos);
		} else if (op0 instanceof DBTPCWResetCartTime) {
			dos.writeByte(OP_GEN_TPCWRESETCARTTIME);
			((DBTPCWResetCartTime) op0).encode(dos);
		}else if (op0 instanceof DBTPCWAddOrderLine){
			dos.writeByte(OP_GEN_TPCWADDORDERLINE);
			((DBTPCWAddOrderLine) op0).encode(dos);
			
		}else if (op0 instanceof DBTPCWAdminUpdate){
			dos.writeByte(OP_GEN_TPCWADMINUPDATE);
			((DBTPCWAdminUpdate) op0).encode(dos);
			
		}else if (op0 instanceof DBTPCWAdminUpdateRelated){
			dos.writeByte(OP_GEN_TPCWADMINUPDATERELATED);
			((DBTPCWAdminUpdateRelated) op0).encode(dos);
			
		}else if (op0 instanceof DBTPCWClearCart){
			dos.writeByte(OP_GEN_TPCWCLEARCART);
			((DBTPCWClearCart) op0).encode(dos);
			
		}else if (op0 instanceof DBTPCWEnterAddress){
			dos.writeByte(OP_GEN_TPCWENTERADDRESS);
			((DBTPCWEnterAddress) op0).encode(dos);
			
		}else if (op0 instanceof DBTPCWEnterCCXact){
			dos.writeByte(OP_GEN_TPCWENTERCCXACT);
			((DBTPCWEnterCCXact) op0).encode(dos);
			
		}else if (op0 instanceof DBTPCWEnterOrder){
			dos.writeByte(OP_GEN_TPCWENTERORDER);
			((DBTPCWEnterOrder) op0).encode(dos);
			
		}else if (op0 instanceof DBTPCWSetStock){
			dos.writeByte(OP_GEN_TPCWSETSTOCK);
			((DBTPCWSetStock) op0).encode(dos);
			
		}
		else
			throw new RuntimeException("Cannot encode type " + op0);
	}

}
