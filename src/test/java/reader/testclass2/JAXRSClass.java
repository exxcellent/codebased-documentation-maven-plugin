package reader.testclass2;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/test")
public class JAXRSClass {
	
	
	public Response noMsg( String msg) {
		 
		String output = "Jersey say : " + msg;
 
		return Response.status(200).entity(output).build();
 
	}
	
	@Path("/all")
	@POST
	public Response allMsg(@PathParam("param") String msg) {
		 
		String output = "Jersey say : " + msg;
 
		return Response.status(200).entity(output).build();
 
	}
 
}