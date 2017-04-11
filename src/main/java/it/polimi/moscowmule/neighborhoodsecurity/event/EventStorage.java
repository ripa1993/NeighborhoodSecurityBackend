package it.polimi.moscowmule.neighborhoodsecurity.event;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import it.polimi.moscowmule.neighborhoodsecurity.utilities.database.Database;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.EventDBException;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.NoEventCreatedException;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.NoEventFoundException;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.NoVoteCreatedException;
import it.polimi.moscowmule.neighborhoodsecurity.utilities.exceptions.VotesDBException;

public enum EventStorage {
	instance;

	/**
	 * Add event to the storage
	 * 
	 * @param e
	 *            event to be added
	 * @return id of the added event
	 * @throws NoEventCreatedException
	 * @throws EventDBException
	 */
	public int add(Event e) throws EventDBException, NoEventCreatedException {
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

			if (id == -1) {
				throw new NoEventCreatedException();
			}

			connection.close();
			return id;
		} catch (SQLException | URISyntaxException | ClassNotFoundException e1) {
			throw new EventDBException("ERROR when creating event", e1);
		}
	}

	/**
	 * Remove event from the storage
	 * 
	 * @param id
	 *            event to be removed
	 * @return true if event is removed, otherwise false
	 * @throws EventDBException
	 */
	public boolean remove(int id) throws EventDBException {

		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement deleteStmt = connection.prepareStatement(
					"DELETE FROM gsx95369n3oh2zo6.events WHERE ID = ?", ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			deleteStmt.clearParameters();
			deleteStmt.setInt(1, id);
			int count = deleteStmt.executeUpdate();
			connection.close();
			if (count > 0) {
				return true;
			} else {
				return false;
			}
		} catch (URISyntaxException | SQLException | ClassNotFoundException e) {
			throw new EventDBException("ERROR in deleting event by id", e);
		}
	}

	/**
	 * Find event based on latitude and longitude
	 * 
	 * @param latitudeMin
	 *            of the event
	 * @param latitudeMax
	 *            of the event
	 * @param longiduteMin
	 *            of the event
	 * @param longiduteMax
	 *            of the event
	 * @return a list of events in the selected area
	 * @throws EventDBException
	 */
	public List<Event> getByArea(Float latitudeMin, Float latitudeMax, Float longitudeMin, Float longitudeMax)
			throws EventDBException {
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
				
				try {
					int votes = getVotes(temp.getId());
					temp.setVotes(votes);
				} catch (VotesDBException e) {
					temp.setVotes(0);
				}
				
				events.add(temp);
			}
			connection.close();
			return events;
		} catch (URISyntaxException | SQLException | ClassNotFoundException e) {
			throw new EventDBException("ERROR when finding events by coordinates", e);
		}
	}

	/**
	 * Find events based on radius starting from a position
	 * 
	 * @param latitude
	 *            of the center
	 * @param longitude
	 *            of the center
	 * @param radius
	 *            where to search
	 * @return a list of events in the selected area
	 * @throws EventDBException
	 */
	public List<Event> getByRadius(Float latitude, Float longitude, Float radius) throws EventDBException {
		return getByArea(latitude - radius, latitude + radius, longitude - radius, longitude + radius);
	}

	/**
	 * Find an event given its id
	 * 
	 * @param id
	 *            id of the event
	 * @return the found event if it exists, or null
	 * @throws EventDBException
	 * @throws NoEventFoundException
	 */
	public Event getById(int id) throws NoEventFoundException, EventDBException {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement(
					"SELECT * FROM gsx95369n3oh2zo6.events WHERE ID = ?", ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setInt(1, id);
			ResultSet result = getStmt.executeQuery();
			Event temp = null;
			if (result.next()) {
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
				
				try {
					int votes = getVotes(temp.getId());
					temp.setVotes(votes);
				} catch (VotesDBException e) {
					temp.setVotes(0);
				}
				
				connection.close();
				return temp;
			} else {
				connection.close();
				throw new NoEventFoundException();
			}

		} catch (URISyntaxException | SQLException | ClassNotFoundException e) {
			throw new EventDBException("ERROR in finding event by id", e);
		}
	}

	public int getSubmitter(int id) throws EventDBException, NoEventFoundException {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement(
					"SELECT SUBMITTERID FROM gsx95369n3oh2zo6.events WHERE ID = ?", ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setInt(1, id);
			ResultSet result = getStmt.executeQuery();
			if (result.next()) {
				return result.getInt(1);
			}
		} catch (URISyntaxException | SQLException | ClassNotFoundException e) {
			throw new EventDBException("ERROR when finding submitter by event id", e);
		}
		throw new NoEventFoundException();
	}

	public List<Event> getByUser(int id) throws EventDBException {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement(
					"SELECT * FROM gsx95369n3oh2zo6.events WHERE SUBMITTERID = ?", ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setInt(1, id);
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
			return events;
		} catch (ClassNotFoundException | URISyntaxException | SQLException e) {
			throw new EventDBException("ERROR when finding events by submitter id", e);
		}
	}

	public boolean vote(int userid, int eventid) throws VotesDBException, NoVoteCreatedException {
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement createStmt = connection.prepareStatement("INSERT INTO gsx95369n3oh2zo6.votes (USERID, EVENTID) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
			createStmt.clearParameters();
			createStmt.setInt(1, userid);
			createStmt.setInt(2, eventid);
			createStmt.executeUpdate();
			ResultSet results = createStmt.getGeneratedKeys();
			if(results.next()){
				return true;
			} else {
				throw new NoVoteCreatedException();
			}
		} catch (ClassNotFoundException | URISyntaxException | SQLException e) {
			throw new VotesDBException("ERROR when creating vote", e);
		}
		
		
	}
	
	public boolean unvote(int userid, int eventid) throws VotesDBException{
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement delStmt = connection.prepareStatement("DELETE FROM gsx95369n3oh2zo6.votes WHERE USERID = ? AND EVENTID = ?", ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			delStmt.clearParameters();
			delStmt.setInt(1, userid);
			delStmt.setInt(2, eventid);
			int count = delStmt.executeUpdate();
			connection.close();
			if (count > 0){
				return true;
			} else {
				return false;
			}
		} catch (ClassNotFoundException | URISyntaxException | SQLException e) {
			throw new VotesDBException("ERROR when deleting vote", e);
		}
		
	}
	
	public int getVotes(int eventId) throws VotesDBException{
		Connection connection;
		try {
			connection = Database.getConnection();
			PreparedStatement getStmt = connection.prepareStatement("SELECT COUNT(*) FROM gsx95369n3oh2zo6.votes WHERE EVENTID = ?", ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			getStmt.clearParameters();
			getStmt.setInt(1, eventId);
			ResultSet results = getStmt.executeQuery();
			connection.close();
			if(results.next()){
				return results.getInt(1);
			} else {
				return 0;
			}
		} catch (ClassNotFoundException | URISyntaxException | SQLException e) {
			throw new VotesDBException("EROR when finding vote",e);
		}
	}
}
