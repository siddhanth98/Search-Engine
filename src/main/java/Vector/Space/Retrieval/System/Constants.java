package Vector.Space.Retrieval.System;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * This class will hold all configuration variables needed in the other programs
 * @author Siddhanth Venkateshwaran
 */
public class Constants {

    static Config config = ConfigFactory.load(
            ClassLoader.getSystemClassLoader(),
            "configuration/input.conf");

    static String collectionDirectoryName = config.getString("conf.CollectionDirectory");
    static String fileNamePrefix = config.getString("conf.FileNamePrefix");
    public static String stopWordsFileName = config.getString("conf.StopWordsFileName");
    static String queriesFileName = config.getString("conf.QueriesFileName");
    static String relevanceFileName = config.getString("conf.RelevanceFileName");
    public static String indexFile = config.getString("conf.IndexFile");
    public static String docLengthsFile = config.getString("conf.DocumentLengthsFile");

    static int collectionSize = config.getInt("conf.CollectionSize");

    public static boolean stem = config.getBoolean("conf.Stem");
    public static boolean eliminateStopWords = config.getBoolean("conf.StopWordsElimination");
    public static String scoring = config.getString("conf.scoring");

    public static int k = config.getInt("conf.K");

    public static int crawlLimit = config.getInt("conf.CrawlLimit");
    public static String seedUrl = config.getString("conf.SeedUrl");
    public static String address = config.getString("conf.Host");
    public static int port = config.getInt("conf.Port");
    public static boolean indexing = config.getBoolean("conf.Indexing");

}
