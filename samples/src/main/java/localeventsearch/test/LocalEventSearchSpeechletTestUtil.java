package localeventsearch.test;

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;

public class LocalEventSearchSpeechletTestUtil {

    private static final String SLOT_EVENT = "Event";

    private static final String SLOT_CATEGORY = "Category";

    private static final String SLOT_DURATION = "Duration";

    private static final String SLOT_BLURB = "Blurb";

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
}
