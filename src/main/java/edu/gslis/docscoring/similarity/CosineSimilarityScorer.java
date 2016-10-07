package edu.gslis.docscoring.similarity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.gslis.textrepresentation.FeatureVector;

public class CosineSimilarityScorer implements SimilarityScorer {
	
	public double score(FeatureVector doc1, FeatureVector doc2) {
		double num = 0.0;
		double denomX = 0.0;
		double denomY = 0.0;
		
		Set<String> vocab = new HashSet<String>();
		vocab.addAll(doc1.getFeatures());
		vocab.addAll(doc2.getFeatures());

		Iterator<String> termIt = vocab.iterator();
		String term;
		while (termIt.hasNext()) {
			term = termIt.next();
			
			double docWeight = doc1.getFeatureWeight(term);
			double otherDocWeight = doc2.getFeatureWeight(term); 
			
			num += docWeight * otherDocWeight;
			denomX += Math.pow(docWeight, 2);
			denomY += Math.pow(otherDocWeight, 2);
		}
		double denom = Math.sqrt(denomX)*Math.sqrt(denomY);
		if (denom == 0) denom = 1;  // should be unnecessary...
		return num/denom;
	}
}
