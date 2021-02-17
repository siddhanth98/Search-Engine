package Vector.Space.Retrieval.System;

public class DocumentUtils {
    public static String constructDocumentName(int documentIndex) {
        if (documentIndex < 10) return String.format("000%d", documentIndex);
        if (documentIndex < 100) return String.format("00%d", documentIndex);
        if (documentIndex < 1000) return String.format("0%d", documentIndex);
        return String.valueOf(documentIndex);
    }
}
