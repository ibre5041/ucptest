package sandbox.ucp.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import oracle.ucp.jdbc.PoolDataSource;

public class ConnectionHelper {
	private static ConnectionHelper instance = null;
	
	protected ConnectionHelper() {
		try {
			Context ctx = new InitialContext();
			Context envContext = (Context) ctx.lookup("java:/comp/env");
			// see context.xml mapping this name onto Resource defined in Tomcat's server.xml
			m_pds = (PoolDataSource) envContext.lookup ("jdbc/TomcatUcpRestDataSource");
			//m_pds.registerConnectionLabelingCallback(new BooksConnectionLabelingCallback());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public static ConnectionHelper getInstance() {
		if(instance == null) {
			instance = new ConnectionHelper();
		}
		return instance;
	}
	   
	PoolDataSource m_pds;	
	
	public Connection getConnection()  throws Exception {
		Connection connection = m_pds.getConnection();
		connection.setAutoCommit(false); // TODO set this in UCP.xml
		return connection;
	}
}
