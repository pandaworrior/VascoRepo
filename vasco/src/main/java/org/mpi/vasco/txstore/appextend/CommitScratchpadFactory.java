package org.mpi.vasco.txstore.appextend;

import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.mpi.vasco.txstore.scratchpad.ScratchpadConfig;
import org.mpi.vasco.txstore.scratchpad.ScratchpadException;
import org.mpi.vasco.txstore.scratchpad.ScratchpadInterface;
import org.mpi.vasco.txstore.scratchpad.ScratchpadFactory;
import org.mpi.vasco.txstore.scratchpad.rdbms.DBCommitScratchpad;
import org.mpi.vasco.txstore.scratchpad.rdbms.resolution.LWWLockExecution;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.util.debug.Debug;

public class CommitScratchpadFactory implements ScratchpadFactory {

	int dcCount;
	int dcId;
	int storageId;
	// unused queue
	Vector<ScratchpadInterface> unusedLocalSPs;
	final ReentrantLock spQueueLock = new ReentrantLock();
	final Condition spQueueCond = spQueueLock.newCondition();
	Vector<ScratchpadInterface> unusedRemoteSPs;
	int userNum;
	ScratchpadConfig config;
	int scratchpadInadvance = 0;
	int scratchpadPoolSize = 0;
	int localSPNum = 0;
	

	public CommitScratchpadFactory(int c, int dc, int ssId, String dbXmlFile, int s) {
		dcCount = c;
		dcId = dc;
		storageId = ssId;
		unusedLocalSPs = new Vector<ScratchpadInterface>();
		scratchpadPoolSize = s;
		localSPNum = s;
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
		String padClass = "txstore.scratchpad.rdbms.DBCommitScratchpad";
		Debug.println("config CommitScratchpad");
		config = new ScratchpadConfig(driver, url, user, pwd, padClass);
		Vector<String> redTableList = db.getRedTableList();
		Vector<String> blueTableList = db.getBlueTableList();
		for (int i = 0; (i < redTableList.size()) && (redTableList.size() >0) ; i++) {
			String tableName = redTableList.get(i);
			System.out.println("config red table " + tableName + "\n");
			config.putPolicy(tableName, new LWWLockExecution(false));
		}
		for (int i = 0; (i < blueTableList.size()) && (blueTableList.size() > 0); i++) {
			String tableName = blueTableList.get(i);
			System.out.println("config blue table " + tableName + "\n");
			config.putPolicy(tableName, new LWWLockExecution(true));
		}
		try {
			DBCommitScratchpad.prepareDBScratchpad(config);
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
		spQueueLock.lock();
		try{
			unusedLocalSPs.add(sp);
			spQueueCond.signal();
		}finally{
			spQueueLock.unlock();
		}
	}
	
	public void releaseScratchpad(ScratchpadInterface sp, ProxyTxnId txnId){
		Debug.println("release scratchpad " + txnId);
		spQueueLock.lock();
		try{
			unusedLocalSPs.add(sp);
			spQueueCond.signal();
		}finally{
			spQueueLock.unlock();
		}
	}

	public ScratchpadInterface getScratchpad(ProxyTxnId txnId) {
		ScratchpadInterface sp = null;
		Debug.println("try to get a scratchpad");
		spQueueLock.lock();
		try{
			while(unusedLocalSPs.isEmpty()){
				try {
					spQueueCond.await();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Debug.println("get a scratchpad " + txnId);
			sp = unusedLocalSPs.remove(0);
		}finally{
			spQueueLock.unlock();
		}
		return sp;
	}

	public ScratchpadInterface createNewSp() {
		// This part needs to get from xml
		Debug.println("create a new scratchpad from database");
		DBCommitScratchpad dbSp = null;
		String url = config.getURL();
		try {
			if (url.contains("mysql"))
				dbSp = new DBCommitScratchpad(config);
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

