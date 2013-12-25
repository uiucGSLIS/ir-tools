package edu.gslis.indexes;

import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.textrepresentation.FeatureVector;
import edu.gslis.utils.Stopper;


public interface IndexWrapper {

	public SearchHits runQuery(GQuery query, int count);
	
	public SearchHits runQuery(String query, int count);
	
	public double docCount();

	public double termCount();

	public double docFreq(String term);

	public double termFreq(String term);
	
	public double docLengthAvg();
	
	public FeatureVector getDocVector(int docID, Stopper stopper);
	
	public FeatureVector getDocVector(String docno, Stopper stopper);
	
	public String getMetadataValue(String docno, String metadataName);
	
	public int getDocId(String docno);
	
	public double getDocLength(int docID);
	
	// really shouldn't use this
	public Object getActualIndex();
}
