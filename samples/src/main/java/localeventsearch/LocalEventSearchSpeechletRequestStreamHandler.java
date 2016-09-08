package localeventsearch;

import helloworld.HelloWorldSpeechlet;

import java.util.HashSet;
import java.util.Set;

import scorekeeper.ScoreKeeperSpeechlet;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

/**
 * This class could be the handler for an AWS Lambda function powering an Alexa Skills Kit
 * experience. To do this, simply set the handler field in the AWS Lambda console to
 * "scorekeeper.ScoreKeeperSpeechletRequestStreamHandler" For this to work, you'll also need to
 * build this project using the {@code lambda-compile} Ant task and upload the resulting zip file to
 * power your function.
 */
public final class LocalEventSearchSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds;

    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        supportedApplicationIds = new HashSet<String>();
        // supportedApplicationIds.add("amzn1.echo-sdk-ams.app.[unique-value-here]");
    }

    public LocalEventSearchSpeechletRequestStreamHandler() {
        //super(new ScoreKeeperSpeechlet(), supportedApplicationIds);
        super(new HelloWorldSpeechlet(), supportedApplicationIds);
    }
}
