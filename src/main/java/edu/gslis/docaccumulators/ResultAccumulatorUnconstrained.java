package edu.gslis.docaccumulators;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.validator.GenericValidator;

import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.ScoredExtentResult;
import edu.gslis.indexes.IndexWrapperIndriImpl;
import edu.gslis.searchhits.UnscoredSearchHit;
import edu.gslis.textrepresentation.FeatureVector;

public class ResultAccumulatorUnconstrained {

	private String query;
	private QueryEnvironment env;
	private Map<Integer,UnscoredSearchHit> accumulatedFilteredDocs;
	
	public ResultAccumulatorUnconstrained(IndexWrapperIndriImpl indexWrapper, String query) {
		
		// danger!  assumes we've got an indri index
		this.env = (QueryEnvironment)indexWrapper.getActualIndex();
		this.query = query;
		accumulatedFilteredDocs = new HashMap<Integer,UnscoredSearchHit>();
	}

	public void accumulate() {
		try 
		{
			// iterate over each query term
			FeatureVector surfaceForm = new FeatureVector(query, null);
			Iterator<String> featureIterator = surfaceForm.iterator();
			while(featureIterator.hasNext()) {
				String feature = featureIterator.next();
				
				String featureQuery = "#band(" + feature + ")";
				
				ScoredExtentResult[] featureResults = env.expressionList(featureQuery);

				String[] docnos   = env.documentMetadata(featureResults, "docno");
				String[] epochs   = env.documentMetadata(featureResults, "epoch");
				
				if(featureResults.length==0)
					continue;
				// convert expression list to term-doc counts
				int[] docIds = this.extractDocIds(featureResults);
				PostingsAggregator postingsAggregator = new PostingsAggregator();
				Postings postingsForFeature = postingsAggregator.aggregate(docIds);
				Iterator<Integer> matchingDocIdIterator = postingsForFeature.docIdIterator();
				int k=0;
				while(matchingDocIdIterator.hasNext()) {
					int docId = matchingDocIdIterator.next();
					
	                UnscoredSearchHit hit = accumulatedFilteredDocs.get(docId);
	                if(hit == null) {
	                    String docno = docnos[k];
	                    double length = (double)env.documentLength(docId);
	                    
	                    double epoch = 0;
	                    if (GenericValidator.isDouble(epochs[k]))
	                        epoch = Double.parseDouble(epochs[k]);
	                    hit = new UnscoredSearchHit(docno, docId, length, epoch);
	                }
					    
					int count = postingsForFeature.lookup(docId);
														
					hit.addFeature(feature, count);
					accumulatedFilteredDocs.put(docId, hit);
				}
				k++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private int[] extractDocIds(ScoredExtentResult[] r) {
		int[] d = new int[r.length];
		for(int i=0; i<r.length; i++) {
			d[i] = r[i].document;
		}
		return d;
	}

	public Map<Integer,UnscoredSearchHit> getAccumulatedDocs() {
		return accumulatedFilteredDocs;
	}
}
