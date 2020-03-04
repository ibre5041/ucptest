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
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.transaction.Transaction;

/***************
 * 
 * Sample standalone application UCP + JPA
 * 
 * run as:
 * mvn exec:exec
 * 
 * use -Djava.util.logging.config.file=logging.properties
 */
public class AppJPA 
{	
	// UCP XML config file location URI
	private static File ucpConfig = new File(AppJPA.class.getClassLoader().getResource("ucp.xml").getFile());
	private static File l4jConfig = new File(AppJPA.class.getClassLoader().getResource("logging.properties").getFile());
	private static final String ucpConfigURI = ucpConfig.toURI().toString();
	
    public static void main( String[] args ) throws Exception
    {
		//Override system DNS setting
		System.setProperty("sun.net.spi.nameservice.nameservers", "192.168.8.200");
		System.setProperty("sun.net.spi.nameservice.provider.1", "dns,sun");
    	
    	System.setProperty("oracle.ucp.jdbc.xmlConfigFile", ucpConfigURI);
    	
    	Logger.getLogger("oracle.ucp").setLevel(Level.FINEST);
    	Logger.getLogger("oracle.ucp.jdbc.PoolDataSource").setLevel(Level.FINEST);
    	    	
    	EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("Book");
    	EntityManager entityManager = entityManagerFactory.createEntityManager();
        
    	EntityTransaction tx = entityManager.getTransaction();
    	tx.begin();
        
        for(int i = 0; i < 100000; i++)
        {
        	BigDecimal randomId = new BigDecimal(i + /*Math.random() * */ 1000000000 + 1000000000).setScale(0, RoundingMode.FLOOR);        		
        	BookEntity book = new BookEntity();
        	book.setId(randomId);
        	book.setName(randomAlphaNumeric(20));
        	book.setAuthor("Hello world task author");
        	System.out.println("Inserting:("+i+")" + book.toString());
        	entityManager.merge(book);
        	entityManager.flush();
        }
        tx.commit();
    }
    
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static String randomAlphaNumeric(int count) {
    	StringBuilder builder = new StringBuilder();
    	while (count-- != 0) {
    		int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
    		builder.append(ALPHA_NUMERIC_STRING.charAt(character));
    	}
    	return builder.toString();
    }
    
}
