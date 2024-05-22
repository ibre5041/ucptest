import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.security.MessageDigest;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.QueryTimeoutException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Settings;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

/***************
 * 
 * Sample standalone application UCP + Hibernate, Query timeout
 * 
 * run as:
 * mvn exec:exec
 * 
 * use -Djava.util.logging.config.file=logging.properties
 * 
 * https://brightinventions.pl/blog/database-timeouts/
 * http://javadeveloperz0ne.blogspot.com/2015/07/why-jpa-hints-on-namedquery-wont-work.html
 */
public class TimeoutHibernate 
{	
	// UCP XML config file location URI
	private static File ucpConfig = new File(TimeoutHibernate.class.getClassLoader().getResource("ucp.xml").getFile());
	private static File l4jConfig = new File(TimeoutHibernate.class.getClassLoader().getResource("logging.properties").getFile());
	private static final String ucpConfigURI = ucpConfig.toURI().toString();
	
    private static Session session;
    private Transaction transaction;

    // Iterate over Exceptions causes. try to find "oracle.jdbc.OracleDatabaseException" class and call getSql/getOriginalSql on it
    private static String sqlFromException(Throwable t)
    {
    	try {
    		while(t != null)
    		{
    			System.err.println(t.getClass().toString());
    			Class<?> tClass = t.getClass();
    			if (tClass.getName() != "oracle.jdbc.OracleDatabaseException")
    			{
    				t = t.getCause();
    				continue;
    			}
    			for (Method m : tClass.getMethods()) {
    				if (m.getName().equals("getSql")) {
    					String sql  = m.invoke(t).toString();
    					return sql;
    				}
    			}
    			t = t.getCause(); //never reached	    
    		}
    	} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
    	}
    	return "";
    }
    
    // Compute sqlid for a statement, the same way as Oracle does
    private static String sqlid(String stmt)
    {
    	// select sql_id from v$session where sid = (select distinct sid from v$mystat)
    	String result = "(sql_id)";

    	try
    	{
    		// compute MD5 sum from SQL string - including trailing 0x00 Byte
    		byte[] message  = (stmt).getBytes("utf8");
    		byte[] bytesMessage = new byte[message.length+1];
    		System.arraycopy(message, 0, bytesMessage, 0, message.length);
    		MessageDigest md = MessageDigest.getInstance("MD5");
    		byte[] b = md.digest(bytesMessage);

    		// most significant unsigned int
    		long val_msb = (((b[11] & 0xff) * 0x100 + (b[10] & 0xff)) * 0x100 + (b[9] & 0xff))  * 0x100 + (b[8] & 0xff);
    		val_msb = Integer.toUnsignedLong((int)val_msb);

    		// least significant unsigned int
    		long val_lsb = (((b[15] & 0xff) * 0x100 + (b[14] & 0xff)) * 0x100 + (b[13] & 0xff)) * 0x100 + (b[12] & 0xff);
    		val_lsb = Integer.toUnsignedLong((int)val_lsb);

    		// Java does not have unsigned long long, use BigInteger as bite array
    		BigInteger sqln = BigInteger.valueOf(val_msb);
    		sqln = sqln.shiftLeft(32);
    		sqln = sqln.add(BigInteger.valueOf(val_lsb));

    		// Compute Base32, take 13x 5bits
    		char alphabet [] = new String("0123456789abcdfghjkmnpqrstuvwxyz").toCharArray();
    		result = "";
    		for (int i = 0; i < 13; i++) // max sql_id length is 13 chars, 13 x 5 => 65bits most significant is always 0
    		{
    			int idx = sqln.and(BigInteger.valueOf(31)).intValue();
    			result = alphabet[idx] + result;
    			sqln = sqln.shiftRight(5);
    		}
    	} catch (Exception e) {

    	}
    	return result;
    }

    public static void main( String[] args ) throws Exception
    {
		//Override system DNS setting with Google free DNS server
		System.setProperty("sun.net.spi.nameservice.nameservers", "192.168.8.200");
		System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
    	
    	System.setProperty("oracle.ucp.jdbc.xmlConfigFile", ucpConfigURI);
    	// Bug 22644072 : ORA-01013 ON SUBSEQUENT QUERIES WHEN USING SETQUERYTIMEOUT
    	// JDBC Application Getting ORA-12592: TNS:bad packet when a Query Timeout is Reached (Doc ID 2162761.1) - fixed in 12.2 JDBC drivers
    	// ORA-12592: TNS:bad Packet After 12c JDBC Driver Upgrade (Doc ID 2196978.1)
    	// Grr, it looks like Cisco AnyConnect SSL VPN client merges OOB packets into TCP stream - comment this out, if you're beyond VPN
    	// System.setProperty("oracle.net.disableOob", "true");
    	
    	Logger.getLogger("oracle.ucp").setLevel(Level.FINEST);
    	Logger.getLogger("oracle.ucp.jdbc.PoolDataSource").setLevel(Level.FINEST);
    	
    	    
    	SessionFactory sessionFactory = new Configuration()        	
                .configure() // configures settings from hibernate.cfg.xml
                .addAnnotatedClass(BookEntity.class)
                .buildSessionFactory();

    	SqlStatementLogger sqlloger = sessionFactory.getSessionFactory().getJdbcServices().getSqlStatementLogger();
    	SqlExceptionHelper exhelper = sessionFactory.getSessionFactory().getJdbcServices().getSqlExceptionHelper();
    	JdbcServices jdbcservices = sessionFactory.getSessionFactory().getJdbcServices();
    	
    	
    	Field field = jdbcservices.getClass().getDeclaredField("sqlStatementLogger");
    	// Allow modification on the field
    	field.setAccessible(true);
    	// Sets the field to the new value for this instance
    	field.set(jdbcservices, new CustomSqlStatementLogger());

        session = sessionFactory.openSession();
        
        Transaction tx = session.beginTransaction();        
        
        try {
        	Query<BookEntity> query = session.createNamedQuery("Last Book slow", BookEntity.class);
        	BookEntity result = query.getSingleResult();
        	System.out.println(result);
        } catch (QueryTimeoutException e) {
        	String sql = sqlFromException(e);
        	String sqlid = sqlid(sql);
        	System.err.println(sqlid);
        	System.err.println(sql);
        	e.printStackTrace();
        	tx.rollback();
        }

        try {
        	Query<BookEntity> query = session.createNamedQuery("Last Book slow", BookEntity.class);
        	BookEntity result = query.getSingleResult();
        	System.out.println(result);
        } catch (QueryTimeoutException e) {
        	String sql = sqlFromException(e);
        	String sqlid = sqlid(sql);
        	System.err.println(sqlid);
        	System.err.println(sql);
        	e.printStackTrace();
        	tx.rollback();
        }
    }
}
