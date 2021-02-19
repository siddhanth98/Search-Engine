package Vector.Space.Retrieval.System.preprocessor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class to process vocabulary of a document collection
 * @author Siddhanth Venkateshwaran
 */
public class VocabularyProcessor {

    /**
     * Method will create and return a set of words from the given list of tokens
     * @param tokens List of tokens (may contain repeated tokens)
     * @return Set of tokens
     */
    public Set<String> getVocabulary(List<String> tokens) {
        return new HashSet<>(tokens);
    }


}
