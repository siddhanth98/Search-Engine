package Vector.Space.Retrieval.System.preprocessor.crawler;

import ch.qos.logback.classic.util.ContextInitializer;

import Vector.Space.Retrieval.System.indexer.InvertedIndexer;
import Vector.Space.Retrieval.System.preprocessor.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class Crawler {
    private final Queue<String> urlFrontier;
    private final Set<String> visitedUrls;
    private final Set<String> enqueued;
    private final InvertedIndexer indexer;
    private final int limit;
    private int crawlCount;

    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);

    public Crawler(final int limit) {
        System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "src/main/resources/configuration/logback-test.xml");
        this.limit = limit;
        this.crawlCount = 0;
        this.indexer = new InvertedIndexer();
        this.urlFrontier = new LinkedList<>();
        this.visitedUrls = new HashSet<>();
        this.enqueued = new HashSet<>();
    }

    /**
     * Fetches, parses the page and extracts the hyperlinks in the given document
     * <br>
     * Stops when there are no links in the frontier left to crawl
     * @param url URL to be crawled next
     */
    public void crawl(String url) {
        try {
            logger.info(String.format("crawling url %s%n", url));
            Document document = Jsoup.connect(url).get(); /* Fetch the document at this url */
            Parser parser = new Parser(document);
            parser.parse();

            this.visitedUrls.add(getNormalized(url));
            List<String> hyperlinks = parser.getLinks();
            if (parser.canFollow()) enqueueUrls(getFiltered(hyperlinks));
            if (parser.canIndex()) {
                this.indexer.addToIndex(parser.getTokens(), url, parser.getTitle(), parser.getDescription());
                this.indexer.setCollectionSize(this.indexer.getCollectionSize()+1); /* increment number of indexed documents */
            }

            this.crawlCount++;

            if (!this.urlFrontier.isEmpty() && this.crawlCount <= this.limit) crawl(this.dequeueUrl());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Filter out links by removing urls which are already crawled
     * or are already enqueued in the url frontier,
     * and remove links if they do not belong to the <i>uic.edu</i> domain
     * @param links List of urls extracted from the document
     * @return list of urls not already crawled and not already in the URL frontier
     */
    public List<String> getFiltered(List<String> links) {
        List<String> filteredLinks = new LinkedList<>();
        for (String link : links) {
            if (/*link.contains("uic.edu")*/true) { /* this condition needs to be restored later on */
                String normalizedLink = getNormalized(link);
                if (!(this.enqueued.contains(normalizedLink) || this.visitedUrls.contains(normalizedLink))) {
                    filteredLinks.add(normalizedLink);
                }
            }
        }
        return filteredLinks;
    }

    /**
     * Removes page fragment id (if it exists) and any trailing '/' from the url
     * @param url Url to normalize
     * @return absolute url with any page fragment and trailing '/' removed
     */
    public String getNormalized(String url) {
        String absoluteUrl = url;
        try {
            if (Pattern.matches("^.*#.+$", url)) {
                /* Url has a page fragment identifier */
                int fragmentStartIndex = absoluteUrl.lastIndexOf("#");
                absoluteUrl = absoluteUrl.substring(0, fragmentStartIndex);
            }
            int trailingSlashIndex = absoluteUrl.lastIndexOf("/");
            if (trailingSlashIndex == absoluteUrl.length() - 1) /* url ends with a '/' */
                absoluteUrl = absoluteUrl.substring(0, absoluteUrl.length() - 1);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        logger.info(String.format("URL - %s => Normalized URL - %s%n%n", url, absoluteUrl));
        return absoluteUrl;
    }

    /**
     * Adds each extracted hyperlink to the url frontier and the set of enqueued urls
     * @param urls List of hyperlinks extracted from document
     */
    public void enqueueUrls(List<String> urls) {
        for (String url : urls) {
            this.urlFrontier.add(url);
            this.enqueued.add(url);
        }
    }

    /**
     * Pops and returns the url at the front of the url frontier
     * <br>
     * and also removes it from the set of enqueued urls
     * @return next url to crawl
     */
    public String dequeueUrl() {
        String nextUrl = this.urlFrontier.poll();
        this.enqueued.remove(nextUrl);
        return nextUrl;
    }
}
