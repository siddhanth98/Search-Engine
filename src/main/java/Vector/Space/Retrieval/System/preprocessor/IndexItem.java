package Vector.Space.Retrieval.System.preprocessor;

import java.io.Serializable;

/**
 * This class is the serializable object stored corresponding
 * to each document URL for each token in the inverted index. <br>
 * It will have the web document object and corresponding
 * term frequency of the term in the document at the URL.
 * @author Siddhanth Venkateshwaran
 */
public class IndexItem implements Serializable {
    private final WebDocument document;
    private int termFrequency;

    public IndexItem(final WebDocument document, final int termFrequency) {
        this.document = document;
        this.termFrequency = termFrequency;
    }

    public WebDocument getDocument() {
        return this.document;
    }

    public int getTermFrequency() {
        return this.termFrequency;
    }

    /**
     * Modifies the term frequency of the term in the document at the URL
     * where this item resides in the index
     * @param termFrequency New term frequency for the term in this document
     */
    public void setTermFrequency(int termFrequency) {
        this.termFrequency = termFrequency;
    }
}
