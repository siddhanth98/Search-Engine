package Vector.Space.Retrieval.System.indexer;

import Vector.Space.Retrieval.System.preprocessor.DocumentParser;
import Vector.Space.Retrieval.System.preprocessor.Tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static Vector.Space.Retrieval.System.DocumentUtils.constructDocumentName;

public class InvertedIndexer {
    private final String directory;
    private final String fileNamePrefix;
    private final int collectionSize;
    private final Map<String, Map<String, Integer>> index;
    private Map<String, Double> documentVector;

    public InvertedIndexer(String directory, int collectionSize, String fileNamePrefix) {
        this.directory = directory;
        this.collectionSize = collectionSize;
        this.fileNamePrefix = fileNamePrefix;
        index = new HashMap<>();
    }

    public void constructInvertedIndex(Tokenizer tokenizer) {
        DocumentParser documentParser = new DocumentParser();

        try {
            List<String> preprocessedTokens = new ArrayList<>();
            for (int documentIndex = 1; documentIndex <= this.collectionSize; documentIndex++) {
                List<String> currentDocumentLines = documentParser.parseAndRetrieveText(documentIndex, this.directory, this.fileNamePrefix);
                List<List<String>> tokens = currentDocumentLines.stream().map(tokenizer::tokenize).collect(Collectors.toList());
                tokens.forEach(tokenList -> preprocessedTokens.addAll(tokenizer.preprocessTokens(tokenList)));
                insertTokensInIndex(preprocessedTokens, documentIndex);
                preprocessedTokens.clear();
            }
            constructDocumentVectorTable();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void insertTokensInIndex(List<String> tokens, int documentIndex) {
        String documentName = String.format("%s%s", this.fileNamePrefix, constructDocumentName(documentIndex));
        tokens.forEach(token -> {
            this.index.putIfAbsent(token, new HashMap<>());
            if (this.index.get(token).containsKey(documentName))
                this.index.get(token).put(documentName, this.index.get(token).get(documentName)+1);
            else this.index.get(token).put(documentName, 1);
        });
    }

    public Map<String, Map<String, Integer>> getIndex() {
        return index;
    }

    private void constructDocumentVectorTable() {
        this.documentVector = new HashMap<>();
        this.index.keySet().forEach(token ->
            this.index.get(token).forEach((document, termFrequency) -> {
                try {
                    double currentTokenWeight = Math.pow(getWeight(token, document), 2);
                    if (this.documentVector.containsKey(document))
                        this.documentVector.put(document, this.documentVector.get(document)+currentTokenWeight);
                    else
                        this.documentVector.put(document, currentTokenWeight);
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }));
    }

    public double getWeight(String token, String document) throws Exception {
        return getTermFrequency(token, document) * getInverseDocumentFrequency(token);
    }

    public int getDocumentFrequency(String token) {
        if (this.index.containsKey(token)) return this.index.get(token).keySet().size();
        return 0;
    }

    public int getTermFrequency(String token, String document) {
        if (this.index.containsKey(token) && this.index.get(token).containsKey(document)) return this.index.get(token).get(document);
        return 0;
    }

    public double getInverseDocumentFrequency(String token) throws Exception {
        if (this.index.containsKey(token)) return Math.log10((double)(this.collectionSize) / (double)(getDocumentFrequency(token)));
        throw new Exception();
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        this.index.keySet().forEach(token -> {
            result.append(String.format("%s\t\t\t\t\t\t\t\t\t\t\t\t", token));
            this.index.get(token).forEach((document, termFrequency) -> result.append(String.format("%s -> %d\t\t", document, termFrequency)));
            result.append("\n");
        });
        return result.toString();
    }

    public void printDocumentVector() {
        this.documentVector.keySet().forEach(document -> System.out.printf("%s -> %s%n", document, this.documentVector.get(document)));
    }
}
