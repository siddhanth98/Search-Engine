package Vector.Space.Retrieval.System.preprocessor.crawler;

import Vector.Space.Retrieval.System.indexer.InvertedIndexer;
import Vector.Space.Retrieval.System.preprocessor.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.*;
import java.util.regex.Pattern;

public class Crawler {
    private final Queue<String> urlFrontier;
    private final Set<String> visitedUrls;
    private final Set<String> enqueued;
    private final InvertedIndexer indexer;

    public Crawler(InvertedIndexer indexer) {
        this.indexer = indexer;
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
            Document document = Jsoup.connect(url).get(); /* Fetch the document at this url */
            Parser parser = new Parser(document);
            parser.parse();

            List<String> hyperlinks = parser.getLinks();
            if (parser.canFollow()) enqueueUrls(filter(hyperlinks));
            if (parser.canIndex())
                this.indexer.addToIndex(parser.getTokens(), url, parser.getTitle(), parser.getDescription());

            this.visitedUrls.add(url);
            if (!this.urlFrontier.isEmpty()) crawl(this.dequeueUrl());
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
     */
    public List<String> filter(List<String> links) {
        List<String> filteredLinks = new LinkedList<>();
        for (String link : links) {
            if (link.contains("uic.edu")) {
                String normalizedLink = normalize(link);
                if (!(this.enqueued.contains(normalizedLink) || this.visitedUrls.contains(normalizedLink)))
                    filteredLinks.add(normalizedLink);
            }
        }
        return filteredLinks;
    }

    /**
     * Removes page fragment id (if it exists) and any trailing '/' from the url
     * @param url Url to normalize
     */
    public String normalize(String url) {
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
     */
    public String dequeueUrl() {
        if (this.urlFrontier.isEmpty()) return "";
        String nextUrl = this.urlFrontier.poll();
        this.enqueued.remove(nextUrl);
        return nextUrl;
    }
}
