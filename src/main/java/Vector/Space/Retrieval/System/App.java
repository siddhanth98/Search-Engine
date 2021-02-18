package Vector.Space.Retrieval.System;

import Vector.Space.Retrieval.System.indexer.InvertedIndexer;
import Vector.Space.Retrieval.System.preprocessor.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static Vector.Space.Retrieval.System.Constants.*;

/**
 * Name - Siddhanth Venkateshwaran
 * This is the main class to run
 */
public class App {
    public static void main(String[] args) {
        InvertedIndexer indexer = new InvertedIndexer(collectionDirectoryName, collectionSize, fileNamePrefix);
        StopWordProcessor stopWordProcessor = new StopWordProcessor(stopWordsFileName);
        Tokenizer tokenizer = new Tokenizer(stem, eliminateStopWords, stopWordProcessor);
        List<List<String>> queriesTokens = QueryProcessor.parseQueriesAndGetTokens(queriesFileName, tokenizer);
        List<String> queries = QueryProcessor.parseAndGetQueries(queriesFileName);

        indexer.constructInvertedIndex(tokenizer);

        try {
            for (int i = 0; i < queriesTokens.size(); i++) {
                Map<String, Double> rankedMap = QueryProcessor.getRankedMapOfDocuments(indexer, indexer.getIndex(), queriesTokens.get(i), k);
                System.out.printf("Query: %s%nRanked list of (documents, similarity) => %s%n%n", queries.get(i), rankedMap.toString());
            }
        }
        catch(Exception e) {
            System.out.println("exception while computing query-document similarities");
            e.printStackTrace();
        }
    }

    public static int getMinimumNumberOfUniqueWords(final List<String> rankedListOfTokens,
                                                    final Map<String, Integer> frequencyMap,
                                                    final List<String> allTokens,
                                                    double minPercentage) {
        int minNumberOfWords = 0, totalNumberOfWords = allTokens.size(), totalWordCount = 0;

        for (String rankedListOfToken : rankedListOfTokens) {
            minNumberOfWords++;
            totalWordCount += frequencyMap.get(rankedListOfToken);
            if (totalWordCount >= (minPercentage * totalNumberOfWords)) break;
        }
        return minNumberOfWords;
    }
}
