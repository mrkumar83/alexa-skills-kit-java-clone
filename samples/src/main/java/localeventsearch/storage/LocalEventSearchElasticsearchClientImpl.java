package localeventsearch.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.amazonaws.services.elasticsearch.AWSElasticsearchClient;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

public class LocalEventSearchElasticsearchClientImpl implements LocalEventSearchElasticsearchClient {

	AWSElasticsearchClient awsESConfigClient; //cluster configuration, health information
	Client esSearchClient; //es client for 2.3
	private final JestClient es; //simple rest client, http://blogs.justenougharchitecture.com/using-jest-as-a-rest-based-java-client-with-elasticsearch/

	// Palo Alto
	private final static double[] DEFAULT_LOCATION_LAT_LONG = new double[]{37.4419, -122.1430};

	public LocalEventSearchElasticsearchClientImpl(String elasticsearch) {
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
				.Builder(elasticsearch)
				.multiThreaded(true)
				.build());
		es = factory.getObject();
	}
	
	private List<Event> findEventList(EventType eventType, EventDateGap eventDateGap, String query) throws Exception {
		query = query.toLowerCase();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		
		// Don't show past events
		QueryBuilder queryBuilder = QueryBuilders.rangeQuery("date").from(new Date()); 
		if (query.trim().length() > 0) {
			// there is a query, add it
			MatchQueryBuilder matchDescription = QueryBuilders.matchPhraseQuery("description", query);
			matchDescription.slop(20);
			queryBuilder = QueryBuilders.boolQuery().must(matchDescription).must(queryBuilder);
		}
		if (eventType != null) {
			// specific event type. filter
			queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchPhraseQuery("eventType", eventType.toString())).must(queryBuilder);
		}
		
		if (eventDateGap != null) {
			queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("date").from(new Date()).to(eventDateGap.getDateForFilter())).must(queryBuilder);
		} else {
			// boost recent
			queryBuilder = QueryBuilders.boolQuery().should(QueryBuilders.functionScoreQuery(queryBuilder).boost(5).add(ScoreFunctionBuilders.gaussDecayFunction("date", new Date(), "1d")));
		}
		
		// boost close
		queryBuilder = QueryBuilders.boolQuery().must(queryBuilder).should(generateDistanceBoost());
		
		// TODO: Boost by popularity
		
		searchSourceBuilder.query(queryBuilder);
		System.out.println("Query: " + searchSourceBuilder.toString());
		Search search = new Search.Builder(searchSourceBuilder.toString())
                // multiple index or types can be added.
                .addIndex("events")
                .addIndex("events")
                .setParameter("pretty", "true")
                .build();
		SearchResult result = es.execute(search);
		if (!result.isSucceeded()) {
			throw new RuntimeException("Search failed with error: " + result.getErrorMessage());
		}
		System.out.println("Search result: " + result.getJsonString());
//		if (result.getTotal() == 0) {
//			return null;
//		}
		final List<Event> events = new ArrayList<>();
		result.getHits(Event.class).forEach(hit -> events.add(hit.source));
		return events;
//		return result.getFirstHit(Event.class).source;
	}
	

	/* 
	 * Cases covered: 
	 * "EVENT_TYPE". For example "concerts"
	 * "EVENT_TYPE DATE_GAP". For example "concerts this week"
	 * "QUERY EVENT_TYPE DATE_GAP". For example "Metallica concerts this month"
	 */
	
	@Override
	public List<Event> findEventList(String query) throws Exception {
		Objects.nonNull(query);
		query = query.toLowerCase();
		
		EventType eventType = extractEventType(query);
		if (eventType != null) {
			query = eventType.removeEventTypeKeywords(query);
		}
		
		EventDateGap eventDateGap = extractEventDateGap(query);
		
		if (eventDateGap != null) {
			query = eventDateGap.removeTimeGapKeywords(query);
		}
		return findEventList(eventType, eventDateGap, query);
		
	}



	private QueryBuilder generateDistanceBoost() {
		return QueryBuilders.boolQuery()
			.should(QueryBuilders.boolQuery().boost(10).should(QueryBuilders.geoDistanceRangeQuery("location").from("0km").to("10km").point(DEFAULT_LOCATION_LAT_LONG[0], DEFAULT_LOCATION_LAT_LONG[1])))
			.should(QueryBuilders.boolQuery().boost(5).should(QueryBuilders.geoDistanceRangeQuery("location").from("10km").to("25km").point(DEFAULT_LOCATION_LAT_LONG[0], DEFAULT_LOCATION_LAT_LONG[1])))
			.should(QueryBuilders.boolQuery().boost(2).should(QueryBuilders.geoDistanceRangeQuery("location").from("25km").to("50km").point(DEFAULT_LOCATION_LAT_LONG[0], DEFAULT_LOCATION_LAT_LONG[1])))
			.should(QueryBuilders.boolQuery().boost(-5).should(QueryBuilders.geoDistanceRangeQuery("location").from("200km").point(DEFAULT_LOCATION_LAT_LONG[0], DEFAULT_LOCATION_LAT_LONG[1])));
	}

	private EventDateGap extractEventDateGap(String query)  throws Exception{
		if (query == null) {
			return null;
		}
		for (EventDateGap type:EventDateGap.values()) {
			String q = type.removeTimeGapKeywords(query);
			if (!q.equals(query)) {
				return type;
			}
		}
		return null;
	}



	/*
	 * Very simple implementation for now
	 */
	private EventType extractEventType(String query) {
		if (query == null) {
			return null;
		}
		for (EventType type:EventType.values()) {
			if (query.contains(type.name().toLowerCase())) {
				return type;
			}
		}
		for (EventType type:EventType.values()) {
			String q = type.removeEventTypeKeywords(query);
			if (!q.equals(query)) {
				return type;
			}
		}
		
		return null;
	}
	

	@Override
	public List<Event> findEventList(String category, String eventName, String timeGap) throws Exception {
		EventType eventType = extractEventType(category != null ?category.toLowerCase():null);
		EventDateGap eventDateGap = extractEventDateGap(timeGap != null ?timeGap.toLowerCase():null);
		return findEventList(eventType, eventDateGap, eventName != null? eventName.toLowerCase():"");
	}
	
	public static void main(String[] args) throws Exception {
		LocalEventSearchElasticsearchClientImpl service = new LocalEventSearchElasticsearchClientImpl("http://search-events-cluster-vimfcvl2qetqqffdguwtmcdmym.us-east-1.es.amazonaws.com");
//		service.findEventList("jazz concerts this month");
//		service.findEventList("concerts this week");
		service.findEventList("metallica");
//		service.findEventList("journey tribute");
//		service.findEventList("jazz concerts");
//		service.findEventList("Metallica concerts");
//		service.findEventList("jazz this month");
//		service.findEventList("jazz this week");
//		service.findEventList("jazz this tonight");
//		service.findEventList("tonight");
//		
//		service.findEvent("jazz concerts tonight");
//		service.findEvent("jazz concerts");
//		service.findEvent("Metallica concerts");
//		service.findEvent("jazz this month");
//		service.findEvent("jazz this week");
//		service.findEvent("jazz this tonight");
//		service.findEvent("tonight");
//		
//		service.findEventList("concerts", "jazz", "tonight");
//		service.findEventList("concerts", "jazz", "");
//		service.findEventList("concerts", "jazz", null);
//		service.findEventList("concerts", "Metallica", null);
//		service.findEventList("", "jazz", "this month");
//		service.findEventList(null, "jazz", "this month");
//		service.findEventList(null, "jazz", "this week");
//		service.findEventList(null, "jazz", "tonight");
//		service.findEventList(null, "", "tonight");
//		service.findEventList("", null, "tonight");
//		
//		service.findEvent("concerts", "jazz", "tonight");
//		service.findEvent("concerts", "jazz", "");
//		service.findEvent("concerts", "jazz", null);
//		service.findEvent("concerts", "Metallica", null);
//		service.findEvent("", "jazz", "this month");
//		service.findEvent(null, "jazz", "this month");
//		service.findEvent(null, "jazz", "this week");
//		service.findEvent(null, "jazz", "tonight");
//		service.findEvent(null, "", "tonight");
//		service.findEvent("", null, "tonight");
	}

	@Override
	public Event findEvent(String category, String eventName, String timeGap) throws Exception {
		List<Event> events = findEventList(category, eventName, timeGap);
		if (events.isEmpty()) {
			return null;
		}
		return events.get(0);
	}

	@Override
	public Event findEvent(String query) throws Exception {
		List<Event> events = findEventList(query);
		if (events.isEmpty()) {
			return null;
		}
		return events.get(0);
	}

}
