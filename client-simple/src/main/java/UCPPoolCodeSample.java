import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;


public class UCPPoolCodeSample {
	
	// UCP XML config file location URI
	private static File ucpConfig = new File(UCPPoolCodeSample.class.getClassLoader().getResource("ucp.xml").getFile());
	private static final String ucpConfigURI = ucpConfig.toURI().toString();

	public static void main(String[] args) throws Exception {
		System.out.println("Shared pool configuration using XML: " + ucpConfigURI);

		// Java system property to specify the location of UCP XML configuration
		// file which has shared pool, datasource properties defined in it.
		System.setProperty("oracle.ucp.jdbc.xmlConfigFile", ucpConfigURI);
		
		// Get the datasource instance named as "UCPPoolA1" in XML config file ucp.xml
		PoolDataSource ds1 = PoolDataSourceFactory.getPoolDataSource("UCPPoolA1");

		// Get a connection from datasource
		try(Connection conn1 = ds1.getConnection()) {

			// Run a query on the connection
			runQuery(conn1);

			// return connection to the pool
			conn1.close();
		}

		// Get the datasource
		PoolDataSource ds2 = PoolDataSourceFactory.getPoolDataSource("UCPPoolA2");

		// Get a connection from datasource
		try(Connection conn2 = ds2.getConnection()) {

			// Run a query on the connection obtained using tenant2 datasource i.e.
			// tenant2_ds
			runQuery(conn2);

			// return connection to the pool
			conn2.close();
		}

	}

	/**
	 * Runs a query on the book table using the given connection.
	 */
	private static void runQuery(Connection conn) {
		try {
			String sql = "SELECT ID, NAME, AUTHOR FROM BOOK WHERE ROWNUM < 10";
			try(Statement st = conn.createStatement()) {
				try(ResultSet rs = st.executeQuery(sql)) {
					System.out.println("Books Details:");
					while (rs.next()) {
						System.out.println("ID = " + rs.getLong("ID") + "     NAME = " + rs.getString("NAME"));
					}
					rs.close();
				}
				st.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
