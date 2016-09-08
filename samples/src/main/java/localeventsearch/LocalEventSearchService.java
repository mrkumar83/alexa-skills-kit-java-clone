package localeventsearch;

import localeventsearch.storage.Event;

public interface LocalEventSearchService {

	public Event findEvent(String category, String eventName, String timeGap) throws Exception;
	
	public Event findEvent(String query) throws Exception;
	
}
