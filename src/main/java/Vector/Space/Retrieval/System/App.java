package Vector.Space.Retrieval.System;

import Vector.Space.Retrieval.System.indexer.InvertedIndexer;
import Vector.Space.Retrieval.System.preprocessor.*;

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

        indexer.constructInvertedIndex(tokenizer);
        System.out.println(indexer.toString());
        indexer.printDocumentVector();
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
