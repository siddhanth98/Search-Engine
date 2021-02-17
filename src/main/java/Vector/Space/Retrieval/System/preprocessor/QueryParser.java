package Vector.Space.Retrieval.System.preprocessor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QueryParser {
    public List<String> getTokens(String filename, Tokenizer tokenizer) {
        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(
                                QueryParser.class.getClassLoader().getResourceAsStream(filename))));
        String line;
        List<String> tokens = new ArrayList<>();

        try {
            while ((line = br.readLine()) != null) tokens.addAll(tokenizer.tokenize(line));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return tokenizer.preprocessTokens(tokens);
    }
}
