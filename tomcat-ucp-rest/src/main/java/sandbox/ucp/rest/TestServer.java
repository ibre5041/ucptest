package sandbox.ucp.rest;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sandbox.pojo.RequestObj;
import sandbox.pojo.ResponseObj;

/**
 *
 * Server tests
 *
 * @author juan
 *
 */
@Path("/test")
public class TestServer {
	/**
	 * This method return a hello World
	 *
	 * @return
	 */
	@GET
	public String inicio()
	{
		return "Hello World!";
	}
	/**
	 * This method return a hello World
	 *
	 * @return
	 */
	@GET
	@Path("/get")    
	public String hello()
	{
		return "Hello World !! - Jersey 2";
	}
	/**
	 *
	 * This method produces a JSON
	 *
	 * @return
	 */
	@GET
	@Path("/get")
	@Consumes(MediaType.APPLICATION_JSON)    
	@Produces(MediaType.APPLICATION_JSON)
	public RequestObj getTrackInJSON() {
		RequestObj requestObj = new RequestObj();
		requestObj.setTitle("Enter Sandman");
		requestObj.setSinger("Metallica");
		return requestObj;
	}
	/**
	 *
	 * This method uses the final url part as parameter
	 *
	 * @param id
	 * @param requestObj
	 * @return
	 */
	@POST
	@Path("/post-id/{id}")
	@Consumes(MediaType.APPLICATION_JSON)    
	@Produces(MediaType.APPLICATION_JSON)
	public ResponseObj getID(@PathParam("id") int id, RequestObj requestObj) {
		ResponseObj rt = new ResponseObj();    
		rt.setResult("OK");
		rt.setRetorno(Response.Status.OK.getStatusCode() +" OK ->" + id);        
		return rt;
	}
	/**
	 *
	 * Test for the PUT
	 *
	 * @param obj
	 * @return
	 */
	@PUT
	@Path("/put")
	@Consumes(MediaType.APPLICATION_JSON)    
	@Produces(MediaType.TEXT_HTML)
	public Response objectToLine(String obj) {
		return Response.status(201).entity("response" + obj).build();
	}
	/**
	 *
	 * Validated Test
	 *
	 * @param requestObj
	 * @return
	 */
	@POST
	@Path("/validate")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response validateField(@Valid RequestObj requestObj) {
		ResponseObj rt = new ResponseObj();
		ObjectMapper mapper = new ObjectMapper();
		String json = "";
		try {
			json = mapper.writeValueAsString(rt);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}        
		return Response.status(Response.Status.ACCEPTED).entity(json).build();
	}
}
