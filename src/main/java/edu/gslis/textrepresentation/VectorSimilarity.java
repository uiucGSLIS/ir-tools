package edu.gslis.textrepresentation;


public interface VectorSimilarity {
	
	public double score(FeatureVector x, FeatureVector y);
	
}
