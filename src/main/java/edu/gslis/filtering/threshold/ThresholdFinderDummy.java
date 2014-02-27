package edu.gslis.filtering.threshold;


import edu.gslis.eval.Qrels;
import edu.gslis.searchhits.SearchHits;



public class ThresholdFinderDummy {
	private double threshold = Double.NEGATIVE_INFINITY;

	
	
	public ThresholdFinderDummy(String queryName, SearchHits resultsForQuery, Qrels qrels) {
		;
	}
	
	public double getThreshold() {
		return threshold;
	}

	
	
	
}
