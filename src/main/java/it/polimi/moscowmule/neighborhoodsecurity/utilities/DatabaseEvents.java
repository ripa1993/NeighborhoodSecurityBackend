package it.polimi.moscowmule.neighborhoodsecurity.utilities;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import it.polimi.moscowmule.neighborhoodsecurity.event.Event;
import it.polimi.moscowmule.neighborhoodsecurity.event.EventType;

public class DatabaseEvents {


	/**
	 * Insert the given events in the database
	 * 
	 * @param e
	 *            the event to be inserted
	 * @return the id of the inserted event, -1 if something went wrong
	 */
	public static int createEvent(Event e) {
		Connection connection;

		try {
			connection = Database.getConnection();
			PreparedStatement createStmt = connection.prepareStatement(
					"INSERT INTO gsx95369n3oh2zo6.events (DATE, EVENTTYPE, DESCRIPTION,"
							+ "COUNTRY, CITY, STREET, LATITUDE, LONGITUDE, SUBMITTERID) VALUES (?,?,?,?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			createStmt.setDate(1, (Date) e.getDate());
			createStmt.setString(2, e.getEventType().toString());
			createStmt.setString(3, e.getDescription());
			createStmt.setString(4, e.getCountry());
			createStmt.setString(5, e.getCity());
			createStmt.setString(6, e.getStreet());
			createStmt.setFloat(7, e.getLatitude());
			createStmt.setFloat(8, e.getLongitude());
			createStmt.setInt(9, e.getSubmitterId());
			createStmt.executeUpdate();
			ResultSet results = createStmt.getGeneratedKeys();
			int id = -1;
			if (results.next()) {
				id = results.getInt(1);
			}

			connection.close();
			return id;
		} catch (SQLException | URISyntaxException e1) {
			return -1;
		}

	}

	/**
	 * Deletes an event stored in the database given its id
	 * 
	 * @param id
	 *            the id of the event
	 * @return true if delete was successfull, false otherwise
	 */
	public static boolean deleteEvent(int id) {

		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement deleteStmt = connection.prepareStatement("DELETE FROM gsx95369n3oh2zo6.events WHERE ID == ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			deleteStmt.clearParameters();
			deleteStmt.setInt(1, id);
			deleteStmt.executeUpdate();
			connection.close();
		} catch (URISyntaxException | SQLException e) {
			return false;
		}
		return true;
	}

	/**
	 * Retrieves a list of events from the database given an area to search
	 * 
	 * @param latitudeMin
	 *            minimum latitude
	 * @param latitudeMax
	 *            maximum latitude
	 * @param longitudeMin
	 *            minimum longitude
	 * @param longitudeMax
	 *            maximum longitude
	 * @return a list of events, it can be empty
	 */
	public static List<Event> getEvents(float latitudeMin, float latitudeMax, float longitudeMin, float longitudeMax) {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement(
					"SELECT * FROM gsx95369n3oh2zo6.events WHERE LATITUDE < ? AND LATITUDE > ? AND LONGITUDE < ? AND LONGITUDE > ?",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setFloat(1, latitudeMax);
			getStmt.setFloat(2, latitudeMin);
			getStmt.setFloat(3, longitudeMax);
			getStmt.setFloat(4, longitudeMin);
			ResultSet result = getStmt.executeQuery();

			List<Event> events = new ArrayList<Event>();

			while (result.next()) {
				Event temp = new Event();
				temp.setId(result.getInt(1));
				temp.setDate(result.getDate(2));
				temp.setEventType(EventType.valueOf(result.getString(3)));
				temp.setDescription(result.getString(4));
				temp.setCountry(result.getString(5));
				temp.setCity(result.getString(6));
				temp.setStreet(result.getString(7));
				temp.setLatitude(result.getFloat(8));
				temp.setLongitude(result.getFloat(9));
				temp.setSubmitterId(result.getInt(10));
				events.add(temp);
			}
			connection.close();
			return events;
		} catch (URISyntaxException | SQLException e) {
			return new ArrayList<Event>();
		}

	}

	/**
	 * Retrieves an event given its id
	 * @param id int id of the event
	 * @return the found event, otherwise null
	 */
	public static Event getById(int id) {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement("SELECT * FROM gsx95369n3oh2zo6.events WHERE ID == ?",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setInt(1, id);
			ResultSet result = getStmt.executeQuery();
			Event temp = null;
			if(result.next()){
				temp = new Event();
				temp.setId(result.getInt(1));
				temp.setDate(result.getDate(2));
				temp.setEventType(EventType.valueOf(result.getString(3)));
				temp.setDescription(result.getString(4));
				temp.setCountry(result.getString(5));
				temp.setCity(result.getString(6));
				temp.setStreet(result.getString(7));
				temp.setLatitude(result.getFloat(8));
				temp.setLongitude(result.getFloat(9));
				temp.setSubmitterId(result.getInt(10));
			}
			connection.close();
			return temp;
		} catch (URISyntaxException | SQLException e) {
			return null;
		}
	}

	/**
	 * Finds the submitter of an event
	 * @param id of the event
	 * @return the submitter if the event exists, else null
	 */
	public static Integer getSubmitterId(int id) {
		Connection connection;
		try{
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement("SELECT SUBMITTERID FROM gsx95369n3oh2zo6.events WHERE ID == ?",
					ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setInt(1, id);
			ResultSet result = getStmt.executeQuery();
			if (result.next()){
				return result.getInt(1);
			}
		} catch (URISyntaxException | SQLException e) {
			return null;
		}
		return null;
	}
}
