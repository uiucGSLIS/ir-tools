package edu.gslis.indexes;

public interface CorpusStats {

	public double docCount();
	
	public double termTokenCount();
	
	public double termTypeCount();
	
	public double docFreq(String term);
	
	public double termFreq(String term);
	
}
