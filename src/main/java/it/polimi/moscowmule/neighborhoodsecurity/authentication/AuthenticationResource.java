package it.polimi.moscowmule.neighborhoodsecurity.authentication;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import it.polimi.moscowmule.neighborhoodsecurity.utilities.Message;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.ProjectConstants;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.AuthorizationDBException;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.NoUserFoundException;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.SecretDBException;

public class AuthenticationResource {

	@POST
	@Path("/login/classic")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response loginClassic(@FormParam("username") String username, @FormParam("password") String password ) {
		try {
			int userId = Authenticator.checkPassword(username, password);
			String authToken = Authenticator.generateToken(userId);
			AuthToken toBeReturned = new AuthToken();
			toBeReturned.setAuthToken(authToken);
			toBeReturned.setUserId(userId);
			toBeReturned.setUsername(username);
			return Response.ok(toBeReturned).build();
		} catch (NoUserFoundException e) {
			return Response.status(Status.UNAUTHORIZED).entity(new Message("AUTHORIZATION", "Login is incorrect")).build();
		} catch (SecretDBException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Message("DATABASE", e.getMessage())).build();
		} catch (AuthorizationDBException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Message("DATABASE", e.getMessage())).build();

		}
		
	}
	
	@POST
	@Path("/logout")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response logout(@HeaderParam(ProjectConstants.AUTH_TOKEN) String authToken){
		try {
			int userId = Authenticator.getUserId(authToken);
			Authenticator.invalidateToken(userId);
			return Response.ok(new Message("AUTHORIZATION", "Logged out, discard your token")).build();
		} catch (AuthorizationDBException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(new Message("DATABASE", e.getMessage())).build();
		} catch (NoUserFoundException e) {
			return Response.status(Status.UNAUTHORIZED).entity(new Message("AUTHORIZATION", "Your auth token is already invalid")).build();
		}
	}
	
}
