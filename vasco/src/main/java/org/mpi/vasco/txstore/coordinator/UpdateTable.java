package org.mpi.vasco.txstore.coordinator;

import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.TimeStamp;
import org.mpi.vasco.util.debug.Debug;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Hashtable;

public class UpdateTable {

	Hashtable<String, updateEntry> table;

	public UpdateTable() {
		table = new Hashtable<String, updateEntry>();
	}
	
	public UpdateTable(int initialCapacity){
		table = new Hashtable<String, updateEntry>(initialCapacity);
	}

	public void addUpdateTime(String objectId, updateEntry uE) {
		updateEntry  v = table.get(objectId);
		if (v == null){
			table.put(objectId, uE);
		}
		else{
			if((v.lc == null && v.ts == null) || v.lc.precedes(uE.lc) || v.ts.precedes(uE.ts)){
				//table.put(objectId, uE);
				v.lc = uE.lc;
				v.ts = uE.ts;
			}
		}
	}
	
	/*
	 * need to clean up the lock list
	 */
	public updateEntry addUpdateTime(String objectId, updateEntry uE, ProxyTxnId txnId) {
		updateEntry  v = table.get(objectId);
		if (v == null){
			v = uE;
			table.put(objectId, v);
		}
		else{
			if((v.lc == null && v.ts == null) || v.lc.precedes(uE.lc) || v.ts.precedes(uE.ts)){
				//table.put(objectId, uE);
				v.lc = uE.lc;
				v.ts = uE.ts;
				if(uE.isDeleted()){
					Debug.println("this element is already deleted");
				}
				v.isdeleted = uE.isdeleted;
			}
			v.lockTable.remove(txnId);
		}
		return v;
	}

	public void addUpdateTime(String objectId, updateEntry uE, LogicalClock start) {
		addUpdateTime(objectId, uE);
		purge(objectId, start);
	}
	
	public void removeElement(String objectId){
		table.remove(objectId);
	}

	public updateEntry getUpdates(String objectId) {
		updateEntry v = table.get(objectId);
		return v;
	}
	
	public void purge(String objectId, LogicalClock start) {
		updateEntry v = table.get(objectId);
		if (v == null) {
			return;
		}
		if (v.lc.precedes(start)){
			table.remove(objectId);
		}
	}

	public int size() {
		return table.size();
	}

	public static void main(String args[]) {

		/*UpdateTable u = new UpdateTable();
		String key1 = "t3-71705.";
		LogicalClock lc1 = new LogicalClock("0-124-116");
		u.addUpdateTime(key1, lc1);
		String key2 = "t2-15723.";
		LogicalClock lc2 = new LogicalClock("0-124-117");
		u.addUpdateTime(key2, lc2);
		System.out.println();
		System.out.println("objectId: " + key1 + " updates " + u.getUpdatesVector(key1));
		System.out.println("objectId: " + key2 + " updates " + u.getUpdatesVector(key2));*/
		
	}
}

class updateEntry {
	LogicalClock lc;
	TimeStamp ts;
	boolean isdeleted = false;
	
	//need to have a lock lists, each entry represents a transaction
	Object2ObjectOpenHashMap<ProxyTxnId, Boolean> lockTable;
	
	updateEntry(LogicalClock l, TimeStamp t, boolean isD){
		lc = l;
		ts = t;
		isdeleted = isD;
		lockTable = new Object2ObjectOpenHashMap<ProxyTxnId, Boolean>();
	}
	
	public boolean precedes(updateEntry uE){
		if((this.lc == null && this.ts == null) ||
				lc.precedes(uE.lc) ||
				ts.precedes(uE.ts)){
			return true;
		}
		return false;
	}
	
	public boolean isDeleted(){
		return isdeleted;
	}
	
	public void lock(ProxyTxnId txnId){
		synchronized(this.lockTable){
			this.lockTable.put(txnId, true);
		}
	}
	
	public void unlock(ProxyTxnId txnId){
		synchronized(this.lockTable){
			this.lockTable.remove(txnId);
		}
	}
	
	public boolean isLockedByOtherTransaction(ProxyTxnId txnId){
		synchronized(this.lockTable){
			//if there are more than 1 transactions that lock the item, then true
			//if there is only one transaction, but it is not the current one, then true
			if((this.lockTable.size() > 1) || 
					(this.lockTable.size() == 1 && !this.lockTable.containsKey(txnId))){
				return true;
			}
		}
		return false;
	}
	
	public String toString(){
		String _str = "<" + this.lc + "," + this.ts + "," + this.isdeleted + ", {";
		
		for(ProxyTxnId txnId : this.lockTable.keySet()){
			_str += txnId.toString() + ",";
		}
		_str += "}>";
		return _str;
	}
}