package Vector.Space.Retrieval.System.preprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

import static Vector.Space.Retrieval.System.DocumentUtils.constructDocumentName;

/**
 * This class will parse the collection to extract document and tokenize the text
 * @author - Siddhanth Venkateshwaran
 * */
public class DocumentParser {

    /**
     * This method will parse the entire collection and create a global list of all valid lines of text
     * found in all documents in the collection
     * @param directory name of the directory having the collection
     * @param fileNamePrefix prefix of the document name common to all documents
     * @return global list of all valid lines of text found in the collection
     */
    public List<String> parseAndRetrieveText(String directory, String fileNamePrefix) {
        List<String> result = new ArrayList<>();
        int documentIndex = 1;

        while(documentIndex < 2) {
            try {
                BufferedReader document = new BufferedReader(
                        new InputStreamReader(
                                Objects.requireNonNull(
                                        DocumentParser.class.getClassLoader()
                                                .getResourceAsStream(
                                                        String.format("%s/%s%s", directory, fileNamePrefix,
                                                                constructDocumentName(documentIndex++))
                                                )
                                )
                        )
                );
                List<String> currentDocumentLines = parseDocument(document);

                result.addAll(currentDocumentLines);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * This method will parse and retrieve valid text from a given document
     * @param documentIndex ID of the current document being processed
     * @param directory name of the directory having the collection
     * @param fileNamePrefix common prefix of all document names
     * @return list of all valid lines of text found in the current document
     */
    public List<String> parseAndRetrieveText(int documentIndex, String directory, String fileNamePrefix)
            throws IOException {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(
                                DocumentParser.class.getClassLoader().getResourceAsStream(String.format("%s/%s%s", directory, fileNamePrefix,
                                        constructDocumentName(documentIndex))
                                )
                        )
                )
        );
        return parseDocument(br);
    }

    /**
     * This method specifically parses and extracts the title and text sections of the
     * cranfield collection
     * @param br file pointer for the given document
     * @return list of all lines in the title and text sections in the current document
     */
    private List<String> parseDocument(BufferedReader br) throws IOException {
        String tagStart = "<[A-Z]+>";
        String line;
        List<String> documentLines = new ArrayList<>();
        Set<String> requiredTags = new HashSet<>();
        requiredTags.add("title");
        requiredTags.add("text");

        while((line = br.readLine()) != null) {
            if (Pattern.matches(tagStart, line) && requiredTags.contains(line.substring(1, line.length()-1).toLowerCase())) {
                documentLines.addAll(getText(br, line.substring(1, line.length() - 1)));
            }
        }
        return documentLines;
    }

    /**
     * This method extracts and returns all lines between the <title>...</title> or
     * <text>...</text> tags in the cranfield collection
     * @param br file pointer
     * @param tagName TITLE or TEXT
     * @return list of all lines found between the required tags
     */
    private List<String> getText(BufferedReader br, String tagName) throws IOException {
        String line = br.readLine(), tagEnd = String.format("</%s>", tagName);
        List<String> textLines = new ArrayList<>();
        while (!line.equals(tagEnd)) {
            textLines.add(line.trim());
            line = br.readLine();
        }
        return textLines;
    }
}
