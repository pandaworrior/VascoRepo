package org.mpi.vasco.txstore.appextend;

import java.util.ArrayList;
import java.util.Vector;

import org.mpi.vasco.txstore.scratchpad.ScratchpadConfig;
import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.ScratchpadInterface;
import org.mpi.vasco.txstore.scratchpad.rdbms.DBScratchpad;
import org.mpi.vasco.txstore.scratchpad.rdbms.resolution.AllOpsLockExecution;
import org.mpi.vasco.txstore.scratchpad.rdbms.resolution.LWWLockExecution;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.util.debug.Debug;

public class ScratchpadFactory implements org.mpi.vasco.txstore.scratchpad.ScratchpadFactory {

	int dcCount;
	int dcId;
	int storageId;
	// unused queue
	Vector<ScratchpadInterface> unusedLocalSPs;
	int userNum;
	ScratchpadConfig config;
	int scratchpadInadvance = 0;
	int scratchpadPoolSize = 0;
	int localSPNum = 0;
	int remoteSPNum = 0;
	

	public ScratchpadFactory(int c, int dc, int ssId, String dbXmlFile, int s) {
		dcCount = c;
		dcId = dc;
		storageId = ssId;
		unusedLocalSPs = new Vector<ScratchpadInterface>();
		scratchpadPoolSize = s;
		 if(dcCount > 1){
             localSPNum = (int) (s*0.6);
             remoteSPNum = (int) (s - s*0.6);
	     }else{
	             localSPNum = s;
	             remoteSPNum = 0;
	     }
		configureScratchpad(dbXmlFile);
		initSPs();
		//define default logical clock
		String defaultLogicalClock="0"; //blue epoch
		for(int i=0; i<dcCount;i++){
			defaultLogicalClock+="-0";
		}
		LogicalClock.DefaultForInTrx=defaultLogicalClock;
		Debug.println("Default logical clock ="+LogicalClock.DefaultForInTrx);
	}

	public void configureScratchpad(String dbXmlFile) {
		Databases dbs;
		String driver = "";
		String url;
		String user;
		String pwd;
		Vector<String> connInfo;
		// get database info
		dbs = new Databases();
		dbs.parseXMLfile(dbXmlFile);
		dbs.printOut();
		Database db = dbs.returnDB(dcId, storageId);
		if (db.url_prefix.contains("mysql"))
			driver = "com.mysql.jdbc.Driver";
		else {
			if (db.url_prefix.contains("mimer"))
				driver = "com.mimer.jdbc.Driver";

		}
		connInfo = db.getConnInfo();
		url = db.url_prefix + connInfo.get(0) + ':' + connInfo.get(1) + '/'
				+ connInfo.get(4);
		user = connInfo.get(2);
		pwd = connInfo.get(3);
		String padClass = "txstore.scratchpad.rdbms.DBScratchpad";
		Debug.println("config DBScratchpad");
		config = new ScratchpadConfig(driver, url, user, pwd, padClass);
		Vector<String> redTableList = db.getRedTableList();
		Vector<String> blueTableList = db.getBlueTableList();
		for (int i = 0; i < redTableList.size(); i++) {
			String tableName = redTableList.get(i);
			System.out.println("config red table " + tableName + "\n");
			config.putPolicy(tableName, new LWWLockExecution(false));
		}

		for (int i = 0; i < blueTableList.size(); i++) {
			String tableName = blueTableList.get(i);
			System.out.println("config blue table " + tableName + "\n");
			config.putPolicy(tableName, new AllOpsLockExecution(true));
		}
		try {
			DBScratchpad.prepareDBScratchpad(config);
		} catch (ScratchpadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("finish to config scratchpad \n");
	}

	public void initSPs() {
		int totalsp=0;
		for(int i = 0; i < localSPNum ; i++){
			unusedLocalSPs.add(createNewSp());
			System.err.println("Creating local scratchpad:"+i);
			totalsp++;
		}
		System.err.println("Total scratchpads "+totalsp);
	}

	public ScratchpadInterface createScratchPad(ProxyTxnId txnId) {
		// TODO Auto-generated method stub
		ScratchpadInterface sp = getScratchpad(txnId);
		return sp;

	}

	public void releaseScratchpad(ScratchpadInterface sp) {
		synchronized(unusedLocalSPs){
			unusedLocalSPs.add(sp);
			unusedLocalSPs.notify();
		}
	}
	
	public void releaseScratchpad(ScratchpadInterface sp, ProxyTxnId txnId){
		synchronized(unusedLocalSPs){
			unusedLocalSPs.add(sp);
			unusedLocalSPs.notify();
		}
	}

	public ScratchpadInterface getScratchpad(ProxyTxnId txnId) {
		ScratchpadInterface sp = null;
		Debug.println("try to get a scratchpad");
			synchronized(unusedLocalSPs){
				while(unusedLocalSPs.isEmpty()){
					try {
						unusedLocalSPs.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				sp = unusedLocalSPs.remove(0);
			}
		return sp;
	}

	public ScratchpadInterface createNewSp() {
		// This part needs to get from xml
		Debug.println("create a new scratchpad from database");
		DBScratchpad dbSp = null;
		String url = config.getURL();
		try {
			if (url.contains("mysql"))
				dbSp = new DBScratchpad(config);
			else {
//				if (url.contains("mimer"))
//					dbSp = new OCCDBScratchpad(config);

			}
		} catch (ScratchpadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dbSp;
	}
	
	public int getAvailablePoolSize() {
		// TODO Auto-generated method stub
		return 0;
	}
}

