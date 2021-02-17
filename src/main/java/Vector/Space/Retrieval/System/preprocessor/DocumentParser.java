package Vector.Space.Retrieval.System.preprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

public class DocumentParser {
    public List<String> parseAndRetrieveText(String directory, String fileNamePrefix) {
        List<String> result = new ArrayList<>();
        int documentIndex = 1;

        while(documentIndex < 2) {
            try {
                BufferedReader document = new BufferedReader(
                        new InputStreamReader(
                                Objects.requireNonNull(
                                        Tokenizer.class.getClassLoader()
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

    private String constructDocumentName(int documentIndex) {
        if (documentIndex < 10) return String.format("000%d", documentIndex);
        if (documentIndex < 100) return String.format("00%d", documentIndex);
        if (documentIndex < 1000) return String.format("0%d", documentIndex);
        return String.valueOf(documentIndex);
    }
}
