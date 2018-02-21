package sandbox.ws;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import oracle.jdbc.OracleDriver;
import oracle.ucp.jdbc.PoolDataSource;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {

	/**
	 * Method handling HTTP GET requests. The returned object will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {

		StringBuilder sb = new StringBuilder(2048);		
		try {
			Context ctx = new InitialContext();
			Context envContext = (Context) ctx.lookup("java:/comp/env");

			// Look up a data source
			javax.sql.DataSource ds =(javax.sql.DataSource)envContext.lookup("jdbc/UCPPool");
			PoolDataSource pds = (PoolDataSource) ds;

			sb.append("ConnectionFactoryClassName:" + pds.getConnectionFactoryClassName());
			sb.append("Driver version: " + OracleDriver.getDriverVersion());
			sb.append("\tbuild date: " + OracleDriver.getBuildDate());
			sb.append("\tJDBC ver:" + OracleDriver.getJDBCVersion());
			sb.append("\tDebug: " + String.valueOf(OracleDriver.isDebug()));
			sb.append("\n");
			sb.append("InitialPoolSize: " + pds.getInitialPoolSize());
			sb.append("MinPoolSize: " + pds.getMinPoolSize());
			sb.append("MaxPoolSize: " + pds.getMaxPoolSize());
			sb.append("ConnectionPoolName: " + pds.getConnectionPoolName());
			sb.append("FastConnectionFailoverEnabled: " + String.valueOf(pds.getFastConnectionFailoverEnabled()));
			sb.append("ONSConfiguration: " + pds.getONSConfiguration());
			sb.append("\n");
			sb.append("AbandonedConnectionTimeout: " +pds.getAbandonedConnectionTimeout());
			sb.append("ConnectionWaitTimeout: " + pds.getConnectionWaitTimeout());
			sb.append("TimeToLiveConnectionTimeout: " + pds.getTimeToLiveConnectionTimeout());
			sb.append("TimeoutCheckInterval: " + pds.getTimeoutCheckInterval());
			sb.append("ValidateConnectionOnBorrow: " + pds.getValidateConnectionOnBorrow());
			sb.append("AvailableConnectionsCount: " + pds.getAvailableConnectionsCount());
			sb.append("BorrowedConnectionsCount: " + pds.getBorrowedConnectionsCount());
			sb.append("MaxConnectionReuseTime: " + pds.getMaxConnectionReuseTime());
			sb.append("ConnectionReuseCount: " + pds.getMaxConnectionReuseCount());
			sb.append("SecondsToTrustIdleConnection: " + pds.getSecondsToTrustIdleConnection());
			sb.append("FailoverEnabled: " + pds.getFastConnectionFailoverEnabled());

			//		oracle.ucp.jdbc.JDBCConnectionPoolStatistics stats = pds.getStatistics();
			//		sb.append("\tgetAvailableConnectionsCount() " + stats.getAvailableConnectionsCount());
			//		sb.append("\tgetBorrowedConnectionsCount() " + stats.getBorrowedConnectionsCount() );
			//		sb.append("\tgetRemainingPoolCapacityCount() " + stats.getRemainingPoolCapacityCount());
			//		sb.append("\tgetTotalConnectionsCount() " + stats.getTotalConnectionsCount());

			//sb.append(((oracle.ucp.jdbc.oracle.OracleJDBCConnectionPoolStatistics)pds.getStatistics()).get-FCFProcessingInfo());
			//sb.append(((oracle.ucp.jdbc.oracle.OracleJDBCConnectionPoolStatistics)pds.getStatistics()).getFCFProcessingInfoProcessedOnly());			

			sb.append("\n");
			try (Connection conn = pds.getConnection()) 
			{
				sb.append("Connection class: " + conn.getClass().getName());
				if (conn instanceof oracle.jdbc.replay.ReplayableConnection) {
					sb.append("ReplayableConnection: true");
				} else {
					sb.append("ReplayableConnection: false");
				}
			}

		} catch (NamingException | SQLException e) {
			sb.append("NamingException in StatusPage");
			for(StackTraceElement ste : e.getStackTrace())
				sb.append(ste.toString());
		}
		return sb.toString();
	}

}
