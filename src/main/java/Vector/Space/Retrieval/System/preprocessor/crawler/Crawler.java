package Vector.Space.Retrieval.System.preprocessor.crawler;

import Vector.Space.Retrieval.System.Constants;
import Vector.Space.Retrieval.System.indexer.InvertedIndexer;
import Vector.Space.Retrieval.System.preprocessor.IndexItem;
import Vector.Space.Retrieval.System.preprocessor.Parser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Crawler {
    private final Queue<String> urlFrontier;
    private final Set<String> visitedUrls;
    private final Set<String> enqueued;
    private final Map<String, String> redirectMap;
    private final InvertedIndexer indexer;
    private final int limit;
    private int crawlCount;

    private static final Logger logger = LoggerFactory.getLogger(Crawler.class);

    public Crawler(final int limit) {
        this.limit = limit;
        this.crawlCount = 1;
        this.indexer = new InvertedIndexer();
        this.urlFrontier = new LinkedList<>();
        this.visitedUrls = new HashSet<>();
        this.enqueued = new HashSet<>();
        this.redirectMap = new HashMap<>();

//        ((ch.qos.logback.classic.Logger)logger).setLevel(Level.OFF);
    }

    public void init(String seedUrl) {
        this.urlFrontier.add(seedUrl);
        while(!this.urlFrontier.isEmpty() && this.crawlCount <= this.limit)
            crawl(this.dequeueUrl());
        finishCrawl();
    }

    /**
     * Fetches, parses the page and extracts the hyperlinks in the given document
     * <br>
     * Stops when there are no links in the frontier left to crawl
     * @param url URL to be crawled next
     */
    public void crawl(String url) {
        try {
            String crawlUrl = getNormalized(url);
            Document document = connectAndFetch(crawlUrl);
            String redirectedUrl = getNormalized(document.baseUri());
            if (!(crawled(crawlUrl) || crawled(redirectedUrl))) {
                /* Crawl this document */
                logger.info(String.format("crawling url-%d %s", this.crawlCount, document.baseUri()));
                Parser parser = new Parser(document);
                parser.parse();
                List<String> hyperlinks = parser.getLinks();
                if (parser.canFollow()) enqueueUrls(getFiltered(hyperlinks, crawlUrl, redirectedUrl));
                if (parser.canIndex()) {
                    this.indexer.addToIndex(parser.getTokens(), crawlUrl, parser.getTitle(), parser.getDescription());
                    this.indexer.setCollectionSize(this.indexer.getCollectionSize() + 1); /* increment number of indexed documents */
                }

                /* this url has been crawled. add to visited set */
                markCrawled(crawlUrl, redirectedUrl);
                this.crawlCount++;
            }
        }
        catch(Exception e) {}
    }

    /**
     * Filter out links by removing urls which are already crawled
     * or are already enqueued in the url frontier,
     * and remove links if they do not belong to the <i>uic.edu</i> domain
     * @param links List of urls extracted from the document
     * @return list of urls not already crawled and not already in the URL frontier
     */
    public List<String> getFiltered(List<String> links, String originalUrl, String redirectedUrl) {
        List<String> filteredLinks = new LinkedList<>();
        Set<String> collectedLinks = new HashSet<>();

        for (String link : links) {
            String normalizedLink = getNormalized(link);
            if (normalizedLink.contains("uic.edu") && !collectedLinks.contains(normalizedLink) &&
                    !(normalizedLink.equals(originalUrl) || normalizedLink.equals(redirectedUrl))) {
                if (isValid(normalizedLink)) {
//                    logger.info(String.format("adding hyperlink %s", normalizedLink));
                    filteredLinks.add(normalizedLink);
                }
                collectedLinks.add(normalizedLink);
            }
        }
//        logger.info("\n\n");
        return filteredLinks;
    }

    /**
     * Checks whether or not the url is already enqueued, crawled and has proper protocols
     * @param url URL being examined
     * @return a boolean indicating if URL is valid to be crawled or not
     */
    public boolean isValid(String url) {
        return (
                !(this.enqueued.contains(url) || crawled(url)) &&
                (url.contains("http"))
        );
    }

    public Document connectAndFetch(String url) throws IOException {
        Connection.Response response =
                Jsoup.connect(url.concat("/"))
                        .timeout(10000)
                        .validateTLSCertificates(false)
                        .followRedirects(true).execute();

        return response.parse();
    }

    /**
     * Marks the url (and its redirected url if it exists) as crawled
     * @param url URL most recently crawled
     */
    public void markCrawled(String url, String redirectedUrl) {
        this.visitedUrls.add(url);

        if (!url.equals(redirectedUrl)) {
            this.visitedUrls.add(redirectedUrl);
            this.redirectMap.put(url, redirectedUrl);
        }
    }

    /**
     * Checks whether or not this url (or its redirected url) has already been crawled
     * @param url Current URL being checked for past crawl
     */
    public boolean crawled(String url) {
        String redirectedUrl = this.redirectMap.getOrDefault(url, url);
        return (this.visitedUrls.contains(url) || this.visitedUrls.contains(redirectedUrl));
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

        return absoluteUrl;
    }

    /**
     * Constructs the euclidean normalized document length table if all documents are crawled
     */
    public void finishCrawl() {
        if (this.urlFrontier.isEmpty() || this.crawlCount >= this.limit) {
            this.indexer.constructDocumentVectorTable();
            this.writeObjectToFile(this.getIndexer().getIndex(), "index");
            this.writeObjectToFile(this.getIndexer().getDocumentVector(), "docLengths");
        }
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

    /**
     * Get the indexer used by this crawler
     * @return Inverted index used for this collection
     */
    public InvertedIndexer getIndexer() {
        return this.indexer;
    }

    public void readIndex() {
        try {
            FileInputStream fileInputStream = new FileInputStream(Constants.indexFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object obj = objectInputStream.readObject();
            this.getIndexer().setIndex((HashMap)obj);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void readDocLengths() {
        try {
            FileInputStream fileInputStream = new FileInputStream(Constants.docLengthsFile);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Object obj = objectInputStream.readObject();
            this.getIndexer().setDocumentVector((HashMap)obj);
            this.getIndexer().setCollectionSize(((HashMap)(obj)).size());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Stores index/(euclidean-normalized-document-vector) to disk
     * @param obj Inverted index / Document lengths vector
     * @param fileName Name of the file on disk
     */
    public void writeObjectToFile(Object obj, String fileName) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(String.format("src/main/resources/%s.ser", fileName));
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.close();
            fileOutputStream.close();
        }
        catch(IOException io) {
            io.printStackTrace();
        }
    }
}
