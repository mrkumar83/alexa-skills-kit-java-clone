package localeventsearch.storage;

public enum EventType {
	CONCERT(new String[]{"concerts", "concert"});


	private String[] keywords;

	EventType(String[] keywords) {
		this.keywords = keywords;
	};

	public String removeEventTypeKeywords(String input) {
		for (String keyword:keywords) {
			input = input.replaceAll(keyword, "");
		}
		return input;
	}
}