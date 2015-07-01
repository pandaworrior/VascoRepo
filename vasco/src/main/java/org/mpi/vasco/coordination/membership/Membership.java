package org.mpi.vasco.coordination.membership;

import org.mpi.vasco.network.IMembership;
import org.mpi.vasco.network.Principal;

public class Membership implements IMembership{
	
	String membershipFile;
	Role myRole;
	int myId;
	
	LockService lockService;

	public Membership(String mFile, Role role, int mId) {
		this.setMembershipFile(mFile);
		this.setMyRole(role);
		this.setMyId(mId);
		this.setLockService(new LockService(null, null));
	}

	@Override
	public void readXml(String file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Principal getMe() {
		return getPrincipal(myRole, myId);
	}
	
	public Principal getPrincipal(Role role, int roleId){
		return this.getLockService().getPrincipal(role, roleId);
	}

	public String getMembershipFile() {
		return membershipFile;
	}

	public void setMembershipFile(String membershipFile) {
		this.membershipFile = membershipFile;
	}

	public Role getMyRole() {
		return myRole;
	}

	public void setMyRole(Role myRole) {
		this.myRole = myRole;
	}

	public int getMyId() {
		return myId;
	}

	public void setMyId(int myId) {
		this.myId = myId;
	}

	public LockService getLockService() {
		return lockService;
	}

	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

}
