package Vector.Space.Retrieval.System;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * Name - Siddhanth Venkateshwaran
 * This class will hold all configuration variables needed in the other programs
 */
public class Constants {

    static Config config = ConfigFactory.load(
            ClassLoader.getSystemClassLoader(),
            "configuration/input.conf");

    static String collectionDirectoryName = config.getString("conf.CollectionDirectory");
    static String fileNamePrefix = config.getString("conf.FileNamePrefix");
    static String stopWordsFileName = config.getString("conf.StopWordsFileName");
    static String queriesFileName = config.getString("conf.QueriesFileName");
    static String relevanceFileName = config.getString("conf.RelevanceFileName");

    static int collectionSize = config.getInt("conf.CollectionSize");

    static boolean stem = config.getBoolean("conf.Stem");
    static boolean eliminateStopWords = config.getBoolean("conf.StopWordsElimination");

    static int k = config.getInt("conf.K");
}
