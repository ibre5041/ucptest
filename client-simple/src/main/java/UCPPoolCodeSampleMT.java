import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

public class UCPPoolCodeSampleMT {
	
	private static class RunnableThread implements Runnable {
		private Thread t;
		private String threadName;
		private PoolDataSource pool;

		RunnableThread( String name, PoolDataSource pool) {
			this.threadName = name;
			this.pool = pool;
			System.out.println("Creating " +  threadName );
		}

		private static void runQuery(Connection conn) {
			try {
				String sql = "SELECT ID, NAME, AUTHOR FROM BOOK WHERE ROWNUM < 10";
				try(Statement st = conn.createStatement()) {
					try(ResultSet rs = st.executeQuery(sql)) {
						//System.out.println("Books Details:");
						while (rs.next()) {
							//System.out.println("ID = " + rs.getLong("ID") + "     NAME = " + rs.getString("NAME"));
						}
						rs.close();
					}
					st.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		public void run() {
			System.out.println("Running " +  threadName );

			// Get a connection from datasource
			try(Connection conn = pool.getConnection()) {
				for(int i = 10; i > 0; i--) {
					// Run a query on the connection
					runQuery(conn);
					//System.out.println("Thread: " + threadName + ", " + i);
					// Let the thread sleep for a while.
					Thread.sleep(500);					
				}
				
				// return connection to the pool
				conn.close();
			} catch (InterruptedException | SQLException e) {
				System.out.println("Thread " +  threadName + " interrupted.");
			}
			System.out.println("Thread " +  threadName + " exiting.");
		}

		public void start () {
			System.out.println("Starting " +  threadName );
			if (t == null) {
				t = new Thread (this, threadName);
				t.start ();
			}
		}
	}

	// UCP XML config file location URI
	private static File ucpConfig = new File(UCPPoolCodeSample.class.getClassLoader().getResource("ucp.xml").getFile());
	private static final String ucpConfigURI = ucpConfig.toURI().toString();

	public static void main(String args[]) throws Exception {
		// Java system property to specify the location of UCP XML configuration
		// file which has shared pool, datasource properties defined in it.
		System.setProperty("oracle.ucp.jdbc.xmlConfigFile", ucpConfigURI);
		
		// Get the datasource instance named as "UCPPoolA1" in XML config file ucp.xml
		PoolDataSource ds = PoolDataSourceFactory.getPoolDataSource("UCPPoolA1");
		
		// Start N parallel working threads - where N is close to pool size
		for (int i = 1; i <= ds.getMaxPoolSize() - 5; i++)
		{
			RunnableThread R1 = new RunnableThread("Thread-"+i, ds);
			R1.start();
		}
		
		for (int i = 0; i < 10; i++)
		{
			System.out.println("Borrowed connections: " + ds.getStatistics().getBorrowedConnectionsCount() + "\n");
			Thread.sleep(1000);
		}
	}   
}
