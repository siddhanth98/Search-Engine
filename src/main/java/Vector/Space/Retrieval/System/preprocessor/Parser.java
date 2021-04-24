package Vector.Space.Retrieval.System.preprocessor;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Parser to utilize JSoup to parse a given document
 * It internally maintains the list of tokens, list of hyperlinks,
 * title of page, meta description and robots exclusion parameters
 * specified in the html.
 * @author Siddhanth Venkateshwaran
 */
public class Parser {
    private final static Tokenizer tokenizer = new Tokenizer();
    private final Document document;
    private String title;
    private String description;
    private final List<String> tokens;
    private final List<String> links;
    private boolean follow, index;

    public Parser(final Document document) {
        this.document = document;
        this.title = "";
        this.description = "";
        this.tokens = new ArrayList<>();
        this.links = new ArrayList<>();
        this.follow = true;
        this.index = true;
    }

    /**
     * Parse the document belonging to this parser
     */
    public void parse() {
        for (Element child : this.document.children()) processNode(child);
        System.out.printf("Finishing parsing document at url %s%n", this.document.baseUri());
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
            if (text.length() > 0) this.tokens.addAll(tokenizer.preprocessTokens(tokenizer.tokenize(text)));
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
        Elements title = head.getElementsByTag("title");
        if (!title.isEmpty()) {
            /* Page title does exist */
            System.out.printf("Found title - %s%n%n", title.get(0).ownText());
            this.setTitle(title.get(0).ownText());
        }
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
                    System.out.printf("Meta description - %s%n%n", content);
                    this.setDescription(content);
                    break;
                }
                case "robots": {
                    System.out.printf("Robots - %s%n%n", content);
                    if (content.contains("none") || content.contains("nofollow"))
                        this.setFollow(false);
                    if (content.contains("none") || content.contains("noindex"))
                        this.setIndex(false);
                    break;
                }
                case "keywords": {
                    String[] words = content.split(",\\s*");
                    System.out.printf("Meta keywords - %s%n%n", Arrays.toString(words));
                    this.tokens.addAll(tokenizer.preprocessTokens(tokenizer.tokenize(content)));
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
        System.out.printf("Link to - %s%nAnchor Text - %s%n%n", link, anchorText);
        this.tokens.addAll(tokenizer.preprocessTokens(tokenizer.tokenize(anchorText)));
        this.links.add(link);
    }

    /**
     * Get title of document
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get meta description of document
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the tokens extracted from the document
     */
    public List<String> getTokens() {
        return tokens;
    }

    /**
     * Get the hyperlinks extracted from the document
     */
    public List<String> getLinks() {
        return links;
    }

    /**
     * Get indicator which indicates whether or not hyperlinks can be followed
     */
    public boolean canFollow() {
        return follow;
    }

    /**
     * Get indicator which indicates whether or not the document can be indexed
     */
    public boolean canIndex() {
        return index;
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
}
