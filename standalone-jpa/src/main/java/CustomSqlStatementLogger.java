// https://pokurinaresh.wordpress.com/category/java/hibernate/

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.ParameterMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.engine.jdbc.spi.SqlStatementLogger;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.build.AllowSysOut;

import org.jboss.logging.Logger;

import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.driver.OracleSql;

public class CustomSqlStatementLogger extends SqlStatementLogger {
	private static final Logger LOG = CoreLogging.logger( "org.hibernate.SQL" );
	private static final Logger LOG_SLOW = CoreLogging.logger( "org.hibernate.SQL_SLOW" );
	
	private static final String UNKNOWN = new String("UNKNOWN");

	private boolean logToStdout;
	private boolean format;

	/**
	 * Configuration value that indicates slow query. (In milliseconds) 0 - disabled.
	 */
	private final long logSlowQuery;

	/**
	 * Constructs a new SqlStatementLogger instance.
	 */
	public CustomSqlStatementLogger() {
		this( false, false );
	}

	/**
	 * Constructs a new SqlStatementLogger instance.
	 *
	 * @param logToStdout Should we log to STDOUT in addition to our internal logger.
	 * @param format Should we format the statements prior to logging
	 */
	public CustomSqlStatementLogger(boolean logToStdout, boolean format) {
		this( logToStdout, format, 0 );
	}

	/**
	 * Constructs a new SqlStatementLogger instance.
	 *
	 * @param logToStdout Should we log to STDOUT in addition to our internal logger.
	 * @param format Should we format the statements prior to logging
	 * @param logSlowQuery Should we logs query which executed slower than specified milliseconds. 0 - disabled.
	 */
	public CustomSqlStatementLogger(boolean logToStdout, boolean format, long logSlowQuery) {
		this.logToStdout = logToStdout;
		this.format = format;
		this.logSlowQuery = logSlowQuery;
	}

	/**
	 * Are we currently logging to stdout?
	 *
	 * @return True if we are currently logging to stdout; false otherwise.
	 */
	public boolean isLogToStdout() {
		return logToStdout;
	}

	/**
	 * Enable (true) or disable (false) logging to stdout.
	 *
	 * @param logToStdout True to enable logging to stdout; false to disable.
	 *
	 * @deprecated Will likely be removed:
	 * Should either become immutable or threadsafe.
	 */
	@Deprecated
	public void setLogToStdout(boolean logToStdout) {
		this.logToStdout = logToStdout;
	}

	public boolean isFormat() {
		return format;
	}

	/**
	 * @deprecated Will likely be removed:
	 * Should either become immutable or threadsafe.
	 */
	@Deprecated
	public void setFormat(boolean format) {
		this.format = format;
	}

	public long getLogSlowQuery() {
		return logSlowQuery;
	}

	/**
	 * Log a SQL statement string.
	 *
	 * @param statement The SQL statement.
	 */
	public void logStatement(String statement) {
		// for now just assume a DML log for formatting
		logStatement( statement, FormatStyle.BASIC.getFormatter() );
	}

	/**
	 * Log a SQL statement string using the specified formatter
	 *
	 * @param statement The SQL statement.
	 * @param formatter The formatter to use.
	 */
	@AllowSysOut
	public void logStatement(String statement, Formatter formatter) {
		if ( format ) {
			if ( logToStdout || LOG.isDebugEnabled() ) {
				statement = formatter.format( statement );
			}
		}
		LOG.debug( statement );
		if ( logToStdout ) {
			System.out.println( "Hibernate: " + statement );
		}
	}

	/**
	 * Log a slow SQL query
	 *
	 * @param statement SQL statement.
	 * @param startTime Start time in milliseconds.
	 */
	public void logSlowQuery(Statement statement, long startTime) {
		if ( logSlowQuery < 1 ) {
			return;
		}	

		String sql_id = UNKNOWN;
		String sql = actualSQL(statement);
		sql_id = SQL_ID(sql);	    
		logSlowQuery( sql_id, startTime );
	}

	/**
	 * Log a slow SQL query
	 *
	 * @param sql The SQL query.
	 * @param startTime Start time in milliseconds.
	 */
	@AllowSysOut
	public void logSlowQuery(String sql, long startTime) {
		if ( logSlowQuery < 1 ) {
			return;
		}
		assert startTime > 0 : "startTime is invalid!";

		long spent = System.currentTimeMillis() - startTime;

		assert spent >= 0 : "startTime is invalid!";

		if ( spent > logSlowQuery ) {
			String logData = "SlowQuery: " + spent + " milliseconds. SQL_ID: '" + SQL_ID(sql) + "'";
			LOG_SLOW.info( logData );
			if ( logToStdout ) {
				System.out.println( logData );
			}
		}
	}
	
	
	/**
	 * Oracle JDBC woodo for JDBC 19.3 drivers
	 * extract actual SQL being executed
	 */
	private String actualSQL(Statement statement)
	{
		try {
			// Check if Statement was proxied through UCP proxy
			if (statement instanceof oracle.ucp.jdbc.proxy.oracle.StatementProxy)
			{
				Statement unwrapedStatement = statement.unwrap(Statement.class);				

				if (unwrapedStatement instanceof oracle.jdbc.replay.driver.TxnReplayableStatement)
				{
					oracle.jdbc.internal.OraclePreparedStatement preparedStatement = unwrapedStatement.unwrap(oracle.jdbc.internal.OraclePreparedStatement.class);

					// original SQL is much easier to get, but bind variables differ ? => :1
					ParameterMetaData m = preparedStatement.getParameterMetaData();
					String originalSql = preparedStatement.getOriginalSql();								

					/* oracle.jdbc.driver.OraclePreparedStatementWrapper - internal in-accesible class */
					Object statementWrapper = ((oracle.jdbc.replay.driver.TxnReplayableStatement) unwrapedStatement).getDelegateObj();
					
					// get field named preparedStatement
					Field field = statementWrapper.getClass().getDeclaredField("preparedStatement");
					// Allow modification on the field
					field.setAccessible(true);
					// Sets the field to the new value for this instance
					Object st = field.get(statementWrapper);
					System.out.println(st);

					// Iterate to super-super class to acces field name sqlObject of type OracleSQL
					//			    	System.out.println("----------------");
					//			    	Field f1[] = st.getClass().getDeclaredFields();
					//			    	for (Field ff : f1) {
					//			    		System.out.println(ff.getType().toGenericString() + " " + ff.getName());
					//			    	}
					//			    	System.out.println("----------------");
					//			    	Field f2[] = st.getClass().getSuperclass().getDeclaredFields();
					//			    	for (Field ff : f2) {
					//			    		System.out.println(ff.getType().toGenericString() + " " + ff.getName());
					//			    	}
					//			    	System.out.println("----------------");
					//			    	Field f3[] = st.getClass().getSuperclass().getSuperclass().getDeclaredFields();
					//			    	for (Field ff : f3) {
					//			    		System.out.println(ff.getType().toGenericString() + " " + ff.getName());
					//			    	}
					//			    	System.out.println("----------------");
					// T4CPreparedStatement => OraclePreparedStatement => OracleStatement
					Field field2 = st.getClass().getSuperclass().getSuperclass().getDeclaredField("sqlObject");
					// Allow modification on the field
					field2.setAccessible(true);
					// Sets the field to the new value for this instance
					Object sqlObject = field2.get(st);
					System.out.println(sqlObject);			    	
					OracleSql st3 = (OracleSql)sqlObject;

					// finally OracleSQL.actualSql contains actual SQL beeing sent into database
					Field field3 = st3.getClass().getDeclaredField("actualSql");
					// Allow modification on the field
					field3.setAccessible(true);
					// Sets the field to the new value for this instance
					Object actualSql = field3.get(st3);
					return actualSql.toString();
				}
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return UNKNOWN;
	}
	
    /**
     * Compute sqlid for a statement, the same way as Oracle does
     * http://www.slaviks-blog.com/2010/03/30/oracle-sql_id-and-hash-value/
     * https://blog.tanelpoder.com/2009/02/22/sql_id-is-just-a-fancy-representation-of-hash-value/
     * @param stmt - SQL string without trailing 0x00 Byte
     * @return sql_id as computed by Oracle
     */ 	
	private static String SQL_ID(String stmt)
	{
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
	
}
