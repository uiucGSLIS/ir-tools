package edu.gslis.docscoring.support;


import edu.gslis.indexes.IndexWrapper;
import edu.gslis.indexes.IndexWrapperLuceneImpl;


public class IndexBackedCollectionStatsLucene extends CollectionStats {	
	protected IndexWrapper index;
	
	public void setStatSource(String statSource) {
		this.index = new IndexWrapperLuceneImpl(statSource);
		tokCount = index.termCount();
		docCount = index.docCount();
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
