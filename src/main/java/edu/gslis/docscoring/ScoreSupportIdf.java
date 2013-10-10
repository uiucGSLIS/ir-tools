package edu.gslis.docscoring;

import java.util.Iterator;

import edu.gslis.indexes.CorpusStats;
import edu.gslis.textrepresentation.FeatureVector;


public class ScoreSupportIdf implements ScoreSupport {
	
	private CorpusStats corpusStats;
	
	public ScoreSupportIdf(CorpusStats corpusStats) {
		this.corpusStats = corpusStats;
	}

	
	public double supportForFeature(String feature) {
		double docFreq = corpusStats.docFreq(feature);
		if(docFreq==0.0)
			System.err.println("zero value in background model for feature: " + feature);
		double idf = Math.log(corpusStats.docCount() / (docFreq + 1.0));
		return idf;
	}

	
	public FeatureVector supportForFeatureVector(FeatureVector vector) {
		FeatureVector m = new FeatureVector(null);
		Iterator<String> it = vector.iterator();
		while(it.hasNext()) {
			String feature = it.next();
			double val = this.supportForFeature(feature);
			m.addTerm(feature, val);
		}
		return m;
	}
	
	
}
