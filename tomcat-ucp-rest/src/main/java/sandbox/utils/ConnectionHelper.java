package sandbox.utils;

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

			m_pds = (PoolDataSource) envContext.lookup ("jdbc/UCPPoolFromContextXmlA");
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
//		java.util.Properties props = new java.util.Properties();
//		props.put("v$session.osuser", System.getProperty("user.name").toString());
//		props.put("v$session.machine", InetAddress.getLocalHost().getCanonicalHostName());
//		props.put("v$session.program", "My Program Name");
//		Connection connection = m_pds.getConnection(props);
		Connection connection = m_pds.getConnection();
		connection.setAutoCommit(false);
		return connection;
	}
}
