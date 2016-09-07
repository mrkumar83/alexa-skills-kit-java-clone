package localeventsearch.storage;

import com.amazonaws.services.elasticsearch.AWSElasticsearchClient;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class LocalEventSearchElasticsearchClient {

	AWSElasticsearchClient awsESConfigClient;
	Client esSearchClient;

    public LocalEventSearchElasticsearchClient(
    		final AWSElasticsearchClient awsESConfigClient,
    		final Client esClient) {
        this.awsESConfigClient = awsESConfigClient;
        this.esSearchClient = esClient;
    }
}
