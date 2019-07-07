package edu.gslis.textrepresentation;


import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;

import edu.gslis.indexes.IndexWrapper;
import edu.gslis.utils.KeyValuePair;
import edu.gslis.utils.ScorableComparator;
import edu.gslis.utils.Stopper;




/**
 * Simple container mapping term-count pairs grabbed from an input text.
 * 
 * @author Miles Efron
 *
 */
public class FeatureVector implements Iterable<String> {
	private static StandardAnalyzer analyzer;	
	private Map<String, Double> features;
	private Stopper stopper;
	private double length = 0.0;


	// CONSTRUCTORS  
	public FeatureVector(String text, Stopper stopper) {
		this.stopper = stopper;
		
		if(stopper==null || stopper.asSet().size()==0) {
			analyzer = new StandardAnalyzer();
		} else {
			CharArraySet charArraySet = new CharArraySet(stopper.asSet(), true);
			analyzer = new StandardAnalyzer(charArraySet);
		}
		features = new HashMap<String, Double>();
		List<String> terms = this.analyze(text);
		Iterator<String> termsIt = terms.iterator();
		while(termsIt.hasNext()) {
			String term = termsIt.next();
			length += 1.0;
			Double val = (Double)features.get(term);
			if(val == null) {
				features.put(term, new Double(1.0));
			} else {
				double v = val.doubleValue() + 1.0;
				features.put(term, new Double(v));
			}
		}
	}

	public FeatureVector(Stopper stopper) {
		this.stopper = stopper;
		if(stopper==null || stopper.asSet().size()==0) {
			analyzer = new StandardAnalyzer();
		} else {
			CharArraySet charArraySet = new CharArraySet(stopper.asSet(), true);
			analyzer = new StandardAnalyzer(charArraySet);
		}
		features = new HashMap<String,Double>();
	}


	public void applyStopper(Stopper stopper) {
		for (String term : getFeatures().toArray(new String[features.size()])) {
			if (stopper.isStopWord(term)) {
				removeTerm(term);
			}
		}
	}

	// MUTATORS

	/**
	 * Add all the terms in a string to this vector
	 * @param text a space-delimited string where we want to add each word.
	 */
	public void addText(String text) {
		List<String> terms = this.analyze(text);
		Iterator<String> termsIt = terms.iterator();
		while(termsIt.hasNext()) {
			String term = termsIt.next();		
			addTerm(term);
		}
	}

	/**
	 * Add a term to this vector.  if it's already here, increment its count.
	 * @param term
	 */
	public void addTerm(String term) {
		this.addTerm(term, 1.0);
	}

	public void setTerm(String term, double weight) {
		if(stopper != null && stopper.isStopWord(term))
			return;
		length += weight;
		features.put(term, new Double(weight));
		
	}

	/**
	 * Add a term to this vector with this weight.  if it's already here, supplement its weight.
	 * @param term
	 * @param weight
	 */
	public void addTerm(String term, double weight) {
		if(stopper != null && stopper.isStopWord(term))
			return;
		
		Double w = ((Double)features.get(term));
		if(w == null) {
			features.put(term, new Double(weight));
		} else {
			double f = w.doubleValue();
			features.put(term, new Double(f+weight));
		}
		length += weight;
	}
	
	public void removeTerm(String term) {
		Double w = ((Double)features.get(term));
		if(w != null) {
			length -= w;
			features.remove(term);
		}
	}

	public void clip(int k) {
		List<KeyValuePair> kvpList = getOrderedFeatures();

		Iterator<KeyValuePair> it = kvpList.iterator();
		
		Map<String,Double> newMap = new HashMap<String,Double>(k);
		int i=0;
		double lastScore = Double.NEGATIVE_INFINITY; // track the most recent value so we don't cut off prematurely
		length = 0;
		while(it.hasNext()) {
			KeyValuePair kvp = it.next();
			double currentScore = kvp.getScore();
			if(i++ >= k) {// && lastScore != currentScore) { // only break if we've both exceeded the clip size and don't have a tie for this score
				break;
			}
			lastScore = currentScore;
			length += currentScore;
			newMap.put((String)kvp.getKey(), currentScore);
		}

		features = (HashMap<String, Double>) newMap;

	}

	public void normalize() {
		Map<String,Double> f = new HashMap<String,Double>(features.size());

		double sum = 0.0;
		
		Iterator<String> it = features.keySet().iterator();
		while(it.hasNext()) {
			String feature = it.next();
			double obs = features.get(feature);
			sum += obs;
		}
		
		it = features.keySet().iterator();
		while(it.hasNext()) {
			String feature = it.next();
			double obs = features.get(feature);
			f.put(feature, obs/sum);
		}
		
		features = f;
		length = 1.0;
	}
	
	public void l2Normalize() {
		Map<String,Double> f = new HashMap<String,Double>(features.size());
		double l2Norm = getVectorNorm();
		
		Iterator<String> it = features.keySet().iterator();
		while(it.hasNext()) {
			String feature = it.next();
			double obs = features.get(feature);
			f.put(feature, obs / l2Norm);
		}
		
		
		features = f;
		length = 1.0;
	}
	
	public void toIdf(IndexWrapper index, boolean logTf) {
		Map<String,Double> f = new HashMap<String,Double>(features.size());
		double len = 0.0;
		
		Iterator<String> it = features.keySet().iterator();
		while(it.hasNext()) {
			String feature = it.next();
			double obs = features.get(feature);
			if(logTf)
				obs = Math.log(obs + 1.0);
			double idf = Math.log(index.docCount() / (index.docFreq(feature) + 1.0));
			double tfidf = obs * idf;
			len += tfidf;
			f.put(feature, tfidf);
		}
		
		features = f;
		length = len;
	}

	public double clarity(IndexWrapper index) {
		double kld = 0.0;
		
		Iterator<String> it = features.keySet().iterator();
		while(it.hasNext()) {
			String feature = it.next();
			double obs = features.get(feature) / length;
			double bg  = (index.termFreq(feature) + 1) / index.termCount();
			kld += obs * Math.log(obs / bg);
		}
		return kld;
	}
	

	// ACCESSORS

	public Set<String> getFeatures() {
		return features.keySet();
	}

	public double getLength() {
		return length;
	}

	public int getFeatureCount() {
		return features.size();
	}

	public double getFeatureWeight(String feature) {
		Double w = (Double)features.get(feature);
		return (w==null) ? 0.0 : w.doubleValue();
	}

	public Iterator<String> iterator() {
		return features.keySet().iterator();
	}

	public boolean contains(Object key) {
		return features.containsKey(key);
	}

	public double getVectorNorm() {
		double norm = 0.0;
		Iterator<String> it = features.keySet().iterator();
		while(it.hasNext()) {
			norm += Math.pow(features.get(it.next()), 2.0);
		}
		return Math.sqrt(norm);
	}



	// VIEWING

	@Override
	public String toString() {
		return this.toString(features.size());
	}

	private List<KeyValuePair> getOrderedFeatures() {
		List<KeyValuePair> kvpList = new ArrayList<KeyValuePair>(features.size());
		Iterator<String> featureIterator = features.keySet().iterator();
		while(featureIterator.hasNext()) {
			String feature = featureIterator.next();
			double value   = features.get(feature);
			KeyValuePair keyValuePair = new KeyValuePair(feature, value);
			kvpList.add(keyValuePair);
		}
		ScorableComparator comparator = new ScorableComparator(true);
		Collections.sort(kvpList, comparator);

		return kvpList;
	}

	public String toString(int k) {
		DecimalFormat format = new DecimalFormat("#.#########");
		StringBuilder b = new StringBuilder();
		List<KeyValuePair> kvpList = getOrderedFeatures();
		Iterator<KeyValuePair> it = kvpList.iterator();
		int i=0;
		while(it.hasNext() && i++ < k) {
			KeyValuePair pair = it.next();
			b.append(format.format(pair.getScore()) + " " + pair.getKey() + "\n");
		}
		return b.toString();

	}


	// UTILS
	private List<String> analyze(String text) {
		List<String> result = new LinkedList<String>();
		try {
			TokenStream stream = null;
			stream = analyzer.tokenStream("text", new StringReader(text));

			CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
			stream.reset();
			while(stream.incrementToken()) {
				String term = charTermAttribute.toString();
				result.add(term);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static FeatureVector interpolate(FeatureVector x, FeatureVector y, double xWeight) {
		FeatureVector z = new FeatureVector(null);
		Set<String> vocab = new HashSet<String>();
		vocab.addAll(x.getFeatures());
		vocab.addAll(y.getFeatures());
		Iterator<String> features = vocab.iterator();
		while(features.hasNext()) {
			String feature = features.next();
			double weight  = 0.0;
			if(xWeight >= 0 && xWeight <= 1) {
				weight = xWeight*x.getFeatureWeight(feature) + (1.0-xWeight)*y.getFeatureWeight(feature);
			} else {
				System.err.println("Mixing weight is not between 0 and 1. Performing unweighted mixing.");
				weight = x.getFeatureWeight(feature) + y.getFeatureWeight(feature);
			}
			z.addTerm(feature, weight);
		}
		return z;
	}

	public FeatureVector deepCopy() {
		FeatureVector copy = new FeatureVector(null);
		Iterator<String> terms = features.keySet().iterator();
		while(terms.hasNext()) {
			String term = terms.next();
			double weight = features.get(term);
			copy.addTerm(term, weight);
		}
		return copy;
	}

	public static void main(String[] args) {
		String text = "four score seven years ago forefathers fathers mothers kids";

		Stopper stopper = new Stopper();
		stopper.addStopword("this");
		stopper.addStopword("is");
		stopper.addStopword("better");
		
		FeatureVector featureVector = new FeatureVector(text, stopper);
		System.out.println(featureVector.getFeatureCount());
		featureVector.clip(5);
		System.out.println(featureVector.getFeatureCount());
	}


}
