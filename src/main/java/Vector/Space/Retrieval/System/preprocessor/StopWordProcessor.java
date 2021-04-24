package Vector.Space.Retrieval.System.preprocessor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static Vector.Space.Retrieval.System.Constants.*;

/**
 * Class to handle occurrences of stop-words
 * @author Siddhanth Venkateshwaran
 */
public class StopWordProcessor {

    private final Set<String> stopWords;
    public StopWordProcessor() {
        this.stopWords = getAllStopWords(stopWordsFileName);
    }

    /**
     * This method will return all stop words that are found in a list of tokens
     * @param tokens List of all tokens
     * @return List of stop words found in the list of tokens
     */
    public List<String> getStopWordsFromList(List<String> tokens) {
         return tokens.stream().filter(this.stopWords::contains).collect(Collectors.toList());
    }

    /**
     * This method will remove all stop words from the given list of tokens
     */
    public List<String> eliminateStopWordsFromList(List<String> tokens) {
        return tokens.stream().filter(token -> !this.stopWords.contains(token)).collect(Collectors.toList());
    }

    /**
     * This method returns all possible stop words that can exist in a given document collection
     * @param fileName Path to the file having all the stop words separated by newline.
     * @return Set of all stop words
     */
    public Set<String> getAllStopWords(String fileName) {
        Set<String> stopWords = new HashSet<>();

        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            StopWordProcessor.class.getClassLoader().getResourceAsStream(fileName)));
            String line;
            while((line = br.readLine()) != null) stopWords.add(line.trim());
            br.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return stopWords;
    }
}
