package localeventsearch.storage;

import java.util.Date;
import java.util.Map;

import io.searchbox.annotations.JestId;

public class Event {
	@JestId
	public String id;
	public String description;
	public String locationString;
	public String locationAddress;
	public double latitude;
	public double longitude;
	public short popularity;
	public EventType eventType;
	public Date date;
	public String source;
	
	@Override
	public String toString() {
		return "Event [id=" + id + ", description=" + description + ", locationString=" + locationString + ", latitude=" + latitude
				+ ", longitude=" + longitude + ", popularity=" + popularity + ", eventType=" + eventType + ", date="
				+ date + "]";
	}
	
	public String getLocation() {
		return String.format("%f, %f", latitude, longitude);
	}
	
	
}