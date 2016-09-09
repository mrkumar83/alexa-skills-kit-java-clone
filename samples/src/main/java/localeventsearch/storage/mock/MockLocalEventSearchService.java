package localeventsearch.storage.mock;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import localeventsearch.storage.Event;
import localeventsearch.storage.EventType;
import localeventsearch.storage.LocalEventSearchService;

public class MockLocalEventSearchService implements LocalEventSearchService {
	
	/*
	{
	      "_index" : "events",
	      "_type" : "events",
	      "_id" : "2978624",
	      "_score" : 1.0,
	      "_source" : {
	        "id" : "2978624",
	        "description" : "Davell Crawford",
	        "locationString" : "Yoshi's Oakland",
	        "locationAddress" : "510 Embarcadero West, Oakland, CA 94607, United States",
	        "latitude" : 37.79637749999999,
	        "longitude" : -122.2782598,
	        "popularity" : 2,
	        "eventType" : "CONCERT",
	        "date" : "2016-09-07T00:00:00-0700",
	        "source" : "http://www.pollstar.com/event/2978624&source=rss"
	      }
	},
	*/
	

	@Override
	public Event findEvent(String category, String eventName, String timeGap) {
		Event retEvent = new Event();
		retEvent.id = "2978624";
		retEvent.description = eventName;
		retEvent.locationString = "Yoshi's Oakland";
		retEvent.locationAddress = "510 Embarcadero West, Oakland, CA 94607, United States";
		retEvent.latitude = 37.79637749999999;
		retEvent.longitude = -122.2782598;
		retEvent.popularity = 2;
		retEvent.eventType = EventType.CONCERT;
		retEvent.date = new Date();
		retEvent.source = "http://www.pollstar.com/event/2978624&source=rss";
		return retEvent;
	}

	/*
	{
	      "_index" : "events",
	      "_type" : "events",
	      "_id" : "2994230",
	      "_score" : 1.0,
	      "_source" : {
	        "id" : "2994230",
	        "description" : "Eminence Ensemble",
	        "locationString" : "Hotel Utah",
	        "locationAddress" : "500 4th St, San Francisco, CA 94107, United States",
	        "latitude" : 37.7793954,
	        "longitude" : -122.3980837,
	        "popularity" : 2,
	        "eventType" : "CONCERT",
	        "date" : "2016-09-09T00:00:00-0700",
	        "source" : "http://www.pollstar.com/event/2994230&source=rss"
	      }
	},
	*/
	
	@Override
	public Event findEvent(String query) {
		Event retEvent = new Event();
		retEvent.id = "2994230";
		retEvent.description = "Eminence Ensemble";
		retEvent.locationString = "Hotel Utah";
		retEvent.locationAddress = "500 4th St, San Francisco, CA 94107, United States";
		retEvent.latitude = 37.7793954;
		retEvent.longitude = -122.3980837;
		retEvent.popularity = 2;
		retEvent.eventType = EventType.CONCERT;
		retEvent.date = new Date();
		retEvent.source = "http://www.pollstar.com/event/2994230&source=rss";
		return retEvent;
	}

	@Override
	public List<Event> findEventList(String category, String eventName,
			String timeGap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Event> findEventList(String query) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
