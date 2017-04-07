package it.polimi.moscowmule.neighborhoodsecurity.user;

import java.net.URI;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.validator.routines.EmailValidator;

import it.polimi.moscowmule.neighborhoodsecurity.utilities.ProjectConstants;

@Path("/users")
public class UsersResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response createUser(@FormParam("username") String username, @FormParam("email") String email,
			@FormParam("password") String password) {
		
		if(!EmailValidator.getInstance().isValid(email)){
			return Response.status(Status.BAD_REQUEST).entity("Email is not valid").build();
		}
		// 8 characters, at least 1 lowercase, 1 uppercase, 1 number, no spaces
		String pattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";
		if(!password.matches(pattern)){
			return Response.status(Status.BAD_REQUEST).entity("Password must be 8 characters long, containing at least one uppercase, one lowercase and one number").build();
		}
		// 4-20 characters, no special symbols and spaces
		pattern = "^(?=.*[A-Za-z0-9])(?=\\S+$).{4,20}$";
		if (!username.matches(pattern)){
			return Response.status(Status.BAD_REQUEST).entity("Username must be between 4 and 20 characters, not containing spaces or special symbols").build();
		}
		User u = new User();
		u.setCreated(new Date());
		u.setEmail(email);
		u.setUsername(username);
		int id = UserStorage.instance.addWithPassword(u, password);
		
		if(id > 0){
			return Response.created(URI.create(ProjectConstants.USERS_BASE_URL+"/"+String.valueOf(id))).build();
		} else {
			return Response.status(Status.BAD_REQUEST).entity("Username or email already in use").build();
		}
		
		
	}

	@Path("{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getUser(@PathParam("id") String id) {
		if (NumberUtils.isNumber(id)) {
			User u = UserStorage.instance.getById(NumberUtils.toInt(id));
			if (u == null) {
				return Response.status(Status.NOT_FOUND).entity("No user with id " + id + " has been found").build();
			}
			return Response.ok(u).build();
		}
		return Response.status(Status.BAD_REQUEST).entity("Id must be a valid positive integer!").build();
	}

}
