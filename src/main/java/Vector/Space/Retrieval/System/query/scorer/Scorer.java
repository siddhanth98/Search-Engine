package Vector.Space.Retrieval.System.query.scorer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Scorer {
    Map<String, Integer> termFrequencyMap = new HashMap<>();

    public abstract double getDocumentScore(String term, String documentUrl);
    public abstract double getQueryScore(String term);

    public void prepareQueryTermFrequencyMap(List<String> tokens) {
        tokens.forEach(token -> {
            if (!termFrequencyMap.containsKey(token)) termFrequencyMap.put(token, 0);
            termFrequencyMap.put(token, termFrequencyMap.get(token)+1);
        });
    }

    public int getQueryTermFrequency(String term) {
        return this.termFrequencyMap.get(term);
    }
}
