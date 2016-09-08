package localeventsearch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import localeventsearch.storage.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

public class LocalEventSearchSpeechlet implements Speechlet {
	
    private static final String SLOT_EVENT = "Event";

    private static final String SLOT_CATEGORY = "Category";

    private static final String SLOT_DURATION = "Duration";
    
    private static final String SLOT_BLURB = "Blurb";

    private static final Logger log = LoggerFactory.getLogger(LocalEventSearchSpeechlet.class);
    
    //LocalEventSearchService localEventSearchService = new MockLocalEventSearchService();
    LocalEventSearchService localEventSearchService = new LocalEventSearchServiceImpl("http://search-events-cluster-vimfcvl2qetqqffdguwtmcdmym.us-east-1.es.amazonaws.com");

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        try {
        	if ("GetEventIntent".equals(intentName)) {
                return getQueryResponse(request, session);
            } else if ("GetCategoryIntent".equals(intentName)) {
                return getQueryResponse(request, session);
            } else if ("GetEventTimeRangeIntent".equals(intentName)) {
                return getQueryResponse(request, session);
            } else if ("GetCategoryTimeRangeIntent".equals(intentName)) {
                return getQueryResponse(request, session);
            } else if ("GetBlurbIntent".equals(intentName)) {
                return getProcessedQueryResponse(request, session);
            } else {
                throw new SpeechletException("Invalid Intent");
            }
        } catch (Exception e) {
        	throw new SpeechletException("Exception handling request", e);
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }
    
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }
    
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
    
    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }
    
    public static boolean isTomorrow(Date date) {
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DAY_OF_MONTH, 1);
    	return isSameDay(date, cal.getTime());
    }

    public String buildResponse(Event event) {
    	String queryParameters = "";
        if(event == null) {
        	queryParameters = "No events found. Perhaps a movie on amazon would be to your liking.";
        } else {
        	if(event.date != null) {
        		if(isToday(event.date)) {
        			queryParameters = "LocalFinder found the following. " + event.description + " is happening at " + event.locationString + " today.";
        		} else if(isTomorrow(event.date)) {
        			queryParameters = "LocalFinder found the following. " + event.description + " is happening at " + event.locationString + " tomorrow.";
        		} else {
        			SimpleDateFormat format = new SimpleDateFormat("MMMM d");
        			queryParameters = "LocalFinder found the following. " + event.description + " is happening at " + event.locationString + " on " + format.format(event.date);
        		}
        	} else {
        		queryParameters = "LocalFinder found the following. " + event.description + " is happening at " + event.locationString;
        	}
        }
    	return queryParameters;
    }

    private SpeechletResponse getProcessedQueryResponse(final IntentRequest request, final Session session) throws Exception {
    	
    	String blurb = request.getIntent().getSlot(SLOT_BLURB) != null ? request.getIntent().getSlot(SLOT_BLURB).getValue() : "no duration";
    	
    	//String queryParameters = "Query Blurb: " + blurb;
        Event event = localEventSearchService.findEvent(blurb);
        
        String queryParameters = buildResponse(event);

        
    	// Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("QueryParameters");
        card.setContent(queryParameters);
        
        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(queryParameters);
    
        return SpeechletResponse.newTellResponse(speech, card);
    }
    
    private SpeechletResponse getQueryResponse(final IntentRequest request, final Session session) throws Exception {

        String duration = request.getIntent().getSlot(SLOT_DURATION) != null ? request.getIntent().getSlot(SLOT_DURATION).getValue() : "no duration";

        String category = request.getIntent().getSlot(SLOT_CATEGORY) != null ? request.getIntent().getSlot(SLOT_CATEGORY).getValue() : "no category";

        String event = request.getIntent().getSlot(SLOT_EVENT) != null ? request.getIntent().getSlot(SLOT_EVENT).getValue() : "no event";

        Event retEvent = localEventSearchService.findEvent(category, event, duration);
        
        String queryParameters = buildResponse(retEvent);
        
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("QueryParameters");
        card.setContent(queryParameters);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(queryParameters);

        return SpeechletResponse.newTellResponse(speech, card);
    }
    
    private SpeechletResponse getProcessedQueryResponseTest(final IntentRequest request, final Session session) {
    	
    	String blurb = request.getIntent().getSlot(SLOT_BLURB) != null ? request.getIntent().getSlot(SLOT_BLURB).getValue() : "no duration";
    	String queryParameters = "Query Blurb: " + blurb;
        
    	// Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("QueryParameters");
        card.setContent(queryParameters);
        
        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(queryParameters);
    
        return SpeechletResponse.newTellResponse(speech, card);
    }
    
    private SpeechletResponse getQueryResponseTest(final IntentRequest request, final Session session) {

        String duration = request.getIntent().getSlot(SLOT_DURATION) != null ? request.getIntent().getSlot(SLOT_DURATION).getValue() : "no duration";

        String category = request.getIntent().getSlot(SLOT_CATEGORY) != null ? request.getIntent().getSlot(SLOT_CATEGORY).getValue() : "no category";

        String event = request.getIntent().getSlot(SLOT_EVENT) != null ? request.getIntent().getSlot(SLOT_EVENT).getValue() : "no event";

        String queryParameters = "Category: " + category + " " + "Event: " + event + " " + "Duration: " + duration;
                
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("QueryParameters");
        card.setContent(queryParameters);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(queryParameters);

        return SpeechletResponse.newTellResponse(speech, card);
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechText = "Welcome to the Alexa Skills Kit, you can say hello";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("HelloWorld");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        return SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

}
