package Vector.Space.Retrieval.System.query;

import Vector.Space.Retrieval.System.indexer.InvertedIndexer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import Vector.Space.Retrieval.System.DocumentUtils;
import Vector.Space.Retrieval.System.preprocessor.Tokenizer;

/**
 * This class will parse and tokenize the queries using the given tokenizer instance,
 * compute similarity between a query and a document and compute the average precision and
 * recall of all retrieved documents
 * @author Siddhanth Venkateshwaran
 */
public class QueryProcessor {

    /**
     * Returns the list of all queries as plain texts
     * @param filename Name of the top-level file containing all queries
     * @return list of plain texts of all queries
     */
    public static List<String> parseAndGetQueries(String filename) {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(
                                QueryProcessor.class.getClassLoader().getResourceAsStream(filename))));
        List<String> queries = new ArrayList<>();
        String line;
        try {
            while((line = br.readLine()) != null) queries.add(line);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return queries;
    }

    /**
     * Tokenizes all queries and returns a list of list of all tokens corresponding to each query
     * @param filename Name of the file containing all queries
     * @param tokenizer Instance of tokenizer to use to tokenize the query - should be the same tokenizer instance used by
     *                  the indexer
     * @return List of list of all tokens of all queries
     */
    public static List<List<String>> parseQueriesAndGetTokens(String filename, Tokenizer tokenizer) {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(
                                QueryProcessor.class.getClassLoader().getResourceAsStream(filename))));
        String line;
        List<List<String>> tokens = new ArrayList<>();

        try {
            while ((line = br.readLine()) != null) {
                tokens.add(new ArrayList<>());
                tokens.get(tokens.size()-1).addAll(tokenizer.tokenize(line));
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return tokens.stream().map(tokenizer::preprocessTokens).collect(Collectors.toList());
    }

    public static List<String> getTokens(String query, Tokenizer tokenizer) {
        return tokenizer.preprocessTokens(tokenizer.tokenize(query));
    }

    /**
     * Computes the similarity of a query with all documents in the collection that have at least 1 token appearing in the query
     * @param indexer Instance of inverted indexer
     * @param invertedIndex Inverted index of the target collection
     * @param queryTokens List of all tokens for the given query
     * @param k number of most similar documents to be retrieved
     * @return unordered map of document -> similarity value
     */
    public static Map<String, Double> getRankedMapOfDocuments(InvertedIndexer indexer, Map<String, Map<String, Integer>> invertedIndex,
                                                              List<String> queryTokens, int k) {
        Map<String, Double> similarityMap = new HashMap<>();
        Map<String, Integer> queryTermFrequencyMap = getTermFrequencyMap(queryTokens);

        queryTokens.forEach(currentToken -> {
            if (invertedIndex.containsKey(currentToken))
                    invertedIndex.get(currentToken).forEach((currentDocument, termFrequency) -> {
                        try {
                            double currentSimilarityValue =
                                    indexer.getWeight(currentToken, currentDocument) *
                                            getWeight(queryTermFrequencyMap.get(currentToken),
                                                    indexer.getInverseDocumentFrequency(currentToken));

                            if (!similarityMap.containsKey(currentDocument))
                                similarityMap.put(currentDocument, currentSimilarityValue);
                            else
                                similarityMap.put(currentDocument, similarityMap.get(currentDocument) + currentSimilarityValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
        );
        return getRankedMap(similarityMap);
    }

    /**
     * Computes the reverse-sorted map of retrieved documents in terms of similarity values
     * @param queryDocumentSimilarityMap Unordered Map of document -> similarity values
     * @return map of document -> similarity value (ordered in non-increasing order of cosine similarity value)
     */
    private static Map<String, Double> getRankedMap(Map<String, Double> queryDocumentSimilarityMap) {
        List<String> rankedList = new ArrayList<>();
        Map<String, Double> retrievedDocumentSimilarityMap = new LinkedHashMap<>();

        queryDocumentSimilarityMap.entrySet()
                .stream()
                .sorted((e1, e2) -> -1 * Double.compare(e1.getValue(), e2.getValue()))
                .forEach(e -> rankedList.add(e.getKey()));
        rankedList.forEach(doc -> retrievedDocumentSimilarityMap.put(doc, queryDocumentSimilarityMap.get(doc)));
        return retrievedDocumentSimilarityMap;
    }

    /**
     * Computes the weight of a given token for the given query
     * @param termFrequency number of occurrences of token in the query
     * @param inverseDocumentFrequency IDF of the given token
     * @return weight = TF(token, query) * IDF(token)
     */
    private static double getWeight(int termFrequency, double inverseDocumentFrequency) {
        return termFrequency * inverseDocumentFrequency;
    }

    /**
     * Computes a map of token -> number of occurrences of token in given query
     * @param tokens List of all tokens in the query
     * @return Map of token -> token count in query
     */
    private static Map<String, Integer> getTermFrequencyMap(List<String> tokens) {
        Map<String, Integer> termFrequencyMap = new HashMap<>();
        tokens.forEach(token -> {
            if (!termFrequencyMap.containsKey(token)) termFrequencyMap.put(token, 0);
            termFrequencyMap.put(token, termFrequencyMap.get(token)+1);
        });
        return termFrequencyMap;
    }

    /**
     * Computes the average precision of retrieved documents for all queries using a manually assessed relevance file
     * @param globalRankedList global list of (ranked maps of documents to similarity values) for all queries
     * @param relevanceFileName name of the top-level file having manual relevance judgements
     * @return average precision computed over all queries
     */
    public static double getAveragePrecision(List<Map<String, Double>> globalRankedList, String relevanceFileName, int k) {
        double averagePrecision = 0.0D;
        for (int i = 0; i < globalRankedList.size(); i++)
            averagePrecision += getPrecision(getTopKDocuments(globalRankedList.get(i).keySet(), k), i+1, relevanceFileName);
        return averagePrecision / globalRankedList.size();
    }

    /**
     * Computes the average recall of retrieved documents for all queries using a manually assessed relevance file
     * @param globalRankedList global list of (ranked maps of documents to similarity values) for all queries
     * @param relevanceFileName name of the top-level file having manual relevance judgements
     * @return average recall computed over all queries
     */
    public static double getAverageRecall(List<Map<String, Double>> globalRankedList, String relevanceFileName, int k) {
        double averageRecall = 0.0D;
        for (int i = 0; i < globalRankedList.size(); i++)
            averageRecall += getRecall(getTopKDocuments(globalRankedList.get(i).keySet(), k), i+1, relevanceFileName);
        return averageRecall / globalRankedList.size();
    }

    /**
     * Extracts the top k documents from the ranked list retrieved for the given query
     * @param documents The ranked list of documents in the form of a set
     * @param k number of documents to retrieve
     * @return set of top k documents by cosine similarity
     */
    public static Set<String> getTopKDocuments(Set<String> documents, int k) {
        Set<String> topKDocuments = new HashSet<>();
        int count = 0;

        for (String document : documents) {
            if (count == k) break;
            topKDocuments.add(document);
            count++;
        }
        return topKDocuments;
    }

    /**
     * Computes precision of all documents retrieved using manually assessed relevance judgements for a given query
     * @param retrievedDocuments set of names of all documents retrieved for the given query
     * @param queryId ID of the given query
     * @param relevanceFileName name of file having manually assessed relevance judgements
     * @return precision computed for the given query = (number of relevant documents retrieved / total number of documents retrieved)
     */
    public static double getPrecision(Set<String> retrievedDocuments, int queryId, String relevanceFileName) {
        return
                (double)(getNumberOfRelevantDocumentsRetrieved(retrievedDocuments, queryId, relevanceFileName)) /
                        (double)(retrievedDocuments.size());
    }

    /**
     * Computes recall of all documents retrieved using manually assessed relevance judgements for a given query
     * @param retrievedDocuments set of names of all documents retrieved for the given query
     * @param queryId ID of the given query
     * @param relevanceFileName name of file having manually assessed relevance judgements
     * @return recall computed for the given query = (number of relevant documents retrieved / total number of relevant documents)
     */
    public static double getRecall(Set<String> retrievedDocuments, int queryId, String relevanceFileName) {
        Set<String> relevantDocuments = getActualRelevantDocuments(queryId, relevanceFileName);
        return
                (double)(getNumberOfRelevantDocumentsRetrieved(retrievedDocuments, queryId, relevanceFileName)) /
                        (double)(relevantDocuments.size());
    }

    /**
     * Computes the number of relevant documents which actually have been retrieved by the system for a given query
     * @param retrievedDocuments set of names of all documents which have been retrieved
     * @param queryId ID of the current query
     * @param filename name of the file having manually assessed relevance judgements
     * @return count of relevant documents from the retrieved set
     */
    public static int getNumberOfRelevantDocumentsRetrieved(Set<String> retrievedDocuments, int queryId, String filename) {
        Set<String> actualRelevantDocuments = getActualRelevantDocuments(queryId, filename);
        int count = 0;
        for (String document : retrievedDocuments) {
            if (actualRelevantDocuments.contains(document)) count++;
        }
        return count;
    }

    /**
     * Get the set of all relevant documentIDs from a set of manually assessed relevance judgements
     * @param queryId ID of the current query
     * @param filename name of the file having manually assessed relevance judgements
     * @return set of names of all documents which are actually relevant as per manual assessment
     */
    public static Set<String> getActualRelevantDocuments(int queryId, String filename) {
        Set<String> relevantDocuments = new HashSet<>();
        BufferedReader br =
                new BufferedReader(new InputStreamReader(Objects.requireNonNull(
                        QueryProcessor.class.getClassLoader().getResourceAsStream(filename))));
        String line;
        try {
            while ((line = br.readLine()) != null) {
                int currentQueryId = Integer.parseInt(line.split(" ")[0]);
                int currentDocumentIndex = Integer.parseInt(line.split(" ")[1]);

                if (currentQueryId == queryId) {
                    relevantDocuments.add("cranfield".concat(DocumentUtils.constructDocumentName(currentDocumentIndex)));
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return relevantDocuments;
    }
}
