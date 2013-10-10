package edu.gslis.docscoring;

import edu.gslis.textrepresentation.FeatureVector;


public interface ScoreSupport {

	public double supportForFeature(String feature);
	
	public FeatureVector supportForFeatureVector(FeatureVector vector);
}
