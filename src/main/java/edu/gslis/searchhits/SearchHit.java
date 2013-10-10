package edu.gslis.searchhits;

import java.util.HashMap;
import java.util.Map;

import edu.gslis.utils.Scorable;




public class SearchHit implements Scorable {
	
	private String queryName;
	private String docno;
	private int docID;
	private double score;
	private double length;
	private Map<String,Double> data;		// e.g. a term vector representing this doc
	private Map<String,Object> metadata;	// e.g. info such as a unix epoch
	
	public SearchHit() {
		data = new HashMap<String,Double>();
		metadata = new HashMap<String,Object>();
	}
	
	
	public Object getMetadataValue(String property) {
		if(!metadata.containsKey(property)) {
			System.err.println("requested non-existent metadata " + property + " from doc " + docno);
			System.exit(-1); 
		} 
		return metadata.get(property);
	}
	public void setMetadataValue(String property, Object value) {
		metadata.put(property, value);
	}
	
	public String getQueryName() {
		return queryName;
	}
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}
	public String getDocno() {
		return docno;
	}
	public void setDocno(String docno) {
		this.docno = docno;
	}
	public int getDocID() {
		return docID;
	}
	public void setDocID(int docID) {
		this.docID = docID;
	}
	public Double getVal(String key) {
		if(! data.containsKey(key))
			return 0.0;
		return this.data.get(key);
	}
	public void setField(String key, double value) {
		data.put(key, value);
	}
	public void setData(Map<String,Double> dataMap) {
		this.data = dataMap;
	}
	public Map<String,Double> getFields() {
		return data;
	}
	public void setLength(double length) {
		this.length = length;
	}
	public double getLength() {
		return length;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getScore() {
		return score;
	}
	
}
