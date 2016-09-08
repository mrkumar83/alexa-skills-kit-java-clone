package localeventsearch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import localeventsearch.storage.Event;
import localeventsearch.storage.EventDateGap;
import localeventsearch.storage.EventType;

public class LocalEventSearchServiceImpl implements LocalEventSearchService {
	
	// Palo Alto
	private final static double[] DEFAULT_LOCATION_LAT_LONG = new double[]{37.4419, -122.1430};
	
	private final JestClient es;
	
	public LocalEventSearchServiceImpl(String elasticsearch) {
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
				.Builder(elasticsearch)
				.multiThreaded(true)
				.build());
		es = factory.getObject();
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
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		
		EventType eventType = extractEventType(query);
		if (eventType != null) {
			query = eventType.removeEventTypeKeywords(query);
		}
		
		EventDateGap eventDateGap = extractEventDateGap(query);
		
		if (eventDateGap != null) {
			query = eventDateGap.removeTimeGapKeywords(query);
		}
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
			queryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.functionScoreQuery(queryBuilder).boost(5).add(ScoreFunctionBuilders.gaussDecayFunction("date", new Date(), "1d")));
		}
		
		// boost close
//		TODO: queryBuilder = QueryBuilders.boolQuery().must(queryBuilder).should(generateDistanceBoost());
		
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
		if (result.getTotal() == 0) {
			return null;
		}
		final List<Event> events = new ArrayList<>();
		result.getHits(Event.class).forEach(hit -> events.add(hit.source));
		return events;
//		return result.getFirstHit(Event.class).source;
	}



	private QueryBuilder generateDistanceBoost() {
//		QueryBuilders.boolQuery().must(QueryBuilders.geoDistanceRangeQuery("").)
		return null;
	}

	private EventDateGap extractEventDateGap(String query)  throws Exception{
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
	
	public static void main(String[] args) throws Exception {
		LocalEventSearchServiceImpl service = new LocalEventSearchServiceImpl("http://search-events-cluster-vimfcvl2qetqqffdguwtmcdmym.us-east-1.es.amazonaws.com");
		service.findEvent("jazz concerts tonight");
		service.findEvent("jazz concerts");
		service.findEvent("Metallica concerts");
		service.findEvent("jazz this month");
		service.findEvent("jazz this week");
		service.findEvent("jazz this tonight");
		service.findEvent("tonight");
	}

	@Override
	public Event findEvent(String category, String eventName, String timeGap) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Event> findEventList(String category, String eventName, String timeGap) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Event findEvent(String query) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
