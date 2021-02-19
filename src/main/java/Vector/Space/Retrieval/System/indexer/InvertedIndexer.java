package Vector.Space.Retrieval.System.indexer;

import Vector.Space.Retrieval.System.preprocessor.DocumentParser;
import Vector.Space.Retrieval.System.preprocessor.Tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static Vector.Space.Retrieval.System.DocumentUtils.constructDocumentName;

/**
 * Siddhanth Venkateshwaran - This class will build and store the inverted index for the cranfield collection
 */
public class InvertedIndexer {
    private final String directory;
    private final String fileNamePrefix;
    private final int collectionSize;
    private final Map<String, Map<String, Integer>> index;
    private Map<String, Double> documentVector;

    public InvertedIndexer(String directory, int collectionSize, String fileNamePrefix) {
        this.directory = directory;
        this.collectionSize = collectionSize;
        this.fileNamePrefix = fileNamePrefix;
        index = new HashMap<>();
    }

    /**
     * Constructs the inverted index for the collection using the given tokenizer instance
     * @param tokenizer Tokenizer instance which will be used to tokenize the text from the documents
     */
    public void constructInvertedIndex(Tokenizer tokenizer) {
        DocumentParser documentParser = new DocumentParser();

        try {
            List<String> preprocessedTokens = new ArrayList<>();
            for (int documentIndex = 1; documentIndex <= this.collectionSize; documentIndex++) {
                List<String> currentDocumentLines = documentParser.parseAndRetrieveText(documentIndex, this.directory, this.fileNamePrefix);
                List<List<String>> tokens = currentDocumentLines.stream().map(tokenizer::tokenize).collect(Collectors.toList());
                tokens.forEach(tokenList -> preprocessedTokens.addAll(tokenizer.preprocessTokens(tokenList)));
                insertTokensInIndex(preprocessedTokens, documentIndex);
                preprocessedTokens.clear();
            }
            constructDocumentVectorTable();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This function is used by the index builder to insert the tokens for the current document being processed
     * @param tokens List of tokens extracted from the text of the current document being processed
     * @param documentIndex ID of current document
     */
    private void insertTokensInIndex(List<String> tokens, int documentIndex) {
        String documentName = String.format("%s%s", this.fileNamePrefix, constructDocumentName(documentIndex));
        tokens.forEach(token -> {
            this.index.putIfAbsent(token, new HashMap<>());
            if (this.index.get(token).containsKey(documentName))
                this.index.get(token).put(documentName, this.index.get(token).get(documentName)+1);
            else this.index.get(token).put(documentName, 1);
        });
    }

    /**
     * This returns the inverted index of the current indexer instance
     */
    public Map<String, Map<String, Integer>> getIndex() {
        return this.index;
    }

    /**
     * This function will build the map of documentID -> euclidean normalized length of document vector
     * This map is used while computing the similarity between a document and a query
     */
    private void constructDocumentVectorTable() {
        this.documentVector = new HashMap<>();
        this.index.keySet().forEach(token ->
            this.index.get(token).forEach((document, termFrequency) -> {
                try {
                    double currentTokenWeight = Math.pow(getWeight(token, document), 2);
                    if (this.documentVector.containsKey(document))
                        this.documentVector.put(document, this.documentVector.get(document)+currentTokenWeight);
                    else
                        this.documentVector.put(document, currentTokenWeight);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }));
    }

    /**
     * Computes and returns the weight of a token for a given document
     * @param token Token whose weight is to be computed
     * @param document Target document for the token
     * @return Weight of token for the target document i.e. TF(token, document) * IDF(token)
     */
    public double getWeight(String token, String document) throws Exception {
        return getTermFrequency(token, document) * getInverseDocumentFrequency(token);
    }

    /**
     * This computes and returns the number of documents in which the given token appears
     * @return number of documents in which token appears
     */
    public int getDocumentFrequency(String token) {
        if (this.index.containsKey(token)) return this.index.get(token).keySet().size();
        return 0;
    }

    /**
     * This computes and returns the number of occurrences of a token in a given document
     * @return number of times a token appears in the title and text of the document
     */
    public int getTermFrequency(String token, String document) {
        if (this.index.containsKey(token) && this.index.get(token).containsKey(document)) return this.index.get(token).get(document);
        return 0;
    }

    /**
     * This computes and returns the IDF of a given token
     * @return IDF(token) = log10 ( collectionSize / documentFrequency(token) )
     */
    public double getInverseDocumentFrequency(String token) throws Exception {
        if (this.index.containsKey(token)) return Math.log10((double)(this.collectionSize) / (double)(getDocumentFrequency(token)));
        throw new Exception();
    }

    /**
     * Constructs and returns the stringified representation of the postings list of each token in the inverted index
     * @return stringified inverted index
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        this.index.keySet().forEach(token -> {
            result.append(String.format("%s\t\t\t\t\t\t\t\t\t\t\t\t", token));
            this.index.get(token).forEach((document, termFrequency) -> result.append(String.format("%s -> %d\t\t", document, termFrequency)));
            result.append("\n");
        });
        return result.toString();
    }

    /**
     * Prints the euclidean normalized document vector of all documents in the index
     */
    public void printDocumentVector() {
        this.documentVector.keySet().forEach(document -> System.out.printf("%s -> %s%n", document, this.documentVector.get(document)));
    }
}
