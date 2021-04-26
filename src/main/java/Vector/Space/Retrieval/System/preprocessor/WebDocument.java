package Vector.Space.Retrieval.System.preprocessor;

import java.io.Serializable;

/**
 * This class describes a generic structure of a web page(document) to be used
 * during indexing and to display search results
 * @author Siddhanth Venkateshwaran
 */
public class WebDocument implements Serializable {
    private String title, description;
    private final String url;

    public WebDocument(final String url) {
        this(url, "", "");
    }

    public WebDocument(final String url, final String title, final String description) {
        this.url = url;
        this.title = title;
        this.description = description;
    }

    /**
     * Gets the fully qualified url of this document
     * @return Fully qualified URL string
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Gets the title of the document to display as the search result
     * @return Title of the document
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets a short description of the document which is extracted
     * from the meta tag of the document's html page
     * @return Short description of document to display below title
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Modifies the title of this document
     * @param title New title of this document
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Modifies the meta description of this document
     * @param description New description of this document
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
