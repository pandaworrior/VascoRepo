package org.mpi.vasco.coordination.protocols.util.asym;

public class AsymNonBarrierCounter extends AsymCounter{
	
	private long localCount;
	
	private long globalCount;

	public AsymNonBarrierCounter() {
		this.setLocalCount(0);
		this.setGlobalCount(0);
	}
	
	public AsymNonBarrierCounter(long lc, long gc) {
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

	@Override
	public String toString() {
		StringBuilder strBuild = new StringBuilder("(non-barrier counter: ");
		strBuild.append("(local: ");
		strBuild.append(this.localCount);
		strBuild.append(", global: ");
		strBuild.append(this.globalCount);
		strBuild.append("))");
		return strBuild.toString();
	}
}
