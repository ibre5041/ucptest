package sandbox.ws;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.jdbc.OracleDriver;
import oracle.ucp.jdbc.PoolDataSource;

@WebServlet("/statusPage")
public class StatusPage extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		printTable(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		printTable(request, response);
		
	}

	public void printTable(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		out.print("<HTML><HEAD><TITLE> " + "Print UCP Status" + " </TITLE></HEAD><BODY>");
				
		try {
			Context ctx = new InitialContext();
			Context envContext = (Context) ctx.lookup("java:/comp/env");

			// Look up a data source
			javax.sql.DataSource ds =(javax.sql.DataSource)envContext.lookup("jdbc/UCPPool");
			PoolDataSource pds = (PoolDataSource) ds;
						
			out.println("<PRE>");
			out.println("ConnectionFactoryClassName:" + pds.getConnectionFactoryClassName());
			out.println("Driver version: " + OracleDriver.getDriverVersion());
			out.println("\tbuild date: " + OracleDriver.getBuildDate());
			out.println("\tJDBC ver:" + OracleDriver.getJDBCVersion());
			out.println("\tDebug: " + String.valueOf(OracleDriver.isDebug()));
			out.println();
			out.println("InitialPoolSize: " + pds.getInitialPoolSize());
			out.println("MinPoolSize: " + pds.getMinPoolSize());
			out.println("MaxPoolSize: " + pds.getMaxPoolSize());
			out.println("ConnectionPoolName: " + pds.getConnectionPoolName());
			out.println("FastConnectionFailoverEnabled: " + String.valueOf(pds.getFastConnectionFailoverEnabled()));
			out.println("ONSConfiguration: " + pds.getONSConfiguration());
			out.println();
			out.println("AbandonedConnectionTimeout: " +pds.getAbandonedConnectionTimeout());
			out.println("ConnectionWaitTimeout: " + pds.getConnectionWaitTimeout());
			out.println("TimeToLiveConnectionTimeout: " + pds.getTimeToLiveConnectionTimeout());
			out.println("TimeoutCheckInterval: " + pds.getTimeoutCheckInterval());
			out.println("ValidateConnectionOnBorrow: " + pds.getValidateConnectionOnBorrow());
			out.println("AvailableConnectionsCount: " + pds.getAvailableConnectionsCount());
			out.println("BorrowedConnectionsCount: " + pds.getBorrowedConnectionsCount());
			out.println("MaxConnectionReuseTime: " + pds.getMaxConnectionReuseTime());
			out.println("ConnectionReuseCount: " + pds.getMaxConnectionReuseCount());
			out.println("SecondsToTrustIdleConnection: " + pds.getSecondsToTrustIdleConnection());
			out.println("FailoverEnabled: " + pds.getFastConnectionFailoverEnabled());
			
//			oracle.ucp.jdbc.JDBCConnectionPoolStatistics stats = pds.getStatistics();
//			out.println("\tgetAvailableConnectionsCount() " + stats.getAvailableConnectionsCount());
//			out.println("\tgetBorrowedConnectionsCount() " + stats.getBorrowedConnectionsCount() );
//			out.println("\tgetRemainingPoolCapacityCount() " + stats.getRemainingPoolCapacityCount());
//			out.println("\tgetTotalConnectionsCount() " + stats.getTotalConnectionsCount());

			//out.println(((oracle.ucp.jdbc.oracle.OracleJDBCConnectionPoolStatistics)pds.getStatistics()).get-FCFProcessingInfo());
			//out.println(((oracle.ucp.jdbc.oracle.OracleJDBCConnectionPoolStatistics)pds.getStatistics()).getFCFProcessingInfoProcessedOnly());			
			
			out.println();
			try (Connection conn = pds.getConnection()) 
			{
				out.println("Connection class: " + conn.getClass().getName());
		        if (conn instanceof oracle.jdbc.replay.ReplayableConnection) {
		            out.println("ReplayableConnection: true");
		        } else {
		        	out.println("ReplayableConnection: false");
		        }
			}
			
			out.println("</PRE>");

		} catch (NamingException e) {
			out.println("NamingException in StatusPage");
			for(StackTraceElement ste : e.getStackTrace())
				out.println(ste.toString());
		} catch (SQLException e) {
			out.println("SQLException in StatusPage");
			for(StackTraceElement ste : e.getStackTrace())
				out.println(ste.toString());
			throw new ServletException(this.getClass()	.getSimpleName(), e);
		} catch (NullPointerException e) {
			out.println("NullPointerException in StatusPage");
			for(StackTraceElement ste : e.getStackTrace())
				out.println(ste.toString());			
		}
		
		out.print("</BODY></HTML>");
	}
}
