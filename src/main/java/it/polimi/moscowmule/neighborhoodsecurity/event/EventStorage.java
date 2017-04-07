package it.polimi.moscowmule.neighborhoodsecurity.event;

import java.util.List;

import it.polimi.moscowmule.neighborhoodsecurity.utilities.DatabaseEvents;

public enum EventStorage {
	instance;
	
	/**Add event to the storage
	 * 
	 * @param e event to be added
	 * @return id of the added event
	 */
	public int add(Event e) throws MalformedEvent{
		return DatabaseEvents.createEvent(e);
	}
	
	/**Remove event from the storage
	 * 
	 * @param id event to be removed
	 * @return true if event is removed, otherwise false
	 */
	public boolean remove(int id){
		return DatabaseEvents.deleteEvent(id);
	}
	
	/**Find event based on latitude and longitude
	 * 
	 * @param latitudeMin of the event
	 * @param latitudeMax of the event
	 * @param longiduteMin of the event
	 * @param longiduteMax of the event
	 * @return a list of events in the selected area
	 */
	public List<Event> getByArea(Float latitudeMin, Float latitudeMax, Float longitudeMin, Float longitudeMax){
		return DatabaseEvents.getEvents(latitudeMin, latitudeMax, longitudeMin, longitudeMax);
	}
	
	/**Find events based on radius starting from a position
	 * 
	 * @param latitude of the center
	 * @param longitude of the center
	 * @param radius where to search
	 * @return a list of events in the selected area
	 */
	public List<Event> getByRadius(Float latitude, Float longitude, Float radius){
		return getByArea(latitude-radius, latitude+radius, longitude-radius, longitude+radius);
	}
	
	/**
	 * Find an event given its id
	 * @param id id of the event
	 * @return the found event if it exists, or null
	 */
	public Event getById(int id){
		return DatabaseEvents.getById(id);
	}
	
	public int getSubmitter(int id){
		return DatabaseEvents.getSubmitterId(id);
	}
}
