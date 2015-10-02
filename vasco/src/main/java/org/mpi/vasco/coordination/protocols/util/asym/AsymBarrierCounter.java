package org.mpi.vasco.coordination.protocols.util.asym;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;

import org.mpi.vasco.txstore.util.ProxyTxnId;

public class AsymBarrierCounter extends AsymCounter{
	
	Set<ProxyTxnId> activeBarrierTxnIdSet;//support search by key and remove by key

	public AsymBarrierCounter() {
		Set<ProxyTxnId> tempList = new ObjectOpenHashSet<ProxyTxnId>();
		this.setActiveBarrierTxnIdList(tempList);
	}

	public Set<ProxyTxnId> getActiveBarrierTxnIdSet() {
		return activeBarrierTxnIdSet;
	}

	public void setActiveBarrierTxnIdList(Set<ProxyTxnId> activeBarrierTxnIdSet) {
		this.activeBarrierTxnIdSet = activeBarrierTxnIdSet;
	}
	
	public void addBarrierInstance(ProxyTxnId txnId){
		this.activeBarrierTxnIdSet.add(txnId);
	}
	
	public void removeBarrierInstance(ProxyTxnId txnId){
		this.activeBarrierTxnIdSet.remove(txnId);
	}

	@Override
	public boolean isBarrier() {
		return true;
	}
	
	public String toString(){
		StringBuilder strBuild = new StringBuilder("(barrier counter: ");
		strBuild.append("{");
		for(ProxyTxnId txnId : this.getActiveBarrierTxnIdSet()){
			strBuild.append(txnId.toString());
			strBuild.append(',');
		}
		if(strBuild.charAt(strBuild.length() - 1) == ','){
			strBuild.deleteCharAt(strBuild.length() - 1);
		}
		strBuild.append("})");
		return strBuild.toString();
	}

}
