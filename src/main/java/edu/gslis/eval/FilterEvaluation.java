 package edu.gslis.eval;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.gslis.searchhits.SearchHit;



public class FilterEvaluation {
	private List<SearchHit> results;
	private Qrels qrels;

	private Map<String,List<Double>> aggregateStats;
	
	
	public FilterEvaluation(Qrels qrels) {
		this.qrels = qrels;
		
		aggregateStats = new HashMap<String,List<Double>>();
	}
	

	public void setResults(List<SearchHit> results) {
		this.results = results;
	}
	

	

	


	

	public double precision(String queryName) {
		double numRelRet = this.numRelRet(queryName);
		if(numRelRet==0.0) {
			return 0.0;
		}
		
		double numRet    = (double)results.size();

		
		return numRelRet / numRet;
		
	}
	

	public double recall(String queryName) {
		double numRelRet = this.numRelRet(queryName);
		if(numRelRet==0.0) {
			return 0.0;
		}
		
		double numRel = qrels.numRel(queryName);

		//System.err.println("numRel: " + numRel);
		
		return numRelRet / numRel;
		
	}
	
	public double numRelRet(String queryName) {


		
		double relRet  = 0.0;
		
		Iterator<SearchHit> resultIterator = results.iterator();
		while(resultIterator.hasNext()) {
			SearchHit result = resultIterator.next();
			if(qrels.isRel(queryName, result.getDocno())) {
				relRet += 1.0;
			}
		}
		
		return (double)relRet;
	}
	
	/**
	 * calculate F1 for a particular query
	 * @param queryName the TREC-assigned query ID
	 * @return F1
	 * @throws Exception 
	 */
	public double f1Query(String queryName) {

		
		double precision = this.precision(queryName);
		double recall    = this.recall(queryName);
		
		double f = 2 * (precision * recall) / (precision + recall);
		
		if(Double.isNaN(f)) {
			return 0.0;
		}
		return f;
	}
	
	/**
	 * calculate Fbeta for a particular query
	 * @param queryName the TREC-assigned query ID
	 * @param beta Beta for weighting precision.  beta=1 yields F1 equivalence.
	 * @return Fbeta
	 * @throws Exception 
	 */
	public double f1Query(String queryName, double beta) throws Exception {

		beta *= beta;
		
		double precision = this.precision(queryName);
		double recall    = this.recall(queryName);
		
		double f =  (1 + beta) * (precision * recall) / (beta * precision + recall);
		
		if(Double.isNaN(f)) {
			return 0.0;
		}
		return f;
	}
	
	public double t11su(String queryName) {
		double t = 0.0;
		double minU = 0.5;
		double truePos  = this.numRelRet(queryName);
		double falsePos = (double)results.size() - truePos;
		
		t = (2.0 * truePos - falsePos) / qrels.numRel(queryName);
		
		if(Double.isNaN(t)) 
			t = 0.0;
		
		t = (Math.max(t, minU) - minU) / (1.0 - minU);
		
		return t;
	}
	
	public double utility(String queryName, double truePosWeight) {
		double truePos  = this.numRelRet(queryName);
		double falsePos = (double)results.size() - truePos;
		
		return Math.max(-100, truePosWeight * truePos - falsePos);
		
	}
	
	public void addStat(String statName, double value) {
		List<Double> stats = null;
		if(aggregateStats.containsKey(statName)) {
			stats = aggregateStats.get(statName);
		} else {
			stats = new LinkedList<Double>();
		}
		stats.add(value);
		
		aggregateStats.put(statName, stats);
	}
	
	public double avg(String statName) {
		if(! aggregateStats.containsKey(statName)) {
			return -1.0;
		}
		
		double mean = 0.0;
		
		Iterator<Double> statIterator = aggregateStats.get(statName).iterator();
		while(statIterator.hasNext()) {
			mean += statIterator.next();
		}
		
		mean /= (double)aggregateStats.get(statName).size();
		
		return mean;
	}
	
}
