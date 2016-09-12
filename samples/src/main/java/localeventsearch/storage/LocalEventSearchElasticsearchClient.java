package localeventsearch.storage;

import java.util.List;

public interface LocalEventSearchElasticsearchClient {

	public Event findEvent(String category, String eventName, String timeGap) throws Exception;
	
	public List<Event> findEventList(String category, String eventName, String timeGap) throws Exception;
	
	public Event findEvent(String query) throws Exception;
	
	public List<Event> findEventList(String query) throws Exception;
	
}
