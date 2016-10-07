package edu.gslis.docscoring.similarity;

import java.util.Iterator;

import edu.gslis.textrepresentation.FeatureVector;

public class KLSimilarityScorer implements SimilarityScorer {
	
	public double score(FeatureVector doc1, FeatureVector doc2) {
		double score = 0;
		
		Iterator<String> termIt = doc1.iterator();
		while (termIt.hasNext()) {
			String term = termIt.next();
			
			double pwd = doc1.getFeatureWeight(term) / doc1.getLength();
			double pwc = (doc2.getFeatureWeight(term) + 1) / (doc2.getLength() + doc1.getFeatureCount());
			
			if (pwd == 0) {
				continue;
			} else {
				score += pwd * Math.log(pwd / pwc);
			}
		}
		
		return score;
	}

}
