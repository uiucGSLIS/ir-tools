package edu.gslis.scoring;

import edu.gslis.docscoring.support.CollectionStats;
import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHit;

/**
 * Okapi BM25 scorer.
 * 
 * @author Garrick
 * @see {@link ScorerBM25}
 *
 */
public class BM25DocScorer implements DocScorer {
	
	public static final double DEFAULT_K1 = 1.2;
	public static final double DEFAULT_B = 0.75;
	public static final double DEFAULT_K3 = 2.0;
	
	private double k1;
	private double b;
	private double k3;

	private CollectionStats collectionStats;
	
	public BM25DocScorer(CollectionStats collectionStats) {
		this(collectionStats, DEFAULT_K1, DEFAULT_B, DEFAULT_K3);
	}
	
	public BM25DocScorer(CollectionStats collectionStats, double k1, double b, double k3) {
		this.collectionStats = collectionStats;
		this.k1 = k1;
		this.b = b;
		this.k3 = k3;
	}
	
	@Override
	public double scoreTerm(String term, SearchHit document) {
		double numDocs = collectionStats.getDocCount();
		double avgDocLength = collectionStats.getTokCount() / numDocs;

		double docLength = document.getLength();

		double termFreq = document.getFeatureVector().getFeatureWeight(term);

		double docFreq = collectionStats.docCount(term);
		double idf = Math.log((numDocs - docFreq + 0.5) / (docFreq + 0.5));

		double K = k1 * ((1 - b) + b * (docLength / avgDocLength));
		double documentScaling = ((termFreq * (k1 + 1)) / (K + termFreq));

		return idf * documentScaling;
	}
	
	public double scoreTerm(String term, SearchHit document, GQuery query) {
		double termFreqQuery = query.getFeatureVector().getFeatureWeight(term);
		double queryScaling = (k3 + 1) * termFreqQuery / (k3 + termFreqQuery);

		return scoreTerm(term, document) * queryScaling;
	}

}
