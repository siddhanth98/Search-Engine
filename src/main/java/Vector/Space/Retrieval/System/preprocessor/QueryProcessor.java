package Vector.Space.Retrieval.System.preprocessor;

import Vector.Space.Retrieval.System.indexer.InvertedIndexer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class QueryProcessor {
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
                                            getWeight(queryTermFrequencyMap.get(currentToken), indexer.getInverseDocumentFrequency(currentToken));

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
        return getRankedList(similarityMap, k);
    }

    private static Map<String, Double> getRankedList(Map<String, Double> queryDocumentSimilarityMap, int k) {
        List<String> rankedList = new ArrayList<>();
        Map<String, Double> retrievedDocumentSimilarityMap = new LinkedHashMap<>();

        queryDocumentSimilarityMap.entrySet()
                .stream()
                .sorted((e1, e2) -> -1 * Double.compare(e1.getValue(), e2.getValue()))
                .forEach(e -> rankedList.add(e.getKey()));
        rankedList.subList(0, k).forEach(doc -> retrievedDocumentSimilarityMap.put(doc, queryDocumentSimilarityMap.get(doc)));
        return retrievedDocumentSimilarityMap;
    }

    private static double getWeight(int termFrequency, double inverseDocumentFrequency) {
        return termFrequency * inverseDocumentFrequency;
    }

    private static Map<String, Integer> getTermFrequencyMap(List<String> tokens) {
        Map<String, Integer> termFrequencyMap = new HashMap<>();
        tokens.forEach(token -> {
            if (termFrequencyMap.containsKey(token)) termFrequencyMap.put(token, termFrequencyMap.get(token)+1);
            else termFrequencyMap.put(token, 1);
        });
        return termFrequencyMap;
    }
}
