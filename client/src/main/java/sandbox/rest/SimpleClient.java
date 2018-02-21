package sandbox.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class SimpleClient{

	public static void main(String[] args) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:8080/ucptest-ws");
		WebTarget resourceWebTarget;
		resourceWebTarget = target.path("webapi/myresource");
		Invocation.Builder invocationBuilder;
		invocationBuilder = resourceWebTarget.request(MediaType.TEXT_PLAIN_TYPE);
		Response response = invocationBuilder.get();
		System.out.println(response.getStatus());
		System.out.println(response.readEntity(String.class));
	}
}
