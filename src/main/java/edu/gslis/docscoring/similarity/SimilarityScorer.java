package edu.gslis.docscoring.similarity;

import edu.gslis.textrepresentation.FeatureVector;

public interface SimilarityScorer {
	
	public double score(FeatureVector doc1, FeatureVector doc2);

}
