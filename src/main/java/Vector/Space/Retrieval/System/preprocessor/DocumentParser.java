package Vector.Space.Retrieval.System.preprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

import static Vector.Space.Retrieval.System.DocumentUtils.constructDocumentName;

public class DocumentParser {
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
                currentDocumentLines.forEach(System.out::println);

                result.addAll(currentDocumentLines);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

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
