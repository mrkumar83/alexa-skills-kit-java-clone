package localeventsearch;

import localeventsearch.storage.Event;

public interface LocalEventSearchService {

	public Event findEvent(String category, String eventName, String timeGap);
	
	public Event findEvent(String query);
	
}
