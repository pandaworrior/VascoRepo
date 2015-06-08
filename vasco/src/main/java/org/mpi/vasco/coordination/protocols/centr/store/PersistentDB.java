package org.mpi.vasco.coordination.protocols.centr.store;

import java.sql.Connection;
import java.sql.Statement;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class PersistentDB{
	
	Connection con;
	String url;
	String username;
	String passwd;
	
	StringBuilder strBuild;
	Statement stmt;
	
	private static final Logger log = Logger.getLogger( PersistentDB.class.getName() );
	
	PersistentDB(String _url, String _username, String _passwd){
		log.log(Level.CONFIG, "Initializing PersistentDB");
		this.setUrl(_url);
		this.setUsername(_username);
		this.setPasswd(_passwd);
		this.setStrBuild(new StringBuilder());
	}

	public Connection getCon() {
		return con;
	}

	public void setCon(Connection con) {
		this.con = con;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public StringBuilder getStrBuild() {
		return strBuild;
	}

	public void setStrBuild(StringBuilder strBuild) {
		this.strBuild = strBuild;
	}

	public Statement getStmt() {
		return stmt;
	}

	public void setStmt(Statement stmt) {
		this.stmt = stmt;
	}
}
