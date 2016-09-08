package localeventsearch.storage;

import java.util.Calendar;
import java.util.Date;

public enum EventDateGap {
	
	TODAY(new String[]{"today", "tonight"}, Calendar.DAY_OF_YEAR),
	THIS_WEEK(new String[]{"this week"}, Calendar.WEEK_OF_YEAR),
	THIS_MONTH(new String[]{"this month"}, Calendar.MONTH);
	
	private String[] keywords;
	private int calendarElement;
	
	EventDateGap(String[] keywords, int calendarElement) {
		this.keywords = keywords;
		this.calendarElement = calendarElement;
	};
	
	public String removeTimeGapKeywords(String input) {
		for (String keyword:keywords) {
			input = input.replaceAll(keyword, "");
		}
		return input;
	}
	
	public Date getDateForFilter() {
		Calendar c = Calendar.getInstance(); 
		c.add(calendarElement, 1);
		return c.getTime();
	}
}
