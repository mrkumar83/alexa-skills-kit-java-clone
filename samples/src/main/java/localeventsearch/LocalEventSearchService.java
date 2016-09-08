package localeventsearch;

import java.util.List;

import localeventsearch.storage.Event;

public interface LocalEventSearchService {

	public Event findEvent(String category, String eventName, String timeGap);
	
	public List<Event> findEventList(String category, String eventName, String timeGap);
	
	public Event findEvent(String query);
	
}
