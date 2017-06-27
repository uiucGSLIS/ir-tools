package edu.gslis.lucene.expansion;


import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import edu.gslis.indexes.IndexWrapper;
import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.textrepresentation.FeatureVector;

public class Rocchio {
	
	private double alpha;
	private double beta;
	private double k1;
	private double b;
	
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
	
	public void expandQuery(IndexWrapper index, GQuery query, int fbDocs, int fbTerms) throws IOException {
		
		SearchHits hits = index.runQuery(query, fbDocs);
		
		FeatureVector summedTermVec = new FeatureVector(null);
		
		for (SearchHit hit: hits.hits()) {
			// Get the document tokens and add to the doc vector
			FeatureVector docVec = index.getDocVector(hit.getDocID(), null);
			
			// Compute the BM25 weights
			computeBM25Weights(index, docVec, summedTermVec);
		}
		
		// Multiply the summed term vector by beta / |Dr|
		FeatureVector relDocTermVec = new FeatureVector(null);
		for (String term : summedTermVec.getFeatures()) {
			relDocTermVec.addTerm(term, summedTermVec.getFeatureWeight(term) * beta / fbDocs);
		}
		
		// Create a query vector and scale by alpha
		FeatureVector rawQueryVec = new FeatureVector(null);
		parseText(query.getText(), rawQueryVec);
		
		FeatureVector summedQueryVec = new FeatureVector(null);
		computeBM25Weights(index, rawQueryVec, summedQueryVec);
		
		FeatureVector queryTermVec = new FeatureVector(null);
		for (String term : rawQueryVec.getFeatures()) {
			queryTermVec.addTerm(term, summedQueryVec.getFeatureWeight(term) * alpha);
		}
		
		// Combine query and rel doc vectors
		for (String term : queryTermVec.getFeatures()) {
			relDocTermVec.addTerm(term, queryTermVec.getFeatureWeight(term));
		}
		
		// Get top terms
		relDocTermVec.clip(fbTerms);
		
		StringBuffer expandedQuery = new StringBuffer();
		for (String term : relDocTermVec.getFeatures()) {
			expandedQuery.append(term + "^" + relDocTermVec.getFeatureWeight(term) + " ");
		}
		
		query.setText(expandedQuery.toString());
	}
	
	private void parseText(String text, FeatureVector vector) throws IOException {
		StandardAnalyzer analyzer = new StandardAnalyzer();
		TokenStream tokenStream = analyzer.tokenStream(null, text);
		CharTermAttribute tokens = tokenStream.addAttribute(CharTermAttribute.class);
		tokenStream.reset();
		while (tokenStream.incrementToken()) {
			String token = tokens.toString();
			vector.addTerm(token);
		}
		analyzer.close();	
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
