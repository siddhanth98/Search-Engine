package Vector.Space.Retrieval.System.preprocessor;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.util.ContextInitializer;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Parser to utilize JSoup to parse a given document
 * It internally maintains the list of tokens, list of hyperlinks,
 * title of page, meta description and robots exclusion parameters
 * specified in the html.
 * @author Siddhanth Venkateshwaran
 */
public class Parser {
    private final static Tokenizer tokenizer = new Tokenizer();
    private final static StopWordProcessor stopWordProcessor = new StopWordProcessor();
    private final Document document;
    private String title;
    private String description;
    private final List<String> tokens;
    private final Set<String> dictionary;
    private final List<String> links;
    private boolean follow, index;

    private static final Logger logger = LoggerFactory.getLogger(Parser.class);

    public Parser(final Document document) {
        this.document = document;
        this.title = "";
        this.description = "";
        this.tokens = new ArrayList<>();
        this.dictionary = new HashSet<>();
        this.links = new ArrayList<>();
        this.follow = true;
        this.index = true;

        ((ch.qos.logback.classic.Logger)logger).setLevel(Level.OFF);
    }

    /**
     * Parse the document belonging to this parser
     */
    public void parse() {
        for (Element child : this.document.children()) processNode(child);
        logger.info(String.format("Finished parsing document at url %s%n", this.document.baseUri()));
        displayTokens();
    }

    /**
     * This extracts text from the current node (if any) and
     * tokenizes it.
     * @param node Current html element being parsed
     */
    public void processNode(Element node) {
        Set<String> headerTags = Set.of("script", "style", "link");
        if (node.tagName().equals("head")) processHead(node);
        else if (node.tagName().equals("a")) processHyperLink(node);
        else if (!headerTags.contains(node.tagName())) {
            /* Process the node's text here */
            String text = node.ownText().strip();
            if (text.length() > 0) {
                this.tokens.addAll(tokenizer.preprocessTokens(tokenizer.tokenize(text)));
                this.dictionary.addAll(new HashSet<>(stopWordProcessor.eliminateStopWordsFromList(tokenizer.tokenize(text))));
            }
            for (Element child : node.children()) processNode(child);
        }
    }

    /**
     * Processes head(title, meta, etc.) of the html page
     * @param head Head element of html
     */
    public void processHead(Element head) {
        processTitle(head);
        processMeta(head);
    }

    /**
     * This extracts the title of the page
     * @param head Head element of the html page
     */
    public void processTitle(Element head) {
        logger.info(String.format("processing head - %s%n", head.tagName()));
        Elements title = head.getElementsByTag("title");
        if (!title.isEmpty()) {
            /* Page title does exist */
            logger.info(String.format("Found title - %s%n%n", title.get(0).ownText()));
            this.setTitle(title.get(0).ownText());
        }
        else logger.debug(String.format("Could not find a title for document at URL %s", head.baseUri()));
    }

    /**
     * Processes each meta tag in the head element of html page
     * @param head Head element of html page
     */
    public void processMeta(Element head) {
        Elements metaElements = head.getElementsByTag("meta");
        for (Element meta : metaElements) addMetaInformation(meta);
    }

    /**
     * Extracts meta information (description, robots exclusion policy, etc.)
     * from the current meta element being parsed
     * @param meta Current meta element of html page
     */
    public void addMetaInformation(Element meta) {
        if (meta.hasAttr("name")) {
            String content = meta.attr("content");
            switch(meta.attr("name").toLowerCase()) {
                case "description": {
                    logger.info(String.format("Meta description - %s%n%n", content));
                    this.setDescription(content);
                    break;
                }
                case "robots": {
                    logger.info(String.format("Robots - %s%n%n", content));
                    if (content.contains("none") || content.contains("nofollow"))
                        this.setFollow(false);
                    if (content.contains("none") || content.contains("noindex"))
                        this.setIndex(false);
                    break;
                }
                case "keywords": {
                    String[] words = content.split(",\\s*");
                    logger.info(String.format("Meta keywords - %s%n%n", Arrays.toString(words)));
                    this.tokens.addAll(tokenizer.preprocessTokens(tokenizer.tokenize(content)));
                    this.dictionary.addAll(stopWordProcessor.eliminateStopWordsFromList(tokenizer.tokenize(content)));
                    break;
                }
            }
        }
    }

    /**
     * Processes an anchor tag and extracts both hyperlink and anchor text
     * @param node An anchor element of html page
     */
    public void processHyperLink(Element node) {
        String link = node.attr("abs:href"); /* Get absolute link */
        String anchorText = node.ownText();

        if (!link.isEmpty()) {
            logger.info(String.format("Link to - %s%nAnchor Text - %s%n%n", link, anchorText));

            this.tokens.addAll(tokenizer.preprocessTokens(tokenizer.tokenize(anchorText)));
            this.dictionary.addAll(stopWordProcessor.eliminateStopWordsFromList(tokenizer.tokenize(anchorText)));
            this.links.add(link);
        }
    }

    /**
     * Get title of document
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Get meta description of document
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Get the tokens extracted from the document
     */
    public List<String> getTokens() {
        return this.tokens;
    }

    /**
     * Get the unique tokens extracted from the document
     * @return Vocabulary of this document
     */
    public Set<String> getDictionary() {
        return this.dictionary;
    }

    /**
     * Get the hyperlinks extracted from the document
     */
    public List<String> getLinks() {
        return this.links;
    }

    /**
     * Get indicator which indicates whether or not hyperlinks can be followed
     */
    public boolean canFollow() {
        return this.follow;
    }

    /**
     * Get indicator which indicates whether or not the document can be indexed
     */
    public boolean canIndex() {
        return this.index;
    }

    /**
     * Set the title of this document
     * @param title Title of document
     */
    public void setTitle(String title) {
        this.title = title.strip();
    }

    /**
     * Set the meta description of this document
     * @param description Description of document
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set hyperlink-follow meta indicator for this document
     * @param follow <b>true</b> if hyperlinks can be followed and <b>false</b> otherwise
     */
    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    /**
     * Set index meta indicator for this document
     * @param index <b>true</b> if document can be indexed and <b>false</b> otherwise
     */
    public void setIndex(boolean index) {
        this.index = index;
    }

    public void displayTokens() {
        logger.info(String.format("Following tokens were found for document titled: %s%n", this.getTitle()));
        logger.info(String.format("[%s]%n%n", String.join(", ", this.getTokens())));
    }
}
