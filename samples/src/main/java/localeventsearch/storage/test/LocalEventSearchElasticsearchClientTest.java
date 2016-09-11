package localeventsearch.storage.test;

import localeventsearch.storage.LocalEventSearchElasticsearchClientImpl;

public class LocalEventSearchElasticsearchClientTest {

	public static void main(String[] args) throws Exception {
		testBasicQueries();
	}
	
	public static void testBasicQueries() throws Exception {

		LocalEventSearchElasticsearchClientImpl service = new LocalEventSearchElasticsearchClientImpl("http://search-events-cluster-vimfcvl2qetqqffdguwtmcdmym.us-east-1.es.amazonaws.com");
		service.findEventList("jazz concerts this month");
		service.findEventList("concerts this week");
		service.findEventList("metallica");
		service.findEventList("journey tribute");
		service.findEventList("jazz concerts");
		service.findEventList("Metallica concerts");
		service.findEventList("jazz this month");
		service.findEventList("jazz this week");
		service.findEventList("jazz this tonight");
		service.findEventList("tonight");
		
		service.findEvent("jazz concerts tonight");
		service.findEvent("jazz concerts");
		service.findEvent("Metallica concerts");
		service.findEvent("jazz this month");
		service.findEvent("jazz this week");
		service.findEvent("jazz this tonight");
		service.findEvent("tonight");
		
		service.findEventList("concerts", "jazz", "tonight");
		service.findEventList("concerts", "jazz", "");
		service.findEventList("concerts", "jazz", null);
		service.findEventList("concerts", "Metallica", null);
		service.findEventList("", "jazz", "this month");
		service.findEventList(null, "jazz", "this month");
		service.findEventList(null, "jazz", "this week");
		service.findEventList(null, "jazz", "tonight");
		service.findEventList(null, "", "tonight");
		service.findEventList("", null, "tonight");
		
		service.findEvent("concerts", "jazz", "tonight");
		service.findEvent("concerts", "jazz", "");
		service.findEvent("concerts", "jazz", null);
		service.findEvent("concerts", "Metallica", null);
		service.findEvent("", "jazz", "this month");
		service.findEvent(null, "jazz", "this month");
		service.findEvent(null, "jazz", "this week");
		service.findEvent(null, "jazz", "tonight");
		service.findEvent(null, "", "tonight");
		service.findEvent("", null, "tonight");
	}
}
