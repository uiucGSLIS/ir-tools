package edu.gslis.scoring;

import edu.gslis.searchhits.SearchHit;

/**
 * Simply returns the weight of the term in the document feature vector.
 * 
 * @author Garrick
 *
 */
public class DoNothingDocScorer implements DocScorer {

	@Override
	public double scoreTerm(String term, SearchHit document) {
		return document.getFeatureVector().getFeatureWeight(term);
	}

}
