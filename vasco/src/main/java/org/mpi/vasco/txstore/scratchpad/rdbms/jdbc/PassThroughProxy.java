package org.mpi.vasco.txstore.scratchpad.rdbms.jdbc;
import org.mpi.vasco.util.debug.Debug;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.mpi.vasco.txstore.proxy.ClosedLoopProxyInterface;
import org.mpi.vasco.txstore.scratchpad.ScratchpadConfig;
import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.ScratchpadInterface;
import org.mpi.vasco.txstore.scratchpad.rdbms.IDBScratchpad;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBShadowOperation;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.Operation;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.Result;
import org.mpi.vasco.txstore.util.TimeStamp;

public class PassThroughProxy implements ClosedLoopProxyInterface {
	public static ScratchpadConfig config;

	public static PassThroughProxy getInstance() {
		return new PassThroughProxy();
	}

	Map<ProxyTxnId, ScratchpadInterface> txs;
	Queue<ScratchpadInterface> queue;
	int counter;

	PassThroughProxy() {
		init();
	}

	protected void init() {
		queue = new LinkedList<ScratchpadInterface>();
		txs = new HashMap<ProxyTxnId, ScratchpadInterface>();
		counter = (int) (new Date().getTime() % 1000000);
	}

	protected ScratchpadInterface getPad() {
		synchronized(queue){
			try {
				if (queue.isEmpty()) {
					Class c = Class.forName(config.getPadClass());
					Constructor cc = c.getConstructor(ScratchpadConfig.class);
					ScratchpadInterface pad = (ScratchpadInterface) cc
							.newInstance(config);
					queue.add(pad);
				}
				return queue.poll();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
				return null;
			}
		}
	}

	@Override
	public byte[] execute(byte[] op, ProxyTxnId txn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] execute(byte[] op, ProxyTxnId txn, int storageId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result execute(Operation op, ProxyTxnId txn) {
		ScratchpadInterface pad = getPad(txn);
		if (pad == null)
			throw new RuntimeException(
					"No active scratchpad for current transaction");
		try {
			return pad.execute(op);
		} catch (ScratchpadException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Result execute(Operation op, ProxyTxnId txn, int storageId) {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized int incrementCounter(){
		return counter++;
	}
	@Override
	public ProxyTxnId beginTxn(){
		int currentCounter = incrementCounter();
		ProxyTxnId id = new ProxyTxnId(0, 0, currentCounter);
		ScratchpadInterface pad = getPad();
		addPad(id, pad);
		pad.beginTransaction(id);
		return id;
	}

	private void addPad(ProxyTxnId id, ScratchpadInterface pad){
		synchronized(txs){
			txs.put(id,pad);
		}
	}
	
	private ScratchpadInterface getPad(ProxyTxnId id){
		synchronized(txs){
			return txs.get(id);
		}
	}
	
	private void removePad(ProxyTxnId id){
		synchronized(txs){
			txs.remove(id);
		}
	}
	
	private void releasePad(ScratchpadInterface pad){
		synchronized(queue){
			queue.add(pad);
		}
	}
	
	
	@Override
	public boolean commit(ProxyTxnId txn) {
		ScratchpadInterface pad = getPad(txn);
		if (pad == null)
			throw new RuntimeException(
					"No active scratchpad for current transaction");
		try {
			long[] dcs = { 1, 2 };
			LogicalClock lc = new LogicalClock(dcs, 1);
			int currentCounter = incrementCounter();
			TimeStamp ts = new TimeStamp(1, currentCounter);
			pad.commit(lc, ts);
			return true;
		} catch (ScratchpadException e) {
			e.printStackTrace();
			return false;
		} finally {
			removePad(txn);
			releasePad(pad);
		}
	}

	@Override
	public void abort(ProxyTxnId txn) {
		ScratchpadInterface pad = getPad(txn);
		if (pad == null)
			throw new RuntimeException(
					"No active scratchpad for current transaction");
		try {
			pad.abort();
		} catch (ScratchpadException e) {
			e.printStackTrace();
		} finally {
			removePad(txn);
			releasePad(pad);
		}
	}

	public boolean commit(ProxyTxnId txId, DBShadowOperation op, int color) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ResultSet executeOrig(Operation op, ProxyTxnId txnid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSet executeOrig(Operation op, ProxyTxnId pr, int sid) {
		// TODO Auto-generated method stub
		return null;
	}

}
