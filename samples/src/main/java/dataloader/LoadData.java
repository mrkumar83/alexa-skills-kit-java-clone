package dataloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.searchbox.annotations.JestId;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import localeventsearch.storage.Event;
import localeventsearch.storage.EventType;

public class LoadData {
	
	private final static String LOCATION_CACHE_INDEX = "location-cache";
	private final static String EVENTS_INDEX = "events";
	private final static String CITIES_INDEX = "cities";
	private final static Pattern POLLSTAR_EVENT_TITLE_PATTERN = Pattern.compile("(.*) at (.*)");
	private final static Pattern POLLSTAR_EVENT_DESCRIPTION_PATTERN = Pattern.compile(".* on (\\w+, \\w+ [0-9][0-9], 201[0-9]).*");
	private final static String POLLSTAR_EVENT_DATE_FORMAT = "EEEE, MMMM dd, yyyy";
	private final static String GET_CITY_QUERY = "{'query': {'match_phrase':{'name': '%s'}}}".replaceAll("'", "\""); //TODO state
//	private final static String CACHE_LOCATION_QUERY = "{'query': { 'and': [{ 'match_phrase': {'name':'%s'}}, { 'match_phrase': {'city':'%s'}}] }}}".replaceAll("'", "\"");
	private final static String CACHE_LOCATION_QUERY = "{'query': { 'match_phrase': {'name':'%s'}} }".replaceAll("'", "\"");
	private final static String GOOGLE_MAPS_QUERY = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=%S&location=%f,%f&radius=50000&key=%s" ;
	
	private final JestClient es;
	private final String googleMapsKey;
	
	public LoadData(String elasticsearch, String googleMapsKey) {
		this.googleMapsKey = googleMapsKey;
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(new HttpClientConfig
				.Builder(elasticsearch)
				.multiThreaded(true)
				.build());
		es = factory.getObject();
	}
	
	public static class City {
		String name;
		String state;
		double longitude,latitude;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public double getLongitude() {
			return longitude;
		}
		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}
		public double getLatitude() {
			return latitude;
		}
		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}
		
	}
	
	public static class Location {
		@JestId
		String id;
		String name,address,city;
		double longitude,latitude;
		double rating;
		
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
		public double getRating() {
			return rating;
		}
		public void setRating(double rating) {
			this.rating = rating;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public double getLongitude() {
			return longitude;
		}
		public void setLongitude(double longitude) {
			this.longitude = longitude;
		}
		public double getLatitude() {
			return latitude;
		}
		public void setLatitude(double latitude) {
			this.latitude = latitude;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		
	}
	
	private static String readUrlToString(String rssUrl) throws Exception {
		return IOUtils.toString(readUrl(rssUrl), "UTF-8");
	}
	
	private static InputStream readUrl(String rssUrl) throws Exception {
		URL url = new URL(rssUrl);
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-Agent", "curl/7.43.0");
		InputStream in = con.getInputStream();
		return in;
	}
	
	private void loadPollstar(String pollstarUrl, String city, String googleMapsKey) throws Exception {
		System.setProperty("http.agent", "");
		InputStream rss = readUrl(pollstarUrl);
//		String rss = IOUtils.toString(new FileInputStream(new File("/Users/tflobbe/Documents/a9/workspaces/hackday/alexa-skills-kit-java/samples/src/test/resources/2.xml")), "UTF-8");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(rss);
		doc.getDocumentElement().normalize();
		NodeList items = doc.getElementsByTagName("item");
		for (int temp = 0; temp < items.getLength(); temp++) {
			Node nNode = items.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String title = eElement.getElementsByTagName("title").item(0).getTextContent();
				String description = eElement.getElementsByTagName("description").item(0).getTextContent();
				Matcher m =  POLLSTAR_EVENT_TITLE_PATTERN.matcher(title);
				Matcher m2 =  POLLSTAR_EVENT_DESCRIPTION_PATTERN.matcher(description);
				if (!m.find() || !m2.find()) {
					System.out.println("Skipping " + title);
					continue;
				}
				Event e = new Event();
				e.description = m.group(1);
				e.locationString = m.group(2);
				e.date = new SimpleDateFormat(POLLSTAR_EVENT_DATE_FORMAT).parse(m2.group(1));
				e.eventType = EventType.CONCERT; //TODO
				e.popularity = (short)(Math.random() * 6);
				e.source = eElement.getElementsByTagName("link").item(0).getTextContent();
				e.id = eElement.getElementsByTagName("guid").item(0).getTextContent().replaceAll("E:", "");
//				if (eventExists(e)) {
//				System.out.println("Event exists. skipping");
//					continue;
//				}
				Location location = getLocationForEvent(e, getCity(city));
				if (location == null) {
					// skip this element
					System.out.println("Location not found. skipping");
					continue;
				}
				e.latitude = location.latitude;
				e.longitude = location.longitude;
				e.locationAddress = location.address;
				e.location = String.format("%f, %f", location.latitude, location.longitude);
				addToIndex(e);
				System.out.println(e);
			}
		}
	}

	private boolean eventExists(Event e) throws Exception {
		Get get = new Get.Builder(EVENTS_INDEX, e.id).type(EVENTS_INDEX).build();
		JestResult result = es.execute(get);
		Event event = result.getSourceAsObject(Event.class);
		return event != null;
	}

	private void addToIndex(Event e) throws Exception {
		Index index = new Index.Builder(e).index(EVENTS_INDEX).type(EVENTS_INDEX).build();
		DocumentResult r = es.execute(index);
		if (!r.isSucceeded()) {
			throw new Exception("Bad Request:" + r.getErrorMessage());
		}
	}

	private City getCity(String cityName) throws IOException {
		Search search = new Search.Builder(String.format(GET_CITY_QUERY, cityName))
                .addIndex(CITIES_INDEX)
                .build();
		SearchResult result = es.execute(search);
		return result.getFirstHit(City.class).source;
	}

	private Location getLocationForEvent(Event e, City city) throws Exception {
		Objects.nonNull(e.locationString);
		Objects.nonNull(city.latitude);
		Objects.nonNull(city.longitude);
		// first get from cache
		Search search = new Search.Builder(String.format(CACHE_LOCATION_QUERY, e.locationString, city.name))
                .addIndex(LOCATION_CACHE_INDEX)
                .build();
		SearchResult result = es.execute(search);
		Location l;
		if (result.getTotal() == 0) {
			// get from google
//			String googleResponse = readUrlToString(String.format(GOOGLE_MAPS_QUERY, URLEncoder.encode(e.locationString.replaceAll(" ", "+"), "UTF-8"), city.latitude, city.longitude, googleMapsKey));
//			System.out.println("Contacted google for " + e.locationString);
//			ObjectMapper objectMapper = new ObjectMapper();
//			JsonNode node = objectMapper.readValue(googleResponse, JsonNode.class);
//			try {
//				JsonNode googleResult = node.get("results").elements().next();
//				l = new Location();
//				l.address = googleResult.get("formatted_address").asText();
//				l.rating = googleResult.get("rating") != null? googleResult.get("rating").asDouble():0;
//				l.name = e.locationString;
//				l.latitude = ((JsonNode)((JsonNode)googleResult.get("geometry")).get("location")).get("lat").asDouble();
//				l.longitude = ((JsonNode)((JsonNode)googleResult.get("geometry")).get("location")).get("lng").asDouble();
//				l.city = city.name;
//				l.id = Long.toString(System.currentTimeMillis());
//				//put in index
//				Index index = new Index.Builder(l).index(LOCATION_CACHE_INDEX).type(LOCATION_CACHE_INDEX).build();
//				DocumentResult r = es.execute(index);
//				if (!r.isSucceeded()) {
//					throw new Exception("Bad Request:" + r.getErrorMessage());
//				}
//			} catch (NoSuchElementException exception) {
//				System.out.println("No Results in google for " + e.locationString);
//				return null;
//			}
			return null;
		} else {
			l = result.getFirstHit(Location.class).source;
			System.out.println(String.format("Found location %s, query was for %s", l.name, e.locationString));
		}
		return l;
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			throw new Exception("Expecting Elasticsearch url and google maps key");
		}
		LoadData l = new LoadData(args[0], args[1]);
//		l.loadPollstar("http://www.pollstar.com/pollstarRSS.aspx?feed=city&id=49955&surrounding=True", "San Francisco", "foo");
//		l.loadPollstar("http://www.pollstar.com/pollstarRSS.aspx?feed=city&id=42111&surrounding=True", "Oakland", "foo");
		l.loadPollstar("http://www.pollstar.com/pollstarRSS.aspx?feed=city&id=34565&surrounding=True", "Los Angeles", "foo");
		
	}
}
