import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/***************
 * 
 * Sample standalone application UCP + Hibernate
 * 
 * run as:
 * mvn exec:exec
 * 
 * use -Djava.util.logging.config.file=logging.properties
 */
public class AppHibernate 
{	
	// UCP XML config file location URI
	private static File ucpConfig = new File(AppHibernate.class.getClassLoader().getResource("ucp.xml").getFile());
	private static File l4jConfig = new File(AppHibernate.class.getClassLoader().getResource("logging.properties").getFile());
	private static final String ucpConfigURI = ucpConfig.toURI().toString();
	
    public static void main( String[] args ) throws Exception
    {
    	System.setProperty("oracle.ucp.jdbc.xmlConfigFile", ucpConfigURI);
    	
    	Logger.getLogger("oracle.ucp").setLevel(Level.FINEST);
    	Logger.getLogger("oracle.ucp.jdbc.PoolDataSource").setLevel(Level.FINEST);
    	
    	//System.setProperty("java.util.logging.config.file", l4jConfig.getAbsolutePath());
//    	Properties preferences = new Properties();
//    	try {
//    	    FileInputStream configFile = new FileInputStream(l4jConfig.getAbsoluteFile());
//    	    preferences.load(configFile);
//    	    LogManager.getLogManager().readConfiguration(configFile);
//    	    System.out.println("Logging props loaded: " + l4jConfig.getAbsolutePath());
//    	} catch (IOException ex)
//    	{
//    	    System.out.println("WARNING: Could not open configuration file: " + l4jConfig.getAbsolutePath());
//    	    System.out.println("WARNING: Logging not configured (console output only)");
//    	}
    	    
    	SessionFactory sessionFactory = new Configuration()        	
                .configure() // configures settings from hibernate.cfg.xml
                .addAnnotatedClass(BookEntity.class)
                .buildSessionFactory();

        Session session = sessionFactory.openSession();
        
        Transaction tx = session.beginTransaction();        
        
        for(int i = 0; i < 100000; i++)
        {
        	BigDecimal randomId = new BigDecimal(i + /*Math.random() * */ 1000000000 + 1000000000).setScale(0, RoundingMode.FLOOR);
        	
        	BookEntity book = session.get(BookEntity.class, randomId);
        	if (book == null)
        	{
        		book = new BookEntity();
            	book.setId(randomId);
        	}
        	book.setName("Hello world task");
        	book.setAuthor("Hello world task author");        		
        	System.out.println("Inserting:("+i+")" + book.toString());
        	session.persist(book);
        }
        tx.commit();
        session.close();
    }
}
