package org.mpi.vasco.txstore.scratchpad.rdbms.tests;
import org.mpi.vasco.util.debug.Debug;

import java.sql.*;
import java.util.*;
import org.mpi.vasco.txstore.scratchpad.*;
import org.mpi.vasco.txstore.scratchpad.rdbms.DBScratchpad;
import org.mpi.vasco.txstore.scratchpad.rdbms.jdbc.PassThroughProxy;
import org.mpi.vasco.txstore.scratchpad.rdbms.resolution.AllOpsLockExecution;
import org.mpi.vasco.txstore.scratchpad.rdbms.resolution.LWWLockExecution;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBOperation;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBSelectResult;
import org.mpi.vasco.txstore.scratchpad.rdbms.util.DBUpdateResult;
import org.mpi.vasco.txstore.util.LogicalClock;
import org.mpi.vasco.txstore.util.ProxyTxnId;
import org.mpi.vasco.txstore.util.ReadWriteSet;
import org.mpi.vasco.txstore.util.Result;
import org.mpi.vasco.txstore.util.TimeStamp;

public class TestJDBCP1
{

	public static void main( String[] args) {
		try {
			// setup proxy
			ScratchpadConfig config = new ScratchpadConfig( "org.h2.Driver", "jdbc:h2:test", "sa", "", "txstore.scratchpad.rdbms.DBScratchpad");
			config.putPolicy("T1", new LWWLockExecution(false));
			config.putPolicy("T2", new AllOpsLockExecution(false));
			config.putPolicy("T3", new AllOpsLockExecution(true));
			config.putPolicy("T4", new LWWLockExecution(false));
			PassThroughProxy.config = config;
			
			long n = (new java.util.Date().getTime() / 1000) % 100000;


			Class.forName("txstore.scratchpad.rdbms.jdbc.TxMudDriver");
			Connection con = DriverManager.getConnection( "jdbc:txmud:test");
			
			Statement stat = con.createStatement();
			
			PreparedStatement prep = con.prepareStatement("insert into t1 (a,b,c,d,e) values (?, ?,2,3,? );");
			
			Debug.println("N=====" + n);
			prep.setLong(1, n);
			prep.setInt(2, (int)( n % 10));
			prep.setInt(3, (int)( n % 100));
			
			int ru = prep.executeUpdate();
			Debug.println( "result = " + ru);

			prep.setLong(1, n+1);
			prep.setInt(2, (int)( (n+1) % 10));
			prep.setInt(3, (int)( (n+1) % 100));
			ru = prep.executeUpdate();
			Debug.println( "result = " + ru);

			ru = stat.executeUpdate( "insert into t2 values (" + n + ", " + (n%5) + ",2,3,\'T" + (n%100) + "\');");
			Debug.println( "result = " + ru);

			ResultSet rq = stat.executeQuery( "select * from t1 where a > 10000 limit 2;");
			Debug.println( "query result = \n" + rq);
			
			if( rq.next()) {
				ru =  stat.executeUpdate( "delete from t1 where a=" + rq.getInt(1) + ";");
				Debug.println( "result = " + ru);
				if( rq.next()) {
					ru =  stat.executeUpdate(  "update t1 set c = c + 1 where a =" + rq.getInt(1) + ";");
					Debug.println( "result = " + ru);
				}
			}
			
			con.commit();


			rq = stat.executeQuery( "select * from t1;");
			Debug.println( "query result = \n" + rq);
			rq = stat.executeQuery( "select * from t2;");
			Debug.println( "query result = \n" + rq);
			con.rollback();

			Debug.println( "Test 1 completed with success");
			
		} catch( Exception e) {
			e.printStackTrace();
		}
	}
}
