package edu.gslis.lucene.expansion;


import java.io.IOException;

import edu.gslis.indexes.IndexWrapper;
import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.textrepresentation.FeatureVector;
import edu.gslis.utils.Stopper;

public class Rocchio {
	
	private double alpha;
	private double beta;
	private double k1;
	private double b;
	
	private Stopper stoplist = new Stopper();
	
	/**
	 * Default parameter values taken from:
	 * https://nlp.stanford.edu/IR-book/html/htmledition/the-rocchio71-algorithm-1.html
	 */
	public Rocchio() {
		this(1.0, 0.75);
	}	
	
	public Rocchio(double alpha, double beta) {
		this(alpha, beta, 1.2, 0.75);
	}
	
	public Rocchio(double alpha, double beta, double k1, double b) {
		this.alpha = alpha;
		this.beta = beta;
		this.k1 = k1;
		this.b = b;
	}
	
	public void setStopper(Stopper stoplist) {
		this.stoplist = stoplist;
	}
	
	public void expandQuery(IndexWrapper index, GQuery query, int fbDocs, int fbTerms) throws IOException {
		
		SearchHits hits = index.runQuery(query, fbDocs);
		
		FeatureVector feedbackVec = new FeatureVector(stoplist);
		
		for (SearchHit hit: hits.hits()) {
			// Get the document tokens and add to the doc vector
			FeatureVector docVec = index.getDocVector(hit.getDocID(), stoplist);
			
			// Compute the BM25 weights and add to the feedbackVector
			computeBM25Weights(index, docVec, feedbackVec);
		}
		
		// Multiply the summed term vector by beta / |Dr|
		FeatureVector relDocTermVec = new FeatureVector(stoplist);
		for (String term : feedbackVec.getFeatures()) {
			relDocTermVec.addTerm(term, feedbackVec.getFeatureWeight(term) * beta / fbDocs);
		}
		
		// Create a query vector and scale by alpha
		FeatureVector origQueryVec = query.getFeatureVector();
		
		FeatureVector weightedQueryVec = new FeatureVector(stoplist);
		computeBM25Weights(index, origQueryVec, weightedQueryVec);
		
		FeatureVector queryTermVec = new FeatureVector(stoplist);
		for (String term : origQueryVec.getFeatures()) {
			queryTermVec.addTerm(term, weightedQueryVec.getFeatureWeight(term) * alpha);
		}
		
		// Combine query and feedback vectors
		for (String term : queryTermVec.getFeatures()) {
			relDocTermVec.addTerm(term, queryTermVec.getFeatureWeight(term));
		}
		
		// Get top terms
		relDocTermVec.clip(fbTerms);
		
		query.setFeatureVector(relDocTermVec);
	}
	
	private void computeBM25Weights(IndexWrapper index, FeatureVector docVec, FeatureVector summedTermVec) throws IOException {
		for (String term : docVec.getFeatures()) {
			double docCount = index.docCount();
			double docOccur = index.docFreq(term);			
			double avgDocLen = index.docLengthAvg();
			
			double idf = Math.log( (docCount + 1) / (docOccur + 0.5) ); // following Indri
			double tf = docVec.getFeatureWeight(term);
			
			double weight = (idf * k1 * tf) / (tf + k1 * (1 - b + b * docVec.getLength() / avgDocLen));
					
			summedTermVec.addTerm(term, weight);
		}
	}
}
