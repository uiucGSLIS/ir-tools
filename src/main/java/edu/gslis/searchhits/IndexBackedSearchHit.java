package edu.gslis.searchhits;

import edu.gslis.indexes.IndexWrapper;
import edu.gslis.textrepresentation.FeatureVector;

public class IndexBackedSearchHit extends SearchHit {
	
	private IndexWrapper index;
	
	public IndexBackedSearchHit(IndexWrapper index, SearchHit toCopy) {
		this(index);
		copyFromVanillaSearchHit(toCopy);
	}

	public IndexBackedSearchHit(IndexWrapper index) {
		super();
		setIndex(index);
	}
	
	public void setIndex(IndexWrapper index) {
		this.index = index;
	}
	
	public void copyFromVanillaSearchHit(SearchHit hit) {
		setDocno(hit.getDocno());
		setDocID(hit.getDocID());
		setFeatureVector(hit.getFeatureVector());
		setScore(hit.getScore());
		setLength(hit.getLength());
		setQueryName(hit.getQueryName());
	}
	
	@Override
	public int getDocID() {
		int docID = super.getDocID();
		if (docID == 0) {
			if (getDocno() == null) {
				System.err.println("SearchHit has neither docno nor docID. Giving docID of 0. This may crash your program.");
			} else {
				docID = index.getDocId(getDocno());
				setDocID(docID);
			}
		}
		return docID;
	}

	@Override
	public FeatureVector getFeatureVector() {
		FeatureVector vector = super.getFeatureVector();
		if (vector == null) {
			if (getDocID() == 0) {
				System.err.println("SearchHit has no identification. Giving empty FeatureVector.");
				return new FeatureVector(null);
			} else {
				vector = index.getDocVector(getDocID(), null);
				return vector;
			}
		}
		return vector;
	}
}
