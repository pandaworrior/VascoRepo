package org.mpi.vasco.txstore.coordinator;

import org.mpi.vasco.util.debug.Debug;

import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.TimeStamp;

import java.util.Hashtable;
import java.util.Vector;

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
			if(v.lc.precedes(uE.lc) || v.ts.precedes(uE.ts))
				table.put(objectId, uE);
		}
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
	updateEntry(LogicalClock l, TimeStamp t, boolean isD){
		lc = l;
		ts = t;
		isdeleted = isD;
	}
	
	public boolean precedes(updateEntry uE){
		if (lc.precedes(uE.lc))
			return true;
		if(ts.precedes(uE.ts))
			return true;
		return false;
	}
	
	public boolean isDeleted(){
		return isdeleted;
	}
}