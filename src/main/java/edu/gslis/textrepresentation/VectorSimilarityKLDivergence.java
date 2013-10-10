package edu.gslis.textrepresentation;

import java.util.Iterator;


public class VectorSimilarityKLDivergence implements VectorSimilarity {


	public double score(FeatureVector x, FeatureVector y) {
		double score = 0.0;
		
	
		
		Iterator<String> it = x.iterator();
		while(it.hasNext()) {
			String feature = it.next();
			double xVal = x.getFeaturetWeight(feature);
			double yVal = y.getFeaturetWeight(feature);
			score += xVal * Math.log(xVal / yVal);
		}
		
		return score;
	}


	


}
