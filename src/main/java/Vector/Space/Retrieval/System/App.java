package Vector.Space.Retrieval.System;

import Vector.Space.Retrieval.System.query.QueryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static Vector.Space.Retrieval.System.Constants.*;

/**
 * This is the main class to run
 * @author Siddhanth Venkateshwaran
 */
public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    public static void main(String[] args) {
        try {
            new Server().start();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void displayAverageMetrics(List<Map<String, Double>> rankedMapForAllQueries, int k) {
        System.out.printf("****************************************** K = %d ***************************************%n", k);
        System.out.printf("Average precision = %f%%%n", 100*QueryProcessor.getAveragePrecision(rankedMapForAllQueries, relevanceFileName, k));
        System.out.printf("Average Recall = %f%%%n", 100*QueryProcessor.getAverageRecall(rankedMapForAllQueries, relevanceFileName, k));
        System.out.println("*****************************************************************************************\n");
    }
}
