package localeventsearch;

import java.util.List;

import localeventsearch.storage.Event;

public interface LocalEventSearchService {


	public Event findEvent(String category, String eventName, String timeGap) throws Exception;
	
	public List<Event> findEventList(String category, String eventName, String timeGap) throws Exception;
	
	public Event findEvent(String query) throws Exception;
	
	public List<Event> findEventList(String query) throws Exception;
	
}
