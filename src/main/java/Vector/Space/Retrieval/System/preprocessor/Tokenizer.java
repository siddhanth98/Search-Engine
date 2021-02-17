package Vector.Space.Retrieval.System.preprocessor;

import java.util.*;
import java.util.stream.Collectors;
import Vector.Space.Retrieval.System.stemmer.Porter;

/**
 * Name - Siddhanth Venkateshwaran
 * This class is responsible for tokenizing the input string by splitting on whitespace
 * and removing punctuations
 */
public class Tokenizer {
    private final Porter stemmer;
    private final StopWordProcessor stopWordProcessor;
    private final boolean stem;
    private final boolean eliminateStopWords;

    public Tokenizer(final boolean stem, final boolean eliminateStopWords,
                     final StopWordProcessor stopWordProcessor) {
        stemmer = new Porter();
        this.stem = stem;
        this.eliminateStopWords = eliminateStopWords;
        this.stopWordProcessor = stopWordProcessor;
    }

    /**
     * Method to tokenize the given text
     * @param text input text to tokenize
     * @return list of tokens obtained from the text after removing whitespace, punctuations and symbols
     */
    public List<String> tokenize(final String text) {
        final String delimiters = " \t";
        final String punctuationFilter = "[!+=\\-/.:,;?'\"`~(){}<>%&#$\\[\\]|^@*_]";
        final String digitFilter = "\\d+";

        return Collections.list(new StringTokenizer(text, delimiters))
                .stream()
                .map(token -> token.toString().toLowerCase().replaceAll(punctuationFilter, ""))
                .map(token -> token.toLowerCase().replaceAll(digitFilter, ""))
                .filter(token -> token.length() > 0)
                .collect(Collectors.toList());
    }

    /**
     * This method traverses over all files in the given directory and will generate a list of tokens
     * occurring in all files.
     * @param directory Path of the directory
     * @return list of tokens occurring in all files in the given directory
     */
    public List<String> getTokens(String directory, String fileNamePrefix) {
        List<String> tokens = new ArrayList<>();
        try {
            List<String> collectionText = new DocumentParser().parseAndRetrieveText(directory, fileNamePrefix);
            collectionText.forEach(line -> tokens.addAll(tokenize(line)));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return preprocessTokens(tokens);
    }

    /**
     * This method will eliminate stop words and stem all tokens if required
     * @param tokens List of tokens
     * @return List of tokens
     */
    public List<String> preprocessTokens(List<String> tokens) {
        List<String> processedTokens = new ArrayList<>(tokens);
        if (this.eliminateStopWords)
            processedTokens = this.stopWordProcessor.eliminateStopWordsFromList(tokens);
        if (this.stem)
            processedTokens = processedTokens.stream().map(this.stemmer::stripAffixes).collect(Collectors.toList());
        if (this.eliminateStopWords)
            processedTokens = this.stopWordProcessor.eliminateStopWordsFromList(processedTokens);
        return processedTokens.stream().filter(token -> token.length() > 2).collect(Collectors.toList());
    }
}
