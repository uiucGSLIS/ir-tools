package edu.gslis.docscoring;

import java.util.Iterator;

import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHit;


public class ScorerDirichlet implements Scorer {
	private static final double EPSILON = Math.pow(10.0, -7.0);

	private ScoreSupportBackgroundLm collectionModel;
	private GQuery gQuery;
	private double mu = 2500.0;
	
	public ScorerDirichlet(GQuery gQuery, ScoreSupportBackgroundLm collectionModel) {
		// should test to make sure gQuery and collectionModel have the same features defined
		this.gQuery = gQuery;
		this.collectionModel = collectionModel;
	}
	
	public double score(SearchHit doc) {
		double logLikelihood = 0.0;
		Iterator<String> queryIterator = gQuery.getFeatureVector().iterator();
		while(queryIterator.hasNext()) {
			String feature = queryIterator.next();
			double docFreq = doc.getVal(feature);
			double docLength = doc.getLength();
			double collectionProb = collectionModel.supportForFeature(feature) + EPSILON;
			double pr = (docFreq + mu*collectionProb) / (docLength + mu);
			double queryWeight = gQuery.getFeatureVector().getFeaturetWeight(feature);
			logLikelihood += queryWeight * Math.log(pr);
		}
		return logLikelihood;
	}
	

	
	public void setMu(double mu) {
		this.mu = mu;
	}


}
