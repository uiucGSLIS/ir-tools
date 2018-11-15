package edu.gslis.scoring.expansion;

import edu.gslis.queries.GQuery;
import edu.gslis.scoring.DocScorer;
import edu.gslis.scoring.DocScorerWithDocumentPrior;
import edu.gslis.scoring.queryscoring.QueryLikelihoodQueryScorer;
import edu.gslis.searchhits.SearchHit;

/**
 * Computes the relevance model score for a given term.
 * 
 * <p>This class misuses {@link DocScorerWithDocumentPrior} as its superclass by treating the query likelihood as a "document prior".
 * For this reason, you will need to instantiate a new RelevanceModelScorer for each query.
 * 
 * @author Garrick
 *
 */
public class RelevanceModelScorer extends DocScorerWithDocumentPrior {
	
	private QueryLikelihoodQueryScorer queryScorer;
	private GQuery query;
	
	/**
	 * @param termScorer Some DocScorer capable of producing P(w|D).
	 * @param queryScorer A QueryLikelihoodQueryScorer to produce P(Q|D).
	 * @param query A query
	 */
	public RelevanceModelScorer(DocScorer termScorer, QueryLikelihoodQueryScorer queryScorer, GQuery query) {
		super(termScorer);
		this.queryScorer = queryScorer;
		this.query = query;
	}
	
	@Override
	public double getPrior(SearchHit document) {
		return Math.exp(queryScorer.scoreQuery(query, document));
	}

}
