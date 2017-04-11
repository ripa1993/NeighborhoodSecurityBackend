package it.polimi.moscowmule.neighborhoodsecurity.authentication;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import it.polimi.moscowmule.neighborhoodsecurity.utilities.ProjectConstants;

/**
 * Response filter
 * <ul>
 * <li>Add header for 'service_key' and 'auth_token'</li>
 * <li>Other generic headers</li>
 * </ul>
 * 
 * @author Simone Ripamonti
 *
 */
@Provider
@PreMatching
public class ResponseFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
		// You may further limit certain client IPs with
		// Access-Control-Allow-Origin instead of '*'
		responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
		responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
		responseContext.getHeaders().add("Access-Control-Allow-Headers",
				ProjectConstants.SERVICE_KEY + ", " + ProjectConstants.AUTH_TOKEN);
	}

}
