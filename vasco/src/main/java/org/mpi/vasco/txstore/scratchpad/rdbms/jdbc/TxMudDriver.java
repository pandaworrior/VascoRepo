package org.mpi.vasco.txstore.scratchpad.rdbms.jdbc;
import org.mpi.vasco.util.debug.Debug;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import org.mpi.vasco.txstore.proxy.ClosedLoopProxyInterface;
import org.mpi.vasco.txstore.scratchpad.ScratchpadException;

/*
 * Driver urls: jdbc:txmud:database
 */
public class TxMudDriver implements Driver
{
	public static int NUMDATACENTERS = 1;
	public static int THISDATACENTER = 1;
	
	static
	{
		try
		{
			TxMudDriver driverInst = new TxMudDriver() ;
			DriverManager.registerDriver( driverInst ) ;
		}
		catch( SQLException e ) { e.printStackTrace() ; }
	}

	public static ClosedLoopProxyInterface proxy = PassThroughProxy.getInstance();

	@Override
	public boolean acceptsURL(String url) throws SQLException {
		return url.startsWith("jdbc:txmud:");
	}

	@Override
	public Connection connect(String url, Properties props) throws SQLException {
		if( acceptsURL( url)) {
			return new TxMudConnection( url, props, proxy);
		} else
			throw new SQLException( "Cannot process url:" + url);
	}

	@Override
	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean jdbcCompliant() {
		// TODO Auto-generated method stub
		return false;
	}

	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		// TODO Auto-generated method stub
		return null;
	}

}
