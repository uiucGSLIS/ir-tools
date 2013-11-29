package edu.gslis.docscoring;

import edu.gslis.textrepresentation.FeatureVector;

/**
 * provides background stats (or similar) for running a particular {@link edu.gslis.textrepresentation.FeatureVector}.
 * @author mefron
 *
 */
public interface ScoreSupport {

	public double supportForFeature(String feature);
	
	public FeatureVector supportForFeatureVector(FeatureVector vector);
}
