package sandbox.dbcp.utils;

import java.sql.Connection;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

public class ConnectionHelper {
	private static ConnectionHelper instance = null;
	
	protected ConnectionHelper() {
		try {
			Context ctx = new InitialContext();
			Context envContext = (Context) ctx.lookup("java:/comp/env");

			m_pds = (DataSource) envContext.lookup ("jdbc/DBCPOracle");
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
	   
	DataSource m_pds;	
	
	public Connection getConnection()  throws Exception {
		Connection connection = m_pds.getConnection();
		connection.setAutoCommit(false);
		return connection;
	}
}
