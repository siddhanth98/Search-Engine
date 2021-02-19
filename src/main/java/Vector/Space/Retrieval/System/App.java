package Vector.Space.Retrieval.System;

import Vector.Space.Retrieval.System.indexer.InvertedIndexer;
import Vector.Space.Retrieval.System.preprocessor.*;

import java.util.ArrayList;
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
        List<Map<String, Double>> rankedMapForAllQueries = new ArrayList<>();
        indexer.constructInvertedIndex(tokenizer);

        try {
            for (int i = 0; i < queriesTokens.size(); i++) {
                Map<String, Double> rankedMap = QueryProcessor.getRankedMapOfDocuments(indexer, indexer.getIndex(), queriesTokens.get(i), k);
                System.out.printf("Query%d: %s%n", i+1, queries.get(i));
                for (String document : rankedMap.keySet()) System.out.printf("(Query%d, %s)%n", i+1, document);
                System.out.println("\n");
                rankedMapForAllQueries.add(rankedMap);
            }
        }
        catch(Exception e) {
            System.out.println("exception while computing query-document similarities");
            e.printStackTrace();
        }
        System.out.printf("Average precision = %f%%%n", 100*QueryProcessor.getAveragePrecision(rankedMapForAllQueries, relevanceFileName));
        System.out.printf("Average Recall = %f%%%n", 100*QueryProcessor.getAverageRecall(rankedMapForAllQueries, relevanceFileName));
    }
}
