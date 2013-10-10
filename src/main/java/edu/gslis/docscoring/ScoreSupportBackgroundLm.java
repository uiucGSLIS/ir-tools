package edu.gslis.docscoring;

import java.util.Iterator;

import edu.gslis.indexes.CorpusStats;
import edu.gslis.textrepresentation.FeatureVector;



public class ScoreSupportBackgroundLm implements ScoreSupport {
	
	private CorpusStats corpusStats;
	
	public ScoreSupportBackgroundLm(CorpusStats corpusStats) {
		this.corpusStats = corpusStats;
	}

	public double supportForFeature(String feature) {
		feature = feature.toLowerCase();
		double prob = corpusStats.termFreq(feature) / corpusStats.termTokenCount();
		return prob;
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
