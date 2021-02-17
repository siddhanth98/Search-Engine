package Vector.Space.Retrieval.System.preprocessor;

import java.util.*;

/**
 * Name - Siddhanth Venkateshwaran
 * Class to count the number of occurrences of each word in the collection
 */
public class FrequencyGenerator {

    /**
     * This method will count number of occurrences of every word in the given list of words
     * and will return a map of words to corresponding counts.
     * @param tokens List of all tokens in the collection (may contain repeated tokens)
     * @return map of word to count
     */
    public Map<String, Integer> getFrequencyMap(List<String> tokens) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        tokens.forEach(token -> {
            frequencyMap.putIfAbsent(token, 1);
            frequencyMap.put(token, frequencyMap.get(token)+1);
        });
        return frequencyMap;
    }

    /**
     * This method returns the top n words occurring in the collection,
     * where n is one of the arguments to this method
     * @param tokens List of all tokens
     * @param count number of words required
     * @return list of tokens ranked by frequency
     */
    public List<String> getRankedListOfWords(List<String> tokens, int count) {
        Map<String, Integer> frequencyMap = this.getFrequencyMap(tokens);
        List<String> rankedList = new ArrayList<>();

        Map<Integer, List<String>> invertedFrequencyMap = getInvertedFrequencyMap(frequencyMap);

        invertedFrequencyMap.keySet()
                .stream()
                .sorted((c1, c2) -> -1 * Integer.compare(c1, c2))
                .forEach(c -> rankedList.addAll(invertedFrequencyMap.get(c)));
        return rankedList.subList(0, count);
    }

    /**
     * This method simply inverts the given frequency map of words to counts and
     * returns a map of counts to a list of words
     * @param frequencyMap map of words to counts
     * @return map of counts to corresponding list of words having count number of occurrences
     */
    public Map<Integer, List<String>> getInvertedFrequencyMap(Map<String,Integer> frequencyMap) {
        Map<Integer, List<String>> invertedFrequencyMap = new HashMap<>();
        frequencyMap.forEach((key, value) -> {
            invertedFrequencyMap.putIfAbsent(value, new ArrayList<>());
            invertedFrequencyMap.get(value).add(key);
        });
        return invertedFrequencyMap;
    }
}
