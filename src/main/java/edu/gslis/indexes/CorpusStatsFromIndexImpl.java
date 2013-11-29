package edu.gslis.indexes;

public class CorpusStatsFromIndexImpl implements CorpusStats {
	private IndexWrapper index;
	
	
	public CorpusStatsFromIndexImpl(IndexWrapper index) {
		this.index = index;
	}
	
	
	public double docCount() {
		return index.docCount();
	}

	public double termCount() {
		return index.termCount();
	}

	public double docFreq(String term) {
		return index.docFreq(term);
	}

	public double termFreq(String term) {
		return index.termFreq(term);
	}

}
