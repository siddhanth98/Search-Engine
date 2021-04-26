package Vector.Space.Retrieval.System;

import Vector.Space.Retrieval.System.indexer.InvertedIndexer;
import Vector.Space.Retrieval.System.preprocessor.*;
import Vector.Space.Retrieval.System.preprocessor.crawler.Crawler;
import Vector.Space.Retrieval.System.query.QueryProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static Vector.Space.Retrieval.System.Constants.*;
import static Vector.Space.Retrieval.System.Constants.k;

/**
 * This is the main class to run
 * @author Siddhanth Venkateshwaran
 */
public class App {
    public static void main(String[] args) {
        /*InvertedIndexer indexer = new InvertedIndexer(collectionDirectoryName, collectionSize, fileNamePrefix);
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
                List<String> rankedList = new ArrayList<>(rankedMap.keySet());
                System.out.println("[".concat(String.join(", ", rankedList)).concat("]"));
                System.out.printf("Precision = %f%%, Recall = %f%%%n",
                        100*QueryProcessor.getPrecision(rankedMap.keySet(), i+1, relevanceFileName),
                        100*QueryProcessor.getRecall(rankedMap.keySet(), i+1, relevanceFileName));
                System.out.println("\n");
                rankedMapForAllQueries.add(rankedMap);
                rankedList.clear();
            }
        }
        catch(Exception e) {
            System.out.println("exception while computing query-document similarities");
            e.printStackTrace();
        }
        displayAverageMetrics(rankedMapForAllQueries, 10);
        displayAverageMetrics(rankedMapForAllQueries, 50);
        displayAverageMetrics(rankedMapForAllQueries, 100);
        displayAverageMetrics(rankedMapForAllQueries, 500);*/


        Crawler crawler = new Crawler(2);
        crawler.crawl("https://www.html.am/");
    }

    public static void displayAverageMetrics(List<Map<String, Double>> rankedMapForAllQueries, int k) {
        System.out.printf("****************************************** K = %d ***************************************%n", k);
        System.out.printf("Average precision = %f%%%n", 100*QueryProcessor.getAveragePrecision(rankedMapForAllQueries, relevanceFileName, k));
        System.out.printf("Average Recall = %f%%%n", 100*QueryProcessor.getAverageRecall(rankedMapForAllQueries, relevanceFileName, k));
        System.out.println("*****************************************************************************************\n");
    }
}
