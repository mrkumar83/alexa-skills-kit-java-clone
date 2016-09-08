package localeventsearch;

import helloworld.HelloWorldSpeechlet;

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

    private static final String SLOT_DURATION = "PlayerName";
    
    private static final String SLOT_BLURB = "Blurb";

    private static final Logger log = LoggerFactory.getLogger(LocalEventSearchSpeechlet.class);

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
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    private SpeechletResponse getProcessedQueryResponse(final IntentRequest request, final Session session) {
    	
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
    
    private SpeechletResponse getQueryResponse(final IntentRequest request, final Session session) {

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
