package sandbox.rest;


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.jackson.JacksonFeature;

public class BookClientMT extends Thread
{
    static String URL = loadProperties().getProperty("url");
    
    public static void main( String[] args )
    {
        for (int i=0; i<400; i++)
        {
        	BookClientMT object = new BookClientMT();
            object.start();
        }        
    }
    	
    public void run()
    {
    	Client client = ClientBuilder.newBuilder()
    			.register(JacksonFeature.class)
    			.build();

    	//WS text GET - Accept:text/plain
    	WebTarget target = client.target(URL).path("books");
    	String res = target.request().get().readEntity(String.class);
    	System.out.println("A "+ res);

    	//WS JSON GET 
    	WebTarget target2 = client.target(URL).path("books");
    	String res2 = target2.request(MediaType.APPLICATION_JSON).get().readEntity(String.class);
    	System.out.println("B "+ res2);

    	//WS sending and receiving a String by POST
    	{
    		String req = "{\"name\": \"New Book\", \"author\": \"Unknown 1\"}";// a valid json
    		WebTarget target3 = client.target(URL).path("books");
    		String res3 = target3.request().post(Entity.entity(req, MediaType.APPLICATION_JSON), String.class);      
    		System.out.println("C "+ res3);
    	}

    	//WS text getsending and receiving objects POST
    	{
    		BookObj requestObj = new BookObj();
    		requestObj.setAuthor("Rammstein");
    		requestObj.setName("Stripped");

    		System.out.println("D Post: " + requestObj.toString());
    		WebTarget target4 = client.target(URL).path("books");
    		BookObj responseObj = target4.request(MediaType.APPLICATION_JSON).post(Entity.entity(requestObj, MediaType.APPLICATION_JSON)).readEntity(BookObj.class);      
    		System.out.println("D Resp: " + responseObj.toString());
    	}
    }
    
	public static Properties loadProperties() {
		Properties props = new Properties();
		try {
			InputStream is = BookClientMT.class.getResourceAsStream("/books.properties");
			if (is != null) {
				props.load(is);
				is.close();
			}
		}
		catch(IOException ioe) {
			System.err.println("IOException in loadProps");
			for(StackTraceElement ste : ioe.getStackTrace())
				System.err.println(ste.toString());
			System.exit(-1);
		}
		return props;
	}
}
