import java.io.File;
import java.math.BigDecimal;

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
 */
public class App 
{	
	// UCP XML config file location URI
	private static File ucpConfig = new File(App.class.getClassLoader().getResource("ucp.xml").getFile());
	private static final String ucpConfigURI = ucpConfig.toURI().toString();
	
    public static void main( String[] args ) throws Exception
    {
    	System.setProperty("oracle.ucp.jdbc.xmlConfigFile", ucpConfigURI);

    	SessionFactory sessionFactory;
        sessionFactory = new Configuration()
                .configure() // configures settings from hibernate.cfg.xml
                .addAnnotatedClass(BookEntity.class)
                .buildSessionFactory();

        Session session = sessionFactory.openSession();

        Transaction tx = session.beginTransaction();
        BookEntity book = new BookEntity();
        book.setId(new BigDecimal(1));
        book.setName("Hello world task");
        book.setAuthor("Hello world task author");
        session.save(book);
        tx.commit();
        session.close();
    }
}
