package it.polimi.moscowmule.neighborhoodsecurity.event;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import it.polimi.moscowmule.neighborhoodsecurity.authentication.Authenticator;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.ProjectConstants;

@Path("/events")
public class EventsResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	/**
	 * Lists events, two ways are provided to filter events Uses
	 * latMin+latMax+lonMin+lonMax XOR lat+lon+radius
	 * 
	 * For a rectangle area search
	 * 
	 * @param latMin
	 * @param latMax
	 * @param lonMin
	 * @param lonMax
	 * 
	 *            For a search based on center and radius
	 * @param lat
	 * @param lon
	 * @param radius
	 * @return list of events matching the parameters (if any), or BAD_REQUEST
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response listEvents(@DefaultValue("") @QueryParam("latMin") String latMin,
			@DefaultValue("") @QueryParam("latMax") String latMax,
			@DefaultValue("") @QueryParam("lonMin") String lonMin,
			@DefaultValue("") @QueryParam("lonMax") String lonMax, @DefaultValue("") @QueryParam("lat") String lat,
			@DefaultValue("") @QueryParam("lon") String lon, @DefaultValue("") @QueryParam("rad") String rad) {
		if (NumberUtils.isNumber(latMin) && NumberUtils.isNumber(latMax) && NumberUtils.isNumber(lonMin)
				&& NumberUtils.isNumber(lonMax)) {
			Float latitudeMin, latitudeMax, longitudeMin, longitudeMax;
			latitudeMin = NumberUtils.toFloat(latMin);
			latitudeMax = NumberUtils.toFloat(latMax);
			longitudeMin = NumberUtils.toFloat(lonMin);
			longitudeMax = NumberUtils.toFloat(lonMax);

			return Response.ok(EventStorage.instance.getByArea(latitudeMin, latitudeMax, longitudeMin, longitudeMax))
					.build();
		}
		if (NumberUtils.isNumber(lat) && NumberUtils.isNumber(lon) && NumberUtils.isNumber(rad)) {
			Float latitude, longitude, radius;
			latitude = NumberUtils.toFloat(lat);
			longitude = NumberUtils.toFloat(lon);
			radius = NumberUtils.toFloat(rad);

			return Response.ok(EventStorage.instance.getByRadius(latitude, longitude, radius)).build();
		}
		return Response.status(Status.BAD_REQUEST).entity("Please check the parameters!").build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response createEvent(@FormParam("eventType") String eventType, @FormParam("description") String description,
			@FormParam("country") String country, @FormParam("city") String city, @FormParam("street") String street,
			@FormParam("latitude") String latitude, @FormParam("longitude") String longitude, @HeaderParam("auth_token") String authToken) {

		int userId = Authenticator.getUserId(authToken);
		
		if(userId < 0){
			return Response.status(Status.UNAUTHORIZED).entity("Your auth token is not valid!").build();
		}
		
		EventType et = EventType.valueOf(eventType);

		if (NumberUtils.isNumber(latitude) && NumberUtils.isNumber(longitude)) {
			float lat = NumberUtils.toFloat(latitude);
			float lon = NumberUtils.toFloat(longitude);

			// get country, city, street
			String[] address = getAddress(lat, lon);

			Event e = new Event();
			e.setCountry(address[0]);
			e.setCity(address[1]);
			e.setStreet(address[2]);
			e.setEventType(et);
			e.setLatitude(lat);
			e.setLongitude(lon);
			e.setSubmitterId(userId);

			try {
				int id = EventStorage.instance.add(e);
				return Response.created(URI.create(ProjectConstants.EVENTS_BASE_URL + "/" + String.valueOf(id)))
						.build();
			} catch (MalformedEvent e1) {
				return Response.status(Status.BAD_REQUEST).entity("Something went wrong").build();
			}

		} else {
			float[] coordinates = getCoordinates(country, city, street);

			if (coordinates == null) {
				// cannot find coordinates, aborting
				return Response.status(Status.BAD_REQUEST)
						.entity("Please provide valid city-street-address or coordinates").build();
			}

			float lat = coordinates[0];
			float lon = coordinates[1];

			// valid coordinates found, save the new event
			Event e = new Event();
			e.setCountry(country);
			e.setCity(city);
			e.setStreet(street);
			e.setEventType(et);
			e.setLatitude(lat);
			e.setLongitude(lon);
			e.setSubmitterId(userId);

			try {
				int id = EventStorage.instance.add(e);
				return Response.created(URI.create(ProjectConstants.EVENTS_BASE_URL + "/" + String.valueOf(id)))
						.build();
			} catch (MalformedEvent e1) {
				return Response.status(Status.BAD_REQUEST).entity("Something went wrong").build();
			}
		}
	}

	/**
	 * Returns an event based on the provided id
	 * 
	 * @param id
	 *            of the event
	 * @return the requested event, or NOT_FOUND or BAD_REQUEST
	 */
	@Path("{id}")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response getEventById(@PathParam("id") String id) {
		if (NumberUtils.isNumber(id)) {
			Event e = EventStorage.instance.getById(NumberUtils.toInt(id));
			if (e == null) {
				return Response.status(Status.NOT_FOUND).entity("No event with id " + id + " has been found").build();
			}
			return Response.ok(e).build();
		}
		return Response.status(Status.BAD_REQUEST).entity("Id must be a valid positive integer!").build();

	}
	
	/**
	 * Removes the event with the specified id
	 * @param id of the event
	 * @return NO_CONTENT if delete was successfull, NOT_FOUND if there is no event with that id, BAD_REQUEST if id is not valid
	 */
	@DELETE
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response deleteEvent(@PathParam("id") String id, @HeaderParam("auth_token") String authToken) {
		if (NumberUtils.isNumber(id)) {
			
			
			int requestingUser = Authenticator.getUserId(authToken);
			boolean superuser = Authenticator.isSuperuser(requestingUser);
			int ownerUser = EventStorage.instance.getSubmitter(NumberUtils.toInt(id));
			
			if(requestingUser != ownerUser || !superuser){
				return Response.status(Status.UNAUTHORIZED).entity("You are not the owner of event "+id).build();
			}
			
			boolean result = EventStorage.instance.remove(NumberUtils.toInt(id));
			if(result){
				return Response.status(Status.NO_CONTENT).build();
			} else {
				return Response.status(Status.NOT_FOUND).entity("No event with id "+id).build();
			}
		}
		return Response.status(Status.BAD_REQUEST).entity("Id must be a valid positive integer!").build();
	}
	
	/**
	 * Find coordinates given country, city and street
	 * @param country
	 * @param city
	 * @param street
	 * @return [latitude, longitude] if found, otherwise null
	 */
	private float[] getCoordinates(String country, String city, String street) {
		String union = country + ", " + city + ", " + street;
		URL url;
		try {
			url = new URL("http://maps.googleapis.com/maps/api/geocode/json?address=" + URIUtil.encodeQuery(union)
					+ "&sensor=true");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				return null;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output = "", json = "";
			while ((output = br.readLine()) != null) {
				json += output;
			}

			JSONObject obj = new JSONObject(json);
			String latString = (String) obj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry")
					.getJSONObject("location").get("latitude");
			String lonString = (String) obj.getJSONArray("results").getJSONObject(0).getJSONObject("geometry")
					.getJSONObject("location").get("longitude");

			if (NumberUtils.isNumber(latString) && NumberUtils.isNumber(lonString)) {
				float lat = NumberUtils.toFloat(latString);
				float lon = NumberUtils.toFloat(lonString);
				float[] result = { lat, lon };
				return result;
			}

		} catch (IOException e) {
			return null;
		}

		return null;
	}

	/**
	 * Find country, city, address given the coordinates
	 * @param latitude
	 * @param longitude
	 * @return [country, city, address] if applicable
	 */
	private String[] getAddress(float latitude, float longitude) {
		String union = String.valueOf(latitude) + "," + String.valueOf(longitude);
		URL url;
		try {
			url = new URL("http://maps.googleapis.com/maps/api/geocode/json?address=" + URIUtil.encodeQuery(union)
					+ "&sensor=true");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				return null;
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
			String output = "", json = "";
			while ((output = br.readLine()) != null) {
				json += output;
			}

			JSONObject obj = new JSONObject(json);
			String streetNumber = "";
			String[] result = new String[3];

			JSONArray addressComponents = obj.getJSONArray("results").getJSONObject(0)
					.getJSONArray("address_components");
			for (int i = 0; i < addressComponents.length(); i++) {
				String types = (String) addressComponents.getJSONObject(i).getJSONArray("types").getString(0);

				if (types.equals("street_number")) {
					streetNumber = (String) addressComponents.getJSONObject(i).getString("long_name");
				} else if (types.equals("route")) {
					result[2] = (String) addressComponents.getJSONObject(i).getString("long_name");
				} else if (types.equals("administrative_area_level_3")) {
					result[1] = (String) addressComponents.getJSONObject(i).getString("long_name");
				} else if (types.equals("country")) {
					result[0] = (String) addressComponents.getJSONObject(i).getString("long_name");
				}
			}

			if (!streetNumber.equals("")) {
				result[2] = result[2] + ", " + streetNumber;
			}

			return result;

		} catch (IOException e) {
			return null;
		}
	}
}
