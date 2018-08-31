package sandbox.rest;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.jackson.JacksonFeature;

public class BookClient
{
    public static void main( String[] args )
    {
      
        Client client = ClientBuilder.newBuilder()
          .register(JacksonFeature.class)
          .build();
        
        //WS text GET - Accept:text/plain
        WebTarget target = client.target("http://localhost:8080/tomcat-dbcp-rest/").path("books");
        String res = target.request().get().readEntity(String.class);
        System.out.println("A "+ res);
        
        //WS JSON GET 
        WebTarget target2 = client.target("http://localhost:8080/tomcat-dbcp-rest/").path("books");
        String res2 = target2.request(MediaType.APPLICATION_JSON).get().readEntity(String.class);
        System.out.println("B "+ res2);
        
        //WS sending and receiving a String by POST
        {
        	String req = "{\"name\": \"New Book\", \"author\": \"Unknown 1\"}";// a valid json
        	WebTarget target3 = client.target("http://localhost:8080/tomcat-dbcp-rest/").path("books");
        	String res3 = target3.request().post(Entity.entity(req, MediaType.APPLICATION_JSON), String.class);      
        	System.out.println("C "+ res3);
        }
        
        //WS text getsending and receiving objects POST
        {
        	BookObj requestObj = new BookObj();
        	requestObj.setAuthor("Rammstein");
        	requestObj.setName("Stripped");

        	System.out.println("D Post: " + requestObj.toString());
        	WebTarget target4 = client.target("http://localhost:8080/tomcat-dbcp-rest/").path("books");
        	BookObj responseObj = target4.request(MediaType.APPLICATION_JSON).post(Entity.entity(requestObj, MediaType.APPLICATION_JSON)).readEntity(BookObj.class);      
        	System.out.println("D Resp: " + responseObj.toString());
        }
    }
}
