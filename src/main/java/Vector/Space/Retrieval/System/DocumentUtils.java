package Vector.Space.Retrieval.System;

/**
 * This class contains all helper methods required to process a document
 * @author - Siddhanth Venkateshwaran
 */
public class DocumentUtils {

    /**
     * This method will construct the fully qualified document index for the cranfield collection
     * If index is between 1 and 10 exclusive, then the index is 000x
     * If index is between 10 and 100 exclusive, then index is 00xx
     * If index is between 100 and 1000 exclusive, then index is 0xxx
     * If index is > 1000 then index is xxxx
     * @param documentIndex index which is to be expanded
     * @return fully qualified document index
     */
    public static String constructDocumentName(int documentIndex) {
        if (documentIndex < 10) return String.format("000%d", documentIndex);
        if (documentIndex < 100) return String.format("00%d", documentIndex);
        if (documentIndex < 1000) return String.format("0%d", documentIndex);
        return String.valueOf(documentIndex);
    }
}
