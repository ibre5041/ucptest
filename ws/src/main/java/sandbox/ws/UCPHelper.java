package sandbox.ws;
 
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.ValidConnection;
import java.sql.ResultSet;
import javax.naming.*;
 
public class UCPHelper {
    PoolDataSource m_pds;
 
    public Connection getConnection(String jndiName) 
        throws Exception
    {
        Context ctx = new InitialContext();
        Context envContext = (Context) ctx.lookup("java:/comp/env");
         
        m_pds = (PoolDataSource) envContext.lookup (jndiName);
 
        return m_pds.getConnection();
    }
 
    public boolean isReplayConnection(Connection conn) {
        if (conn instanceof oracle.jdbc.replay.ReplayableConnection) {
            return true;
        } else {
            return false;
        }
    }
 
    public void executePLSQL(String message, Connection conn) 
        throws Exception
    {
        java.sql.CallableStatement cstmt = conn.prepareCall("{call canIcarryOn(?)}");
        cstmt.setString(1, message);
        cstmt.execute();
    }
 
}
