package edu.gslis.scoring.expansion;

import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.textrepresentation.FeatureVector;
import edu.gslis.utils.Stopper;

/**
 * Builds an RM3 for a given query.
 * @author Garrick
 *
 */
public class RM3Builder {
	
	public FeatureVector buildRelevanceModel(GQuery query, SearchHits initialResults,
			RM1Builder rm1,
			double originalQueryWeight) {
		return buildRelevanceModel(query, initialResults, rm1, originalQueryWeight, null);
	}

	public FeatureVector buildRelevanceModel(GQuery query, SearchHits initialResults,
			RM1Builder rm1,
			double originalQueryWeight,
			Stopper stopper) {

		query.getFeatureVector().normalize(); // very important (maybe?)

		FeatureVector rmVector = rm1.buildRelevanceModel(query, initialResults, stopper);

		return buildRelevanceModel(query, rmVector, originalQueryWeight);
	}

	public FeatureVector buildRelevanceModel(GQuery query, FeatureVector rmVector, double originalQueryWeight) {
		query.getFeatureVector().normalize(); // very important
		rmVector.normalize(); // also very important

		return FeatureVector.interpolate(query.getFeatureVector(), rmVector, originalQueryWeight);
	}

}
