package Vector.Space.Retrieval.System.indexer;

import ch.qos.logback.classic.Level;

import Vector.Space.Retrieval.System.preprocessor.IndexItem;
import Vector.Space.Retrieval.System.preprocessor.WebDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class will build and store the inverted index for the search engine
 * @author Siddhanth Venkateshwaran
 */
public class InvertedIndexer {
    private int collectionSize;
    private Map<String, Map<String, IndexItem>> index;
    private Map<String, Double> documentVector;

    private final Logger logger = LoggerFactory.getLogger(InvertedIndexer.class);

    public InvertedIndexer() {
        this.index = new HashMap<>();
        this.documentVector = new HashMap<>();
        this.collectionSize = 0;
//        ((ch.qos.logback.classic.Logger)logger).setLevel(Level.OFF);
    }

    /**
     * Adds the document details to the inverted index for the relevant tokens <br>
     * Adds these tokens and document details to the database in which the index resides
     * @param tokens List of tokens for the document at the given url
     * @param url Normalized url for of the document
     * @param title Title of the document
     * @param description Meta description of the document
     */
    public void addToIndex(List<String> tokens, String url, String title, String description) {
        WebDocument document = new WebDocument(url, title, description);
        tokens.forEach(token -> {
            this.index.putIfAbsent(token, new HashMap<>());
            if (!this.index.get(token).containsKey(url))
                this.index.get(token).put(url, new IndexItem(document, 0));

            int oldTermFrequency = this.index.get(token).get(url).getTermFrequency();
            this.index.get(token).get(url).setTermFrequency(oldTermFrequency+1);
        });
    }

    /**
     * Constructs a map to store euclidean normalized lengths of all documents
     * using the (already) computed inverted index.
     */
    public void constructDocumentVectorTable() {
        Map<String, Map<String, IndexItem>> invIndex = this.getIndex();
        Map<String, Double> docVector = this.getDocumentVector();

        invIndex.keySet().forEach(term -> {
            invIndex.get(term).forEach((documentUrl, indexItem) -> {
                try {
                    double currentTermWeight = Math.pow(getWeight(term, documentUrl), 2);
                    if (!docVector.containsKey(documentUrl)) docVector.put(documentUrl, 0.0);
                    docVector.put(documentUrl, docVector.get(documentUrl)+currentTermWeight);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            });
        });

        /* store as square root of each computed length */
        docVector.keySet().forEach(documentUrl -> docVector.put(documentUrl, Math.sqrt(docVector.get(documentUrl))));
    }

    /**
     * Computes and returns the weight of a token for a given document
     * @param term Term whose weight is to be computed
     * @param document Target document url for the term
     * @return Weight of term for the target document i.e. TF(term, document) * IDF(term)
     */
    public double getWeight(String term, String document) throws Exception {
        return getTermFrequency(term, document) * getInverseDocumentFrequency(term);
    }

    /**
     * This computes and returns the total number of web documents in which the given term appears
     * @param term Term whose document frequency is to be found
     * @return number of documents in which term appears
     */
    public int getDocumentFrequency(String term) {
        if (this.index.containsKey(term)) return this.index.get(term).keySet().size();
        return 0;
    }

    /**
     * This computes and returns the number of occurrences of a token in a given document
     * @param term The term for which the frequency is to be obtained
     * @param documentUrl The url of the document in which term frequency is to be obtained
     * @return number of times the term appears in the whole web document at this URL
     */
    public int getTermFrequency(String term, String documentUrl) {
        Map<String, Map<String, IndexItem>> invIndex = this.getIndex();
        if (invIndex.containsKey(term) && invIndex.get(term).containsKey(documentUrl)) {
            return invIndex.get(term).get(documentUrl).getTermFrequency();
        }
        logger.info(String.format("Queried for term frequency of %s%n", term));

        if (invIndex.containsKey(term)) System.out.printf("%s does not appear in the document at url %s%n", term, documentUrl);
        else {
            logger.info(String.format("%s does not appear in the whole index%n%n", term));
        }
        return 0;
    }

    /**
     * This computes and returns the IDF of a given term
     * @param term The term whose IDF is to be found
     * @return IDF(term), which is (log<sub>10</sub> ( collectionSize / documentFrequency(term) ))
     */
    public double getInverseDocumentFrequency(String term) throws Exception {
        if (this.index.containsKey(term)) {
            double df = getDocumentFrequency(term);
            if (df == this.collectionSize) return 1;
            return Math.log10((double) (this.collectionSize) / (double) (getDocumentFrequency(term)));
        }
        throw new Exception();
    }

    /**
     * Constructs and returns a string representation of the postings list of each token in the inverted index
     * @return string representation of inverted index
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        Map<String, Map<String, IndexItem>> invIndex = this.getIndex();

        invIndex.keySet().forEach(token -> invIndex.get(token).forEach((documentUrl, indexItem) -> {
            result.append(String.format("%s -> %s -> %d\t\t",
                    token, indexItem.getDocument().getTitle(), indexItem.getTermFrequency()));
            result.append("\n");
        }));
        return result.toString();
    }

    /**
     * Obtains the inverted index of the current indexer instance
     * @return Inverted index of this collection
     */
    public Map<String, Map<String, IndexItem>> getIndex() {
        return this.index;
    }

    /**
     * Store index object
     * @param index Inverted index
     */
    public void setIndex(Map<String, Map<String, IndexItem>> index) {
        this.index = index;
    }

    /**
     * Obtains the vector having euclidean normalized lengths of each document indexed
     * @return Map of document urls to euclidean normalized lengths
     */
    public Map<String, Double> getDocumentVector() {
        return this.documentVector;
    }

    /**
     * Store the euclidean normalized document lengths vector
     * @param documentVector Document lengths vector
     */
    public void setDocumentVector(Map<String, Double> documentVector) {
        this.documentVector = documentVector;
    }

    /**
     * Obtains the euclidean normalized vector of document at specified url
     * @param documentUrl Absolute URL of document
     * @return the euclidean normalized length
     */
    public Double getDocumentLength(String documentUrl) {
        if (this.documentVector.containsKey(documentUrl))
            return this.documentVector.get(documentUrl);
        return 0.0;
    }

    /**
     * Get the number of documents indexed till now
     * @return Number of indexed documents
     */
    public int getCollectionSize() {
        return this.collectionSize;
    }

    /**
     * Set the number of web documents indexed
     * @param collectionSize Number of collection documents indexed
     */
    public void setCollectionSize(int collectionSize) {
        this.collectionSize = collectionSize;
    }

    /**
     * Prints the euclidean normalized document vector of all documents in the index
     */
    public void printDocumentVector() {
        logger.info("print doc vector now");
        this.getDocumentVector().forEach((docUrl, docLength) -> logger.info(String.format("%s -> %f", docUrl, docLength)));
        logger.info("finished printing doc vector now");
    }
}
