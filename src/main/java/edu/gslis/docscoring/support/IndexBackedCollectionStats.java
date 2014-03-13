package edu.gslis.docscoring.support;


import edu.gslis.indexes.IndexWrapper;
import edu.gslis.indexes.IndexWrapperIndriImpl;


public class IndexBackedCollectionStats extends CollectionStats {	
	protected IndexWrapper index;
	
	public void setStatSource(String statSource) {
		this.index = new IndexWrapperIndriImpl(statSource);
		tokCount = index.termCount();
		termTypeCount = index.termTypeCount();
	}

	@Override
	public double termCount(String term) {
		return index.termFreq(term);
	}

	@Override
	public double docCount(String term) {
		return index.docFreq(term);
	}
		


}
