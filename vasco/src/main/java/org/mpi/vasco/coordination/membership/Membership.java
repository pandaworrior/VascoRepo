package org.mpi.vasco.coordination.membership;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.mpi.vasco.network.IMembership;
import org.mpi.vasco.network.Principal;
import org.mpi.vasco.util.debug.Debug;

public class Membership implements IMembership{
	
	String membershipFile;
	Role myRole;
	int myId;
	
	LockService lockService;
	
	private static final String LOCKSERVICE = "lockService";
	private static final String LOCKSERVERCLUSTER = "lockServerCluster";
	private static final String LOCKSERVERNUM = "lsNum";
	private static final String LOCKSERVER = "lockServer";
	private static final String LOCKSERVERID = "lsId";
	private static final String LOCKSERVERIP = "lsIP";
	private static final String LOCKSERVERPORT = "lsPort";
	private static final String LOCKSERVERDBPORT = "dbPort";
	private static final String LOCKCLIENTGROUP = "lockClientGroup";
	private static final String LOCKCLIENTNUM = "lcNum";
	private static final String LOCKCLIENT = "lockClient";
	private static final String LOCKCLIENTID = "lcId";
	private static final String LOCKCLIENTIP = "lcIP";
	private static final String LOCKCLIENTPORT = "lcPort";

	public Membership(String mFile, Role role, int mId) {
		this.setMembershipFile(mFile);
		this.setMyRole(role);
		this.setMyId(mId);
		this.setLockService(null);
		this.readXml(mFile);
	}

	@Override
	public void readXml(String file) {
		if(this.getLockService() != null){
			throw new RuntimeException("You already parsed the membership file");
		}
		
		LockServer[] lServers = null;
		LockClient[] lClients = null;
		
		int indexOfServer = 0;
		int indexOfClient = 0;
		int numOfServer = 0;
		int numOfClient = 0;
		
		try{
		    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		    InputStream in  = new FileInputStream(file);
		    XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
		   
		    
		    while (eventReader.hasNext()){
				XMLEvent event = eventReader.nextEvent();
	
				if (event.isStartElement()){
				    StartElement startElement = event.asStartElement();
				    
				    if (startElement.getName().getLocalPart() == LOCKSERVERCLUSTER){
					    Debug.println(startElement);
						Iterator<Attribute> attributes = startElement.getAttributes();
						while (attributes.hasNext()) {
						    Attribute attribute = attributes.next();
						    if (attribute.getName().toString().equals(LOCKSERVERNUM)){
								numOfServer = Integer.parseInt(attribute.getValue());
								lServers = new LockServer[numOfServer];
						    }
						}
				    }
				    
				    if (startElement.getName().getLocalPart() == LOCKSERVER){
				    	//Debug.println("\t"+startElement);
				    	Iterator<Attribute> attributes = startElement.getAttributes();
				    	int lsId = -1;
				    	String addr = "";
				    	int lsPort = -1;
				    	int dbPort = -1;
				    	while (attributes.hasNext()) {
				    		Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(LOCKSERVERID)){
				    			lsId = Integer.parseInt(attribute.getValue());
							}else if (attribute.getName().toString().equals(LOCKSERVERIP)){
								addr = attribute.getValue();
							}else if (attribute.getName().toString().equals(LOCKSERVERPORT)){
								lsPort = Integer.parseInt(attribute.getValue());
							}else if (attribute.getName().toString().equals(LOCKSERVERDBPORT)){
								dbPort = Integer.parseInt(attribute.getValue());
							}else
								throw new RuntimeException("invalid attribute");
				    	}
				    	LockServer ls = new LockServer(lsId, addr, lsPort, dbPort);
				    	lServers[indexOfServer++] = ls;
				    }
				    
				    if (startElement.getName().getLocalPart() == LOCKCLIENTGROUP){
				    	//Debug.println("\t\t"+startElement);
				    	Iterator<Attribute> attributes = startElement.getAttributes();
				    	while(attributes.hasNext()){
				    		Attribute attribute = attributes.next();
				    		if (attribute.getName().toString().equals(LOCKCLIENTNUM)){
				    			numOfClient = Integer.parseInt(attribute.getValue());
				    			lClients = new LockClient[numOfClient];
				    		}
				    	}
				    }
				    
				    if (startElement.getName().getLocalPart() == LOCKCLIENT){
				    	//Debug.println("\t\t"+startElement);
				    	Iterator<Attribute> attributes = startElement.getAttributes();
				    	int cId = -1;
				    	String addr = "";
				    	int cPort = -1;
				    	while(attributes.hasNext()){
				    		Attribute attribute = attributes.next();
				    		if (attribute.getName().toString().equals(LOCKCLIENTID)){
				    			cId = Integer.parseInt(attribute.getValue());
							}else if (attribute.getName().toString().equals(LOCKCLIENTIP)){
								addr = attribute.getValue();
							}else if (attribute.getName().toString().equals(LOCKCLIENTPORT)){
								cPort = Integer.parseInt(attribute.getValue());
							}else
								throw new RuntimeException("invalid attribute");
				    	}
				    	
				    	LockClient lc = new LockClient(cId, addr, cPort);
				    	lClients[indexOfClient++] = lc;
				    }
	
				}
			}
		    in.close();
		} catch(Exception e){
		    throw new RuntimeException(e);
		}
		
		
		
		if(lServers == null || lClients == null ||
				indexOfServer != numOfServer || indexOfClient != numOfClient){
			throw new RuntimeException("The server and client is not right");
		}
		
		this.setLockService(new LockService(lClients, lServers));
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
	
	public Principal[] getAllPrincipalByRole(Role r){
		if(r == Role.LOCKCLIENT){
			return this.lockService.getLockClients();
		}else if(r == Role.LOCKSERVER){
			return this.lockService.getLockServers();
		}else{
			throw new RuntimeException("No such role " + r);
		}
	}
	
	public String getMembershipString(){
		return this.getLockService().toString();
	}
	
	public String getMyString(){
		return RoleFactory.getRoleString(myRole) + " id " + this.getMyId();
	}
	
	public String toString(){
		StringBuilder strB = new StringBuilder();
		strB.append("Membership:\n");
		strB.append(this.getMembershipString() + "\n");
		strB.append("Myself:\n");
		strB.append(this.getMyString());
		return strB.toString();
	}
	
	public static void main(String[] args){
		if(args.length != 1){
			System.err.println("Please input the following parameters: membershipFilePath");
			System.exit(-1);
		}
		String memFile = args[0];
		System.out.println("Membership file is " + memFile);
		
		Membership m1 = new Membership(memFile, Role.LOCKSERVER, 0);
		System.out.println(m1.toString());
		
		Membership m2 = new Membership(memFile, Role.LOCKCLIENT, 0);
		System.out.println(m2.toString());
	}

}
