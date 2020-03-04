package sandbox;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/***************************************************************
 * 
 * http://192.168.8.200:8080/ds-rest/OracleUCPJBoss
 *
 */

// URL to reach the Servlet
@WebServlet("/OracleUCPJBoss")
public class OracleUCPJBoss extends HttpServlet {
  private static final long serialVersionUID = 1L;
 
  // Pool Datasource reference, to be instantiated at init
  @Resource(lookup="java:/datasources/mypool")
  private DataSource ds = null;
  
  // Retrieve Datasource reference using JNDI
  @Override
  public void init() throws ServletException {
//    Context initContext;
//    try {
//      initContext = new InitialContext();
//      ds = (DataSource) initContext.lookup("java:/datasources/mypool");
//    } catch (NamingException e) {
//      e.printStackTrace();
//    }
  }
  
  // GET request handling
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    // Retrieve connection from the pool
    try (Connection conn = ds.getConnection(); Statement st = conn.createStatement()) {
    	
      // Initialize output and retrieve parameters
      PrintWriter pw = response.getWriter();
      String job = request.getParameter("job");
      String sql = "select id, name, author from book";
            
      // List employees. If job parameter is sent, filter this list      
      if (job != null) {
    	  sql +=  "where id = '" + job + "'";      
      }
      
      try (ResultSet rs = st.executeQuery(sql)) {
    	  // Show list on browser
    	  while (rs.next()) {
    		  pw.println(rs.getString("id") + " - " + rs.getString("name") + " - " + rs.getString("author"));
    	  }
      }
      // Debug info
      pw.println("Served at: " + request.getContextPath());
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  // Re-route any Post request
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    doGet(request, response);
  }
}
