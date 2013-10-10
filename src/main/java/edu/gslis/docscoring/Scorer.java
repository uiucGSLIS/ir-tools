package edu.gslis.docscoring;

import edu.gslis.searchhits.SearchHit;



public interface Scorer {

	public double score(SearchHit doc);
	
}
