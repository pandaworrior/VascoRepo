package org.mpi.vasco.txstore.membership;
import org.mpi.vasco.network.IMembership;
import org.mpi.vasco.network.Principal;
import org.mpi.vasco.util.debug.Debug;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


public class Membership implements IMembership{

    Datacenter datacenters[];
    Principal principals[];
    int datacenter;
    Role role;
    int roleid;
    int tokenSize=1;

    public Membership(String filename, int datacenter, Role role, int roleid){
	this.datacenter = datacenter;
	this.role = role;
	this.roleid = roleid;

	readXml(filename);

    }



    private static final String DATACENTERCOUNT = "dataCenters";
    private static final String DATACENTERNUM = "dcNum";
    private static final String DATACENTER = "dataCenter";
    private static final String DATACENTERHOST = "cdIP";
    private static final String DATACENTERPORT = "cdPort";
    private static final String REMOTECOORHOST = "RemotecdIP";
    private static final String REMOTECOORPORT = "RemotecdPort";
    private static final String STORAGECOUNT = "storageShims";
    private static final String STORAGENUM = "ssNum";
    private static final String STORAGE = "storageShim";
    private static final String STORAGEHOST = "ssIP";
    private static final String STORAGEPORT = "ssPort";
    private static final String PROXYCOUNT = "webProxies";
    private static final String PROXYNUM = "wpNum";
    private static final String PROXY = "webproxy";
    private static final String PROXYHOST = "wpIP";
    private static final String PROXYPORT = "wpPort";
    private static final String BLUETOKEN = "blueToken";

    public void readXml(String file){
	if (datacenters != null)
	    throw new RuntimeException("xml file already parsed!");
	Principal.resetUniqueIdentifiers();
	Datacenter dcs[] = null;
	Coordinator c = null;
	RemoteCoordinator rc = null;
	Storage[] s = null;
	Proxy[] p = null;
	int curDC = 0;
	int curStorage = 0;
	int curProxy = 0;
	Vector<Principal> princ = new Vector<Principal>();
	try{
	    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	    InputStream in  = new FileInputStream(file);
	    XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
	   
	    
	    while (eventReader.hasNext()){
		XMLEvent event = eventReader.nextEvent();
	

		if (event.isStartElement()){
		    StartElement startElement = event.asStartElement();
		    
		    //  If this is the DC_COUNT entry, then create the datacenters array
		    if (startElement.getName().getLocalPart() == DATACENTERCOUNT){
			//			Debug.println(startElement);
			Iterator<Attribute> attributes = startElement.getAttributes();
			while (attributes.hasNext()) {
			    Attribute attribute = attributes.next();
			    if (attribute.getName().toString().equals(DATACENTERNUM)){
				int dcCount = Integer.parseInt(attribute.getValue());
				if (dcs != null)
				    throw new RuntimeException("malformed xml file.  Too many <dataCenters dcNum=\"XXX\" lines.");
				dcs = new Datacenter[dcCount];
			    }
			    if (attribute.getName().toString().equals(BLUETOKEN))
				tokenSize = Integer.parseInt(attribute.getValue());
			}
		    }
		    
		    // if its the datacenter entry then extract the coordinator IP
		    if (startElement.getName().getLocalPart() == DATACENTER){
			//Debug.println("\t"+startElement);
			Iterator<Attribute> attributes = startElement.getAttributes();
			String chost = null;
			int cport = -1;
			String rchost = null;
			int rcport = -1;
			while (attributes.hasNext()) {
			    Attribute attribute = attributes.next();
			    if (attribute.getName().toString().equals(DATACENTERHOST))
				chost = attribute.getValue();
			    else if (attribute.getName().toString().equals(DATACENTERPORT))
				cport = Integer.parseInt(attribute.getValue());
			    else if (attribute.getName().toString().equals(REMOTECOORHOST))
			    	rchost = attribute.getValue();
			    else if (attribute.getName().toString().equals(REMOTECOORPORT))
			    	rcport = Integer.parseInt(attribute.getValue());
			    else
				throw new RuntimeException("invalid attribute");
			}
			c = new Coordinator(curDC, chost, cport);
			rc = new RemoteCoordinator(curDC, rchost, rcport);
			princ.add(c);
			princ.add(rc);
		    }
		    
		    // if it is storage shim count, then create the shims array
		    if (startElement.getName().getLocalPart() == STORAGECOUNT){
			//Debug.println("\t\t"+startElement);
			Iterator<Attribute> attributes = startElement.getAttributes();
			while(attributes.hasNext()){
			    Attribute attribute = attributes.next();
			    if (attribute.getName().toString().equals(STORAGENUM)){
				int scount = Integer.parseInt(attribute.getValue());
				if (s != null && scount != s.length)
				    throw new RuntimeException("Bad XML:  different storage cunts");
				else
				    s = new Storage[scount];
			    }
			}
		    }
		    
		     // if its a storage entry then add it to the end of the storage list
		    if (startElement.getName().getLocalPart() == STORAGE){
			//Debug.println("\t\t\t"+startElement);
						
			Iterator<Attribute> attributes = startElement.getAttributes();
			String chost = null;
			int cport=-1;
			while (attributes.hasNext()) {
			    Attribute attribute = attributes.next();
			    if (attribute.getName().toString().equals(STORAGEHOST))
				chost = attribute.getValue();
			    else if (attribute.getName().toString().equals(STORAGEPORT))
				cport = Integer.parseInt(attribute.getValue());
			    else
				throw new RuntimeException("invalid attribute");
			}
			s[curStorage++] = new Storage(curStorage, curDC, chost, cport);
			princ.add(s[curStorage-1]);
		    }
		    
		    // if it is proxy shim count, then create the shims array
		    if (startElement.getName().getLocalPart() == PROXYCOUNT){
			//Debug.println("\t\t"+startElement);
			Iterator<Attribute> attributes = startElement.getAttributes();
			while(attributes.hasNext()){
			    Attribute attribute = attributes.next();
			    if (attribute.getName().toString().equals(PROXYNUM)){
				int scount = Integer.parseInt(attribute.getValue());
				if (p != null && scount != p.length)
				    throw new RuntimeException("Bad XML:  different proxy cunts");
				else
				    p = new Proxy[scount];
				    
				
			    }
			}
		    }
		    
		    // if its a proxy entry then add it to the end of the proxy list
		    if (startElement.getName().getLocalPart() == PROXY){
			//Debug.println("\t\t\t"+startElement);
			Iterator<Attribute> attributes = startElement.getAttributes();
			String chost = null;
			int cport=-1;
			while (attributes.hasNext()) {
			    Attribute attribute = attributes.next();
			    if (attribute.getName().toString().equals(PROXYHOST))
				chost = attribute.getValue();
			    else if (attribute.getName().toString().equals(PROXYPORT))
				cport = Integer.parseInt(attribute.getValue());
			    else
				throw new RuntimeException("invalid attribute");
			}
			p[curProxy++] = new Proxy(curProxy, curDC, chost, cport);
			princ.add(p[curProxy-1]);
		    }

		} else if (event.isEndElement()){
		    EndElement endElement = event.asEndElement();

		    // end of data center then create the DC object and reset the local counters
		    if (endElement.getName().getLocalPart() == (DATACENTER)){
			//Debug.println("\t"+endElement);
		    if (p == null) p = new Proxy[0];
			dcs[curDC++] = new Datacenter(curDC, c, rc, p, s);
			if (p != null && curProxy != p.length)
			    throw new RuntimeException("wrong proxy count: "+curProxy+" "+p.length);
			if (s != null && curStorage != s.length)
			    throw new RuntimeException("wrong storage count");
			if (c == null)
			    throw new RuntimeException("missing a coordiantor");
			c = null;
			rc = null;
			s = new Storage[s.length];
			//p = new Proxy[p.length];
			p = null;
			curProxy = 0;
			curStorage = 0;
		    }

		    if  (endElement.getName().equals(DATACENTERCOUNT)){
			if (curDC != dcs.length)
			    throw new RuntimeException("wrong number of data centers "+
						       "looking for "+dcs.length+" found "+
						       curDC);
		    }

		}else
		    ;//throw new RuntimeException("invalid xml element: "+event);
	    }
	} catch(Exception e){
	    throw new RuntimeException(e);
	}
	datacenters =  dcs;
	principals = new Principal[princ.size()];
	for (int i = 0; i < principals.length; i++){
	    principals[i] = princ.get(i);
	}

    }


    public Datacenter[] getDatacenters(){
	return datacenters;
    }
    
  

    public Datacenter getDatacenter(int i){
	return datacenters[i];
    }
    
    public int getTokenSize(){
	return tokenSize;
    }

    public Principal getMe(){
	return datacenters[datacenter].getPrincipal(role, roleid);
    }

    public Principal getPrincipal(int datacenter, Role role, int roleid){
	return getDatacenter(datacenter).getPrincipal(role, roleid);
    }

    public int getDatacenterCount(){
	return datacenters.length;
    }

    public int getStorageCount(){
	return datacenters[0].getStorageCount();
    }
    
    public int getProxyCount(int dcId){
    	return datacenters[dcId].getProxyCount();
    }
    
    public int getProxyCount(){
    	int dcNum = datacenters.length;
    	int pNum = 0;
    	for(int i = 0; i < dcNum; i++){
    		pNum += getProxyCount(i);
    	}
    	return pNum;
    }

    public int getPrincipalCount(){
	return principals.length;
    }

    public Principal getPrincipal(int index){
	return principals[index];
    }


    public static void main(String args[]){
	Membership m = new Membership(args[0], 0, Role.COORDINATOR, 0);

	Datacenter dcs[] = m.getDatacenters();
	Debug.println("datacenters: "+dcs.length);
	for (int i = 0; i < dcs.length; i++){
	    Debug.println("datacenter " + i+" : "+dcs[i]);
	}

	Debug.println("me as dc 0, coordinator 0 : "+m.getMe());
	m = new Membership(args[0], 1, Role.STORAGE, 0);
	Debug.println("me as  dc 1 storage 0 : "+m.getMe());
	m = new Membership(args[0], 1, Role.PROXY, 1);
	Debug.println("me as  dc 1 proxy 1: "+m.getMe());


	Debug.println("principal 2 : "+m.getPrincipal(2));
	Debug.println("principal 5 : "+m.getPrincipal(5));


    }

}