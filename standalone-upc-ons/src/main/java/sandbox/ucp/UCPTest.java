// Playing with Application Continuity in RAC 12c
// https://martincarstenbach.wordpress.com/2013/12/13/playing-with-application-continuity-in-rac-12c/

package sandbox.ucp;

import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.ValidConnection;
import sandbox.fan.SimpleFan;

import java.sql.ResultSet;
import oracle.jdbc.OracleDriver;

import java.lang.Thread;
import java.nio.file.Path;
import java.nio.file.Paths;

/***************
 * 
 * Sample standalone application UCP
 * 
 * run as:
 * mvn exec:exec
 * 
 * reclocate resouce while the app is runnning:
 * /oracle/u01/db/12.1.0.2/DBA$ srvctl relocate service -d RHRAC -s ACTEST -oldinst RHRAC2 -newinst RHRAC1
 * /oracle/u01/db/12.1.0.2/DBA$ srvctl relocate service -d RHRAC -s ACTEST -oldinst RHRAC1 -newinst RHRAC2
 *
 */

public class UCPTest {

	public static Properties loadProperties() {
		String location = BinaryPath.getLocation(UCPTest.class);
		Path path = Paths.get(location, "database.properties");
		Properties props = new Properties();
		InputStream is;
		if(Files.exists(path)) {
			System.out.println("Loading external properties:" + path.toString());
            try {
				is = new FileInputStream(new File(path.toString()));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
			System.out.println("Loading internal properties");
			is = UCPTest.class.getResourceAsStream("/database.properties");
		}

		try {
			if (is != null) {
				props.load(is);
				is.close();
			}
		}
		catch(IOException ioe) {
			System.err.println("IOException in loadProps");
			for(StackTraceElement ste : ioe.getStackTrace())
				System.err.println(ste.toString());
			System.exit(-1);
		}
		return props;
	}


	public static void main (String[] args) throws Exception
	{
		String message;

		if (args.length == 0) {
			message = "default";
		} else {
			message = args[0];
		}

		String propertyLocation = BinaryPath.getLocation(UCPTest.class);
		System.out.println("Location: " + propertyLocation);

		// Set the variable with the absolute path
		String absolutePath = new File(propertyLocation).getAbsolutePath();
		System.out.println("Absolute Path: " + absolutePath);

		PoolDataSource  pds = PoolDataSourceFactory.getPoolDataSource();
		pds.setConnectionFactoryClassName("oracle.jdbc.replay.OracleDataSourceImpl");

		System.out.println("Driver version: " + OracleDriver.getDriverVersion());
		System.out.println("\tbuild date: " + OracleDriver.getBuildDate());
		System.out.println("\tJDBC ver:" + OracleDriver.getJDBCVersion());
		System.out.println("\tDebug: " + String.valueOf(OracleDriver.isDebug()));
		System.out.println("connection factory set");

		System.out.println("PWD: " + new java.io.File( "." ).getCanonicalPath());
		Properties props = UCPTest.loadProperties();

		System.out.println("Using URL: " + props.getProperty("url"));
		pds.setURL(props.getProperty("url"));
		pds.setUser(props.getProperty("username"));
		pds.setPassword(props.getProperty("password"));

		pds.setInitialPoolSize(10);
		pds.setMinPoolSize(10);
		pds.setMaxPoolSize(20);

		// RAC Features
		pds.setConnectionPoolName("Application Continuity Pool");
		pds.setFastConnectionFailoverEnabled(true);

		// use srvctl config nodeapps to get the ONS ports on the cluster
		//pds.setONSConfiguration(props.getProperty("onsnodes"));

		System.out.println("pool configured, trying to get a connection");

		Connection conn = pds.getConnection();
		if (conn == null || !((ValidConnection) conn).isValid())  {

			System.out.println("connection is not valid");
			throw new Exception ("invalid connection obtained from the pool");
		}

		if ( conn instanceof oracle.jdbc.replay.ReplayableConnection ) {
			System.out.println("got a replay data source");
		} else {
			System.out.println("this is not a replay data source. Why not?");
		}

		System.out.println("got a connection! Getting some stats if possible");
		oracle.ucp.jdbc.JDBCConnectionPoolStatistics stats = pds.getStatistics();
		System.out.println("\tgetAvailableConnectionsCount() " + stats.getAvailableConnectionsCount());
		System.out.println("\tgetBorrowedConnectionsCount() " + stats.getBorrowedConnectionsCount() );
		System.out.println("\tgetRemainingPoolCapacityCount() " + stats.getRemainingPoolCapacityCount());
		System.out.println("\tgetTotalConnectionsCount() " + stats.getTotalConnectionsCount());

		System.out.println(((oracle.ucp.jdbc.oracle.OracleJDBCConnectionPoolStatistics)pds.getStatistics()).getFCFProcessingInfo());

		System.out.println("Now working");

		java.sql.CallableStatement cstmt = conn.prepareCall("{call canIcarryOn(?)}");
		cstmt.setString(1, message);
		cstmt.execute();

		System.out.println("Statement executed. Now closing down");
		System.out.println("Almost done! Getting some more stats if possible");

		stats = pds.getStatistics();
		System.out.println("\tgetAvailableConnectionsCount() " + stats.getAvailableConnectionsCount());
		System.out.println("\tgetBorrowedConnectionsCount() " + stats.getBorrowedConnectionsCount() );
		System.out.println("\tgetRemainingPoolCapacityCount() " + stats.getRemainingPoolCapacityCount());
		System.out.println("\tgetTotalConnectionsCount() " + stats.getTotalConnectionsCount());

		System.out.println(((oracle.ucp.jdbc.oracle.OracleJDBCConnectionPoolStatistics)pds.getStatistics()).getFCFProcessingInfo());

		System.out.println("Sleeping");
		Thread.sleep(1000000);
		
		cstmt.close();
		conn.close();
		conn = null;
		System.out.println("Done");
	}
}
