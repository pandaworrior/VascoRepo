package org.mpi.vasco.txstore.scratchpad.rdbms.tests;
import org.mpi.vasco.util.debug.Debug;

import java.sql.*;
import java.util.*;
import org.mpi.vasco.txstore.scratchpad.*;
import org.mpi.vasco.txstore.scratchpad.rdbms.DBScratchpad;
import org.mpi.vasco.txstore.scratchpad.rdbms.resolution.AllOpsLockExecution;
import org.mpi.vasco.txstore.scratchpad.rdbms.resolution.LWWLockExecution;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSelectResult;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSingleOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBUpdateResult;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.ReadWriteSet;
import org.mpi.vasco.txstore.util.Result;
import org.mpi.vasco.txstore.util.TimeStamp;

public class Test2
{

	public static void main( String[] args) {
		try {
			Debug.debug = true;

			ScratchpadConfig config = new ScratchpadConfig( "com.mysql.jdbc.Driver", "jdbc:mysql://localhost:3306/test", "sa", "", "txstore.scratchpad.rdbms.DBScratchpad");
//			ScratchpadConfig config = new ScratchpadConfig( "org.h2.Driver", "jdbc:h2:test", "sa", "", "txstore.scratchpad.rdbms.DBScratchpad");
			config.putPolicy("T1", new LWWLockExecution(false));
			config.putPolicy("T2", new AllOpsLockExecution(false));
			config.putPolicy("T3", new AllOpsLockExecution(true));
			config.putPolicy("T4", new LWWLockExecution(false));
		
			long n = (new java.util.Date().getTime() / 1000) % 100000;
			
			DBScratchpad db = new DBScratchpad( config);
			db.beginTransaction( new ProxyTxnId(0,0,0));

			DBUpdateResult ru;
			DBSelectResult rq;
			
			ru = (DBUpdateResult)db.execute( new DBSingleOperation( "insert into t1 (a,b,c,d,e) values (" + n + ", " + (n%10) + ",2,3,\'S" + (n%100) + "\');"));
			System.out.println( "result = " + ru);

			DBScratchpad db2 = new DBScratchpad( config);
			db2.beginTransaction( new ProxyTxnId(0,1,0));			

			ru = (DBUpdateResult)db2.execute( new DBSingleOperation( "insert into t1 (a,b,c,d,e) values (" + n + ", " + (n%20) + ",3,4,\'P" + (n%200) + "\');"));
			System.out.println( "result = " + ru);

			ReadWriteSet rwset = db.complete();
			System.out.println( "complete = " + rwset);
			
			ReadWriteSet rwset2 = db2.complete();
			System.out.println( "complete = " + rwset);
			
			long []dcs = { 1, 2};
			LogicalClock lc = new LogicalClock( dcs);
			TimeStamp ts = new TimeStamp( 1, n);
			
			db.commit( lc, ts);

			LogicalClock lc2 = new LogicalClock( dcs);
			TimeStamp ts2 = new TimeStamp( 1, n+1);

			db2.commit( lc2, ts2);

			db.beginTransaction( new ProxyTxnId(0,1,0));
			rq = (DBSelectResult)db.execute( new DBSingleOperation( "select * from t1;"));
			System.out.println( "query result = \n" + rq);
			rq = (DBSelectResult)db.execute( new DBSingleOperation( "select * from t2;"));
			System.out.println( "query result = \n" + rq);
			db.abort();


			DBScratchpad db3 = new DBScratchpad( config);
			db3.beginTransaction( new ProxyTxnId(0,0,0));
			ru = (DBUpdateResult)db3.execute( new DBSingleOperation( "insert into t1 (a,b,c,d,e) values (" + (n+2) + ", " + (n%20) + ",3,4,\'P" + (n%200) + "\');"));
			System.out.println( "result = " + ru);
			db3.abort();


			System.out.println( "Test 2 completed with success");
			
		} catch( Exception e) {
			e.printStackTrace();
		}
	}
}
