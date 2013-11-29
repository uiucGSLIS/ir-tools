package edu.gslis.indexes;

public interface CorpusStats {

	public double docCount();
	
	public double termCount();
		
	public double docFreq(String term);
	
	public double termFreq(String term);
	
}
