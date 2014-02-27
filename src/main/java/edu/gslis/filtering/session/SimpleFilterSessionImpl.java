package edu.gslis.filtering.session;

import java.util.Iterator;
import java.util.List;

import edu.gslis.docaccumulators.ResultAccumulator;
import edu.gslis.docscoring.Scorer;
import edu.gslis.eval.Qrels;
import edu.gslis.filtering.threshold.SimpleCutoffThresholdClassifier;
import edu.gslis.filtering.threshold.ThresholdClassifier;
import edu.gslis.filtering.threshold.ThresholdFinderParamSweep;
import edu.gslis.indexes.IndexWrapper;
import edu.gslis.indexes.IndexWrapperIndriImpl;
import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.searchhits.UnscoredSearchHit;

public class SimpleFilterSessionImpl implements FilterSession {
	private IndexWrapper trainIndex;
	private IndexWrapper testIndex;
	private Qrels trainQrels;
	private ThresholdClassifier thresholder;
	private Scorer scorer;
	
	private GQuery query;
	
	public  SimpleFilterSessionImpl(GQuery query, 
			IndexWrapper trainIndex,
			IndexWrapper testIndex,
			Qrels trainQrels,
			Scorer scorer) {
		
		this.query = query;
		this.trainIndex = trainIndex;
		this.trainQrels = trainQrels;
		this.testIndex  = testIndex;
		this.scorer = scorer;
		
	}



	public void train() {
		ResultAccumulator accumulator = new ResultAccumulator((IndexWrapperIndriImpl)trainIndex, 
				query.getText(), query.getMetadata(NAME_OF_CONSTRAINT_FIELD));
		
		//ResultAccumulatorFullText accumulator = new ResultAccumulatorFullText((IndexWrapperIndriImpl) trainIndex, 
		//		query);
		
		accumulator.accumulate();
		List<UnscoredSearchHit> trainingAccumulated = accumulator.getChronologicallyOrderedDocs();
		

		SearchHits trainingHits = new SearchHits();
		Iterator<UnscoredSearchHit> docIterator = trainingAccumulated.iterator();
		while(docIterator.hasNext()) {
			UnscoredSearchHit unscoredHit = docIterator.next();
			SearchHit hit = unscoredHit.toSearchHit();
			double score = scorer.score(hit);
			hit.setScore(score);
			trainingHits.add(hit); 
		}	

		
		ThresholdFinderParamSweep optimizer = new ThresholdFinderParamSweep(query.getTitle(), trainingHits, trainQrels);
		thresholder = new SimpleCutoffThresholdClassifier();
		((SimpleCutoffThresholdClassifier)thresholder).setThreshold(optimizer.getThreshold());
	}

	public SearchHits filter() {
		ResultAccumulator accumulator = new ResultAccumulator((IndexWrapperIndriImpl)testIndex, 
				query.getText(), query.getMetadata(NAME_OF_CONSTRAINT_FIELD));
		accumulator.accumulate();
		List<UnscoredSearchHit> testingAccumulated = accumulator.getChronologicallyOrderedDocs();
		

		SearchHits testingEmitted = new SearchHits();
		Iterator<UnscoredSearchHit> docIterator = testingAccumulated.iterator();
		while(docIterator.hasNext()) {
			UnscoredSearchHit unscoredHit = docIterator.next();
			SearchHit hit = unscoredHit.toSearchHit();
			double score = scorer.score(hit);
			hit.setScore(score);
			
			if(thresholder.emit(score))
				testingEmitted.add(hit); 
			
			
		}	
		
		System.err.println("accumulated: " + testingAccumulated.size() + 
				"   emitted: " + testingEmitted.size() + " cutoff: " +
				thresholder.getThreshold());
		
		return testingEmitted;
	}

}
