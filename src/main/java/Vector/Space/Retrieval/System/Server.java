package Vector.Space.Retrieval.System;

import Vector.Space.Retrieval.System.preprocessor.WebDocument;
import Vector.Space.Retrieval.System.preprocessor.crawler.Crawler;
import Vector.Space.Retrieval.System.query.QueryProcessor;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class exposes websocket endpoints to receive query message from
 * the client and responds with the ranked list of relevant documents
 * @author Siddhanth Venkateshwaran
 */
public class Server extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private final Crawler crawler;
    private final QueryProcessor queryProcessor;

    /**
     * This class represents an incoming query message from the client
     * @author Siddhanth Venkateshwaran
     */
    static class Query {
        private final String query;

        @JsonCreator
        public Query(@JsonProperty("query") String query) {
            this.query = query;
        }

        /**
         * Get the query message that is sent by the client
         * @return Current query message
         */
        public String getQuery() {
            return this.query;
        }
    }

    /**
     * This static class represents the list of documents
     * that will be serialized into a string and sent to the
     * client as the search results
     * @author Siddhanth Venkateshwaran
     */
    static class WebDocuments {
        private final List<WebDocument> documents;

        public WebDocuments() {
            this.documents = new ArrayList<>();
        }

        /**
         * Gets the list of all documents stored as search results
         * @return List of web document objects
         */
        public List<WebDocument> getDocuments() {
            return this.documents;
        }

        /**
         * Adds the given document to the list of documents
         * @param document Web Document to add
         */
        public void addDocument(WebDocument document) {
            this.documents.add(document);
        }

        /**
         * Removes all documents from the list
         */
        public void deleteDocuments() {
            this.documents.clear();
        }
    }

    public Server() throws UnknownHostException {
        super(new InetSocketAddress(InetAddress.getByName(Constants.address), Constants.port));
        this.crawler = new Crawler(Constants.crawlLimit);
        this.queryProcessor = new QueryProcessor(this.crawler.getIndexer());
    }

    /**
     * This listener executes when server instance is started
     * Initiates the crawl process using the seed url
     */
    @Override
    public void onStart() {
        logger.info(String.format("server started at %s on port %d", this.getAddress(), this.getPort()));
        logger.info("Starting crawler");
        this.crawler.init(Constants.seedUrl);
    }

    /**
     * This executes when a client sends the 1st request for the socket connection
     * @param conn Client socket object
     * @param clientHandshake object storing connection handshake parameters
     */
    @Override
    public void onOpen(WebSocket conn, ClientHandshake clientHandshake) {
        logger.info(String.format("connection request received from %s", conn.getRemoteSocketAddress().getAddress().toString()));
    }

    /**
     * This executes whenever the client sends a query message to the server
     * @param conn Client socket object
     * @param message Query as a JSON string message
     */
    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info(String.format("received %s from %s", message, conn.getRemoteSocketAddress().getAddress().toString()));
        ObjectMapper mapper = new ObjectMapper();

        try {
            Query query = mapper.readValue(message, Query.class);
            WebDocuments response = new WebDocuments();

            Map<WebDocument, Double> searchResults =
                    this.queryProcessor.getRankedMapOfDocuments(this.queryProcessor.getTokens(query.getQuery()));
            searchResults.keySet().forEach(response::addDocument);

            conn.send(mapper.writeValueAsString(response));
        }
        catch(JsonProcessingException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This executes when a client closes its connection with the server
     * @param conn Client socket object
     * @param code Indicates reason of connection termination
     * @param reason Additional information for connection termination
     * @param remote Indicates whether or not the remote host closed the connection
     */
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        if (conn != null)
            logger.info(String.format("%s closed the connection", conn.getRemoteSocketAddress().getAddress().toString()));
    }

    /**
     * This executes whenever an exception gets generated
     * as a result of the socket connection
     * @param conn Client socket object
     * @param ex Exception object
     */
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
}
