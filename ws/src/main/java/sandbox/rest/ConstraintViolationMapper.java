package sandbox.rest;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sandbox.pojo.ResponseObj;

/**
 * Generic mapper for validations
 *
 * @author jcgonzalez.com
 *
 */
@Provider
public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException ex) {
        String msg = " Not valid request  ";
        Set<ConstraintViolation<?>> set = ex.getConstraintViolations();
        for (ConstraintViolation<?> cv : set) {
            msg += "--" + cv.getPropertyPath() + " " + cv.getMessage() + "--";
        }
        ResponseObj rt = new ResponseObj();
        rt.setResult("ERROR");
        rt.setError(Response.Status.BAD_REQUEST.getStatusCode() + msg);
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            json = mapper.writeValueAsString(rt);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(json).type(MediaType.APPLICATION_JSON).build();
    }
}
