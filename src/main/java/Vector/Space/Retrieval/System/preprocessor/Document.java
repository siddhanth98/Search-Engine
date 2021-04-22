package Vector.Space.Retrieval.System.preprocessor;

/**
 * This class describes a generic structure of a web page(document) to be used
 * during indexing and to display search results
 * @author Siddhanth Venkateshwaran
 */
public class Document {
    private final String url, title, description;

    public Document(final String url, final String title, final String description) {
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
}
