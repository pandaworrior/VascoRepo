package org.mpi.vasco.coordination.protocols.util.asym;

public class AsymNonBarrierCounter extends AsymCounter{
	
	private long localCount;
	
	private long globalCount;

	public AsymNonBarrierCounter(String _counterName) {
		super(_counterName);
		this.setLocalCount(0);
		this.setGlobalCount(0);
	}
	
	public AsymNonBarrierCounter(String _counterName, long lc, long gc) {
		super(_counterName);
		this.setLocalCount(lc);
		this.setGlobalCount(gc);
	}

	public long getLocalCount() {
		return localCount;
	}

	public void setLocalCount(long localCount) {
		this.localCount = localCount;
	}

	public long getGlobalCount() {
		return globalCount;
	}

	public void setGlobalCount(long globalCount) {
		this.globalCount = globalCount;
	}
	
	public void addLocalInstance(){
		this.localCount++;
	}
	
	public void completeLocalInstance(){
		this.globalCount++;
	}
	
	public void addRemoteInstance(){
		this.globalCount++;
	}

	@Override
	public boolean isBarrier() {
		return false;
	}

}
