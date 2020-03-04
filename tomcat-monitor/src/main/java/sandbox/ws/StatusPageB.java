package sandbox.ws;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.jdbc.OracleDriver;
import oracle.ucp.UniversalConnectionPool;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.UniversalConnectionPoolLifeCycleState;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

/*************************************************************
 * 
 * Adopted from:
 * https://blogs.oracle.com/dev2dev/using-ucp-multi-tenant-shared-pool-feature-with-tomcat
 * 
 * Copy ws/src/main/resources/context.xml.tmpl as into  ${CATALINA_BASE}/conf/context.xml.tmpl
 *
 * Set Tomcat properties:
 * 
 * 1.
 * create file ${CATALINA_BASE}/bin/setenv.{sh,bat}, oracle.ucp.jdbc.xmlConfigFile should point into FileURI ucp.xml
 *  
 * CATALINA_OPTS="-Doracle.ucp.jdbc.xmlConfigFile=file:/"`cygpath -am ${CATALINA_BASE}/conf/ucp.xml`
 * or
 * CATALINA_OPTS="-Doracle.ucp.jdbc.xmlConfigFile=file:/${CATALINA_BASE}/conf/ucp.xml"
 * 
 * 2. For each datasource in ucp.xml create global JNDI resource (*having* dataSourceFromConfiguration property).
 * This tell UCP, that all the datasource's parameters should be read from ucp.xml
 * Amend ${CATALINA_BASE}/conf/server.xml
 * <GlobalNamingResources>
 *     <Resource auth="Container"
 *            dataSourceFromConfiguration="UCPPoolFromUcpXmlA"
 *            factory="oracle.ucp.jdbc.PoolDataSourceImpl"
 *            global="jdbc/UCPPoolFromContextXmlA"
 *            name="jdbc/UCPPoolFromContextXmlA"
 *            type="oracle.ucp.jdbc.PoolDataSource"
 *           />
 *           
 * 3. Link global Resource with application
 * In context.xml add mapping:
 * <ResourceLink auth="Container" global="jdbc/UCPPoolFromContextXmlA" name="jdbc/UCPPoolFromContextXmlA" type="javax.sql.DataSource"/>
 * 
 * Access datasource either via JNDI lookup (StatusPageA.java):
 * PoolDataSource pds =(PoolDataSource)envContext.lookup("jdbc/UCPPoolFromContextXmlA");
 * 
 * or directly from pool manager class (StatusPageB.java):
 * PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource("UCPPoolFromUcpXmlA");
 * 
 * PS: possibly also make changes in Eclipse workspace directory:
 * workspace/Servers/Tomcat v9.0 Server at localhost-config/context.xml
 */

@WebServlet("/statusPageB")
public class StatusPageB extends HttpServlet {

	private static final long serialVersionUID = 1L;

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
						
		try {
			out.print("<HTML><HEAD><TITLE> " + "Print UCP Status" + " </TITLE></HEAD><BODY><PRE>");
	        
			// Look up a data source
			PoolDataSource pds = PoolDataSourceFactory.getPoolDataSource("UCPPoolFromUcpXmlA");
	        {
	        	String file = System.getProperty("oracle.ucp.jdbc.xmlConfigFile");
	        	UniversalConnectionPoolManager mgr = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();

	        	mgr.setLogLevel(Level.FINE);
	        	Logger.getLogger("oracle.ucp").setLevel(Level.FINEST);
	        	Logger.getLogger("oracle.ucp.jdbc.PoolDataSource").setLevel(Level.FINEST);

	        	String[] pools = mgr.getConnectionPoolNames();

	        	UniversalConnectionPool pool = mgr.getConnectionPool("UCPPOOL");
	        	UniversalConnectionPoolLifeCycleState state = pool.getLifeCycleState();
	        	//mgr.startConnectionPool("UCPPoolA");
	        	// Get the datasource instance, named as "UCPPoolA1" in xml config file ${CATALINA_BASE}/conf/ucp.xml
	        	PoolDataSource pdsb = PoolDataSourceFactory.getPoolDataSource("UCPPoolFromUcpXmlB");
	        }

			out.println("PoolDataSource id:" + System.identityHashCode(pds));
			out.println("ConnectionFactoryClassName:" + pds.getConnectionFactoryClassName());
			out.println("Driver version: " + OracleDriver.getDriverVersion());
			out.println("\tbuild date: " + OracleDriver.getBuildDate());
			out.println("\tJDBC ver:" + OracleDriver.getJDBCVersion());
			out.println("\tDebug: " + String.valueOf(OracleDriver.isDebug()));
			out.println("\tall conn props:" + pds.getConnectionProperties().toString());			
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
			out.println("MaxConnectionReuseTime" + pds.getMaxConnectionReuseTime());
			
			oracle.ucp.jdbc.JDBCConnectionPoolStatistics stats = pds.getStatistics();
			out.println("\tgetAvailableConnectionsCount() " + stats.getAvailableConnectionsCount());
			out.println("\tgetBorrowedConnectionsCount() " + stats.getBorrowedConnectionsCount() );
			out.println("\tgetRemainingPoolCapacityCount() " + stats.getRemainingPoolCapacityCount());
			out.println("\tgetTotalConnectionsCount() " + stats.getTotalConnectionsCount());

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
		        
		        out.println("Autocommit: " + String.valueOf(conn.getAutoCommit()));		        
			}
			
			out.println("</PRE>");

		} catch (UniversalConnectionPoolException | NullPointerException | SQLException e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));		
			out.println(errors.toString());
			throw new ServletException(this.getClass().getSimpleName(), e);
		} finally {
			out.print("</PRE></BODY></HTML>");
		}
	}
}
