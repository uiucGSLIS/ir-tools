package edu.gslis.docscoring;

import java.util.Iterator;

import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHit;

/**
 * Standard dirichlet query likelihood scorer
 * 
 * @author mefron
 *
 */
public class ScorerDirichlet implements Scorer {
	// shouldn't need this.  avoid divide by (or log of) 0.
	private static final double EPSILON = Math.pow(10.0, -7.0);

	private ScoreSupportBackgroundLm collectionModel;
	private GQuery gQuery;
	private double mu = 2500.0;
	
	/**
	 * 
	 * @param gQuery
	 * @param collectionModel a {@link ScoreSupport} with bg probabilities for each term in <code>gQuery</code> 
	 */
	public ScorerDirichlet(GQuery gQuery, ScoreSupportBackgroundLm collectionModel) {
		this.gQuery = gQuery;
		this.collectionModel = collectionModel;
		verify();
	}
	
	/**
	 * retrieves the log-likelihood.  assumes the search hit is populated w term counts.
	 */
	public double score(SearchHit doc) {
		double logLikelihood = 0.0;
		Iterator<String> queryIterator = gQuery.getFeatureVector().iterator();
		while(queryIterator.hasNext()) {
			String feature = queryIterator.next();
			double docFreq = doc.getVal(feature);
			double docLength = doc.getLength();
			double collectionProb = collectionModel.supportForFeature(feature) + EPSILON;
			double pr = (docFreq + mu*collectionProb) / (docLength + mu);
			double queryWeight = gQuery.getFeatureVector().getFeatureWeight(feature);
			logLikelihood += queryWeight * Math.log(pr);
		}
		return logLikelihood;
	}
	
	/**
	 * assure the the bg model features match the query
	 * @return
	 */
	private boolean verify() {
		Iterator<String> queryTerms = gQuery.getFeatureVector().iterator();
		while(queryTerms.hasNext()) {
			String queryTerm = queryTerms.next();
			if(! (collectionModel.supportForFeature(queryTerm) > 0)) {
				System.err.println("no background stats for term: " + queryTerm);
				//System.exit(-1);
			}
		}
		return true;
	}
	
	/**
	 * set smoothing parameter
	 * @param mu Dirichlet hyperparameter (pseudo-counts)
	 */
	public void setMu(double mu) {
		this.mu = mu;
	}
	
	


}
