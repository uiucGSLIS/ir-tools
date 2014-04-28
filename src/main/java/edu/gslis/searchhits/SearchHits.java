package edu.gslis.searchhits;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.gslis.utils.ScorableComparator;


public class SearchHits {
	private List<SearchHit> hits;
	private Iterator<SearchHit> iter;
	

	public SearchHit getHit(int i) {
		if(hits.size() <= i)
			return new SearchHit();
		return hits.get(i);
	}
	
	public SearchHits() {
		hits = new LinkedList<SearchHit>();	
		
	}
	
	public SearchHits(List<SearchHit> hits) {
		this.hits = hits;
	}
	
	public void add(SearchHit hit) {
		hits.add(hit);
	}
	
	public void rank() {
		ScorableComparator comparator = new ScorableComparator(true);
		Collections.sort(hits, comparator);
		iter = hits.iterator();
	}
	
	public Iterator<SearchHit> iterator() {
		iter = hits.iterator();
		return iter;
	}
	
	public int size() {
		return hits.size();
	}
	
	public void crop(int size) {
	    hits = hits.subList(0, size);
	    iter = hits.iterator();
	}
	

}
