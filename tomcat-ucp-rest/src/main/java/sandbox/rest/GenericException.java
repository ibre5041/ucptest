package sandbox.rest;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sandbox.pojo.ResponseObj;

@Path("/")
/**
 * 
 * Generic Exception error is parsed into a JSON
 * 
 * @author juan
 *
 */
public class GenericException implements javax.ws.rs.ext.ExceptionMapper<Exception> {
	@Override
	public Response toResponse(Exception error) {
	    Response response;
	    if (error instanceof WebApplicationException) {
	        WebApplicationException webEx = (WebApplicationException) error;
	        response = webEx.getResponse();
	        webEx.getResponse().getStatus();
    		ResponseObj rt = new ResponseObj();	
			rt.setResult("ERROR");
			rt.setError(webEx.getResponse().getStatus() +" " + error.getMessage());	
			ObjectMapper mapper = new ObjectMapper();
			String json = "";
			try {
				json = mapper.writeValueAsString(rt);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return Response.status(webEx.getResponse().getStatus())
					.entity(json)
					.type(MediaType.APPLICATION_JSON)
					.build();
	    }
	    response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	                .entity("Internal error").type("text/plain").build();
	    return response;
	}
}
