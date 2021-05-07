package Vector.Space.Retrieval.System.query.scorer;

import Vector.Space.Retrieval.System.Constants;
import Vector.Space.Retrieval.System.indexer.InvertedIndexer;

public class TFIDFScorer extends Scorer {
    private final InvertedIndexer indexer;

    public TFIDFScorer(InvertedIndexer indexer) {
        this.indexer = indexer;
    }

    @Override
    public double getDocumentScore(String term, String documentUrl) {
        try {
            int tf = this.indexer.getTermFrequency(term, documentUrl);
            double idf = this.indexer.getInverseDocumentFrequency(term);
            if (Constants.weighting.equalsIgnoreCase("tf"))
                return tf;
            return tf * idf;
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public double getQueryScore(String term) {
        try {
            double idf = this.indexer.getInverseDocumentFrequency(term);
            if (Constants.weighting.equalsIgnoreCase("tf"))
                return getQueryTermFrequency(term);
            return getQueryTermFrequency(term) * idf;
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return 0.0;
    }
}
