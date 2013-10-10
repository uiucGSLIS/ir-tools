package edu.gslis.utils;

import java.util.Iterator;

import edu.gslis.textrepresentation.FeatureVector;

public class StatUtils {

	public static double entropy(FeatureVector distribution) {
		if(! distribution.isLanguageModel()) 
			throw new IllegalArgumentException("improper FeatureVector arg: weights don't sum to 1.");
		double h = 0.0;
		
		Iterator<String> it = distribution.iterator();
		while(it.hasNext()) {
			String term = it.next();
			double prob = distribution.getFeaturetWeight(term);
			if(prob > 0)
				h += prob * Math.log(prob);
		}
		return -1 * h;
	}
	
	public static double klDivergence(FeatureVector x, FeatureVector y) {
		if(! x.isLanguageModel() || ! y.isLanguageModel()) 
			throw new IllegalArgumentException("improper FeatureVector arg: weights don't sum to 1.");
		double h = 0.0;
		
		Iterator<String> it = x.iterator();
		while(it.hasNext()) {
			String term = it.next();
			double xProb = x.getFeaturetWeight(term);
			double yProb = y.getFeaturetWeight(term);
			if(xProb > 0)
				h += xProb * Math.log(xProb / yProb);
		}
		return -1 * h;
	}
}
