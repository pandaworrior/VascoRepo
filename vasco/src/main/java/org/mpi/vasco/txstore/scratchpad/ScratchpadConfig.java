package org.mpi.vasco.txstore.scratchpad;

import java.util.*;
import java.util.Map.Entry;

import org.mpi.vasco.txstore.scratchpad.resolution.ExecutionPolicy;

public class ScratchpadConfig
{
	protected String driver, url, user, pwd, padClass;
	protected Map<String,ExecutionPolicy> policies;
	
	public ScratchpadConfig( String driver, String url, String user, String pwd, String padClass) {
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.pwd = pwd;
		this.padClass = padClass;
		this.policies = new HashMap<String,ExecutionPolicy>();
	}
	protected ScratchpadConfig( String driver, String url, String user, String pwd, String padClass, Map<String,ExecutionPolicy> p) {
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.pwd = pwd;
		this.padClass = padClass;
		this.policies = new HashMap<String,ExecutionPolicy>();
		Iterator<Entry<String,ExecutionPolicy>> it = p.entrySet().iterator();
		while( it.hasNext()) {
			Entry<String,ExecutionPolicy> e = it.next();
			policies.put( e.getKey(), e.getValue().duplicate());
		}
	}
	public ScratchpadConfig duplicate() {
		return new ScratchpadConfig( driver, url, user, pwd, padClass, policies);
	}
	public String getPadClass()  {
		return padClass;
	}
	public String getDriver()  {
		return driver;
	}
	public String getURL()  {
		return url;
	}
	public String getUser()  {
		return user;
	}
	public String getPassword()  {
		return pwd;
	}
	public Collection<ExecutionPolicy> getPolicies() {
		return policies.values();
		
	}
	public ExecutionPolicy getPolicy( String tableName) {
		return policies.get(tableName.toUpperCase());
		
	}
	public void putPolicy( String tableName, ExecutionPolicy policy) {
		policies.put(tableName.toUpperCase(), policy);
	}

}
