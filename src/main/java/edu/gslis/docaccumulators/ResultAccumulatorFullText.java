package edu.gslis.docaccumulators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.ScoredExtentResult;
import edu.gslis.eval.Qrels;
import edu.gslis.indexes.IndexWrapper;
import edu.gslis.indexes.IndexWrapperIndriImpl;
import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.UnscoredSearchHit;
import edu.gslis.textrepresentation.FeatureVector;


public class ResultAccumulatorFullText {

	private GQuery gQuery;
	private QueryEnvironment env;
	private IndexWrapper index;
	private Map<Integer,UnscoredSearchHit> accumulatedFilteredDocs;
	private Map<Integer,FeatureVector> accumulatedDocVectors;
	private Qrels qrels;

	// inner class for sorting hits on time
	private class RealTimeDocChronologicalComparator implements Comparator<UnscoredSearchHit>{
		private boolean decreasing = true;

		public RealTimeDocChronologicalComparator(boolean decreasing) {
			this.decreasing = decreasing;
		}
		public int compare(UnscoredSearchHit x, UnscoredSearchHit y) {
			double xVal = x.getEpoch();
			double yVal = y.getEpoch();

			if(decreasing) {
				return (xVal > yVal  ? -1 : (xVal == yVal ? 0 : 1));
			} else {
				return (xVal < yVal  ? -1 : (xVal == yVal ? 0 : 1));
			}
		}	
	}

	public ResultAccumulatorFullText(IndexWrapperIndriImpl indexWrapper, GQuery gQuery) {
		// danger!  assumes we've got an indri index
		this.env = (QueryEnvironment)indexWrapper.getActualIndex();
		this.index = indexWrapper;
		this.gQuery = gQuery;
		accumulatedFilteredDocs = new HashMap<Integer,UnscoredSearchHit>();
		accumulatedDocVectors   = new HashMap<Integer,FeatureVector>();
	}

	public ResultAccumulatorFullText(IndexWrapper indexWrapper) {
		this.env = (QueryEnvironment)indexWrapper.getActualIndex();
		this.index = indexWrapper;		
		accumulatedFilteredDocs = new HashMap<Integer,UnscoredSearchHit>();
		accumulatedDocVectors   = new HashMap<Integer,FeatureVector>();
	}
	

	public void accumulateNMostRelevant(int n) {
		// assumes our query text already has a constraint clause, e.g. #1(foo bar baz)
		String bandConstraint = gQuery.getMetadata("constraint");

		try {
			// accumulate all possibly relevant docs
			ScoredExtentResult[] res = env.runQuery(bandConstraint, 1000000);
			String[] docnos    = env.documentMetadata(res, "docno");
			String[] epochs    = env.documentMetadata(res, "epoch");

			int k=0;
			for(ScoredExtentResult r: res) {
				if(k >= n)
					break;
				String docno = docnos[k];
				int docID = r.document;
				double length = (double)env.documentLength(docID);
				double epoch = Double.parseDouble(epochs[k]);
				UnscoredSearchHit hit = new UnscoredSearchHit(docno, docID, length, epoch);
				accumulatedFilteredDocs.put(docID, hit);
				k++;
			}


			
			toFullText(accumulatedFilteredDocs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void accumulateNMostRecent(int n) {
		// assumes our query text already has a constraint clause, e.g. #1(foo bar baz)
		String bandConstraint = gQuery.getMetadata("constraint");

		try {
			// accumulate all possibly relevant docs
			ScoredExtentResult[] res = env.expressionList(bandConstraint);
			String[] docnos    = env.documentMetadata(res, "docno");
			String[] epochs    = env.documentMetadata(res, "epoch");

			int k=0;
			for(ScoredExtentResult r: res) {
				String docno = docnos[k];
				int docID = r.document;
				double length = (double)env.documentLength(docID);
				double epoch = Double.parseDouble(epochs[k]);
				UnscoredSearchHit hit = new UnscoredSearchHit(docno, docID, length, epoch);
				accumulatedFilteredDocs.put(docID, hit);
				k++;
			}


			// if n < 1, we want all docs
			if(n > 0) {
				List<UnscoredSearchHit> ordered = getChronologicallyOrderedDocs();
				int end   = ordered.size();
				int start = Math.max(0, end - n);

				List<UnscoredSearchHit> recent = new ArrayList<UnscoredSearchHit>(n);
				for(k=start; k<ordered.size(); k++) {
					recent.add(ordered.get(k));
				}

				accumulatedFilteredDocs = new HashMap<Integer,UnscoredSearchHit>(recent.size());
				Iterator<UnscoredSearchHit> it = recent.iterator();
				while(it.hasNext()) {
					UnscoredSearchHit hit = it.next();
					int docId = hit.getDocID();
					accumulatedFilteredDocs.put(docId, hit);
				}
			}
			toFullText(accumulatedFilteredDocs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void accumulateNOldest(int n) {
		// assumes our query text already has a constraint clause, e.g. #1(foo bar baz)
		String bandConstraint = gQuery.getMetadata("constraint");

		try {
			// accumulate all possibly relevant docs
			ScoredExtentResult[] res = env.expressionList(bandConstraint);
			String[] docnos    = env.documentMetadata(res, "docno");
			String[] epochs    = env.documentMetadata(res, "epoch");

			int k=0;
			for(ScoredExtentResult r: res) {
				String docno = docnos[k];
				int docID = r.document;
				double length = (double)env.documentLength(docID);
				double epoch = Double.parseDouble(epochs[k]);
				UnscoredSearchHit hit = new UnscoredSearchHit(docno, docID, length, epoch);
				accumulatedFilteredDocs.put(docID, hit);
				k++;
			}



				List<UnscoredSearchHit> ordered = getChronologicallyOrderedDocs();
		
				List<UnscoredSearchHit> recent = new ArrayList<UnscoredSearchHit>(n);
				for(k=0; k<n && k<ordered.size(); k++) {
					recent.add(ordered.get(k));
				}

				accumulatedFilteredDocs = new HashMap<Integer,UnscoredSearchHit>(recent.size());
				Iterator<UnscoredSearchHit> it = recent.iterator();
				while(it.hasNext()) {
					UnscoredSearchHit hit = it.next();
					int docId = hit.getDocID();
					accumulatedFilteredDocs.put(docId, hit);
				}
			
			toFullText(accumulatedFilteredDocs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	


	
	public void accumulateNMostRecentRel(int n) {
		
		try {
			Set<String> relDocnos = qrels.getRelDocs(gQuery.getTitle());
			if(relDocnos == null || relDocnos.size() < 1)
				return;
			
			Iterator<String> docIt = relDocnos.iterator();
			while(docIt.hasNext()) {
				String docno = docIt.next();
				int docID = index.getDocId(docno);
				double length = index.getDocLength(docID);
				if(length < 1)
					continue;
				double epoch = Double.parseDouble(index.getMetadataValue(docno, "epoch"));
				UnscoredSearchHit hit = new UnscoredSearchHit(docno, docID, length, epoch);
				accumulatedFilteredDocs.put(docID, hit);
			}
			System.err.println("total rel in train qrels: " + accumulatedFilteredDocs.size());
			


			List<UnscoredSearchHit> ordered = getChronologicallyOrderedDocs();
			int end   = ordered.size();
			int start = Math.max(0, end - n);

			int relFound = 0;
			List<UnscoredSearchHit> recent = new ArrayList<UnscoredSearchHit>(n);
			for(int k=start; k<ordered.size(); k++) {
				UnscoredSearchHit hit = ordered.get(k);
				if(qrels.isRel(gQuery.getTitle(), hit.getDocno())) {
					recent.add(ordered.get(k));
					relFound++;
				}
				if(relFound >= n)
					break;
			}

			accumulatedFilteredDocs = new HashMap<Integer,UnscoredSearchHit>(recent.size());
			Iterator<UnscoredSearchHit> it = recent.iterator();
			while(it.hasNext()) {
				UnscoredSearchHit hit = it.next();
				int docId = hit.getDocID();
				accumulatedFilteredDocs.put(docId, hit);
			}

			toFullText(accumulatedFilteredDocs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("total retained rel: " + accumulatedFilteredDocs.size());

	}
	


	public void accumulate() {
		// assumes our query text already has a constraint clause, e.g. #1(foo bar baz)
		String bandConstraint = gQuery.getMetadata("constraint");

		try {
			// accumulate all possibly relevant docs
			ScoredExtentResult[] res = env.expressionList(bandConstraint);
			String[] docnos    = env.documentMetadata(res, "docno");
			String[] epochs    = env.documentMetadata(res, "epoch");

			int k=0;
			for(ScoredExtentResult r: res) {
				String docno = docnos[k];
				int docID = r.document;
				double length = (double)env.documentLength(docID);
				double epoch = -1.0;
				try {
					epoch = Double.parseDouble(epochs[k]);
				} catch (Exception e) {
					System.err.println("failed to parse epoch: " + epochs[k] + " in docid " + docID);
					epoch = Math.random();
				}
				UnscoredSearchHit hit = new UnscoredSearchHit(docno, docID, length, epoch);
				accumulatedFilteredDocs.put(docID, hit);


				FeatureVector docVector = index.getDocVector(docID, null);
				Iterator<String> docTerms = docVector.iterator();
				while(docTerms.hasNext()) {
					String docTerm = docTerms.next();
					hit.addFeature(docTerm, docVector.getFeatureWeight(docTerm));
				}
				accumulatedDocVectors.put(docID,  docVector);

				k++;
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void accumulate(String constraint) {
		// assumes our query text already has a constraint clause, e.g. #1(foo bar baz)
		String bandConstraint = constraint;

		try {
			// accumulate all possibly relevant docs
			ScoredExtentResult[] res = env.expressionList(bandConstraint);
			String[] docnos    = env.documentMetadata(res, "docno");
			String[] epochs    = env.documentMetadata(res, "epoch");

			int k=0;
			for(ScoredExtentResult r: res) {
				String docno = docnos[k];
				int docID = r.document;
				double length = (double)env.documentLength(docID);
				double epoch = Double.parseDouble(epochs[k]);
				UnscoredSearchHit hit = new UnscoredSearchHit(docno, docID, length, epoch);
				accumulatedFilteredDocs.put(docID, hit);


				FeatureVector docVector = index.getDocVector(docID, null);
				Iterator<String> docTerms = docVector.iterator();
				while(docTerms.hasNext()) {
					String docTerm = docTerms.next();
					hit.addFeature(docTerm, docVector.getFeatureWeight(docTerm));
				}
				accumulatedDocVectors.put(docID,  docVector);

				k++;
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void accumulateApprox() {
		// assumes our query text already has a constraint clause, e.g. #1(foo bar baz)
		String bandConstraint = gQuery.getMetadata("constraint");

		try {
			// accumulate all possibly relevant docs
			ScoredExtentResult[] res = env.runQuery(bandConstraint, 5000); //env.expressionList(bandConstraint);
			String[] docnos    = env.documentMetadata(res, "docno");
			String[] epochs    = env.documentMetadata(res, "epoch");

			int k=0;
			for(ScoredExtentResult r: res) {
				String docno = docnos[k];
				int docID = r.document;
				double length = (double)env.documentLength(docID);
				Double epoch = 1.0;
				try {
					epoch = Double.parseDouble(epochs[k]);
				} catch (Exception e) {
					;
				}
				UnscoredSearchHit hit = new UnscoredSearchHit(docno, docID, length, epoch);
				accumulatedFilteredDocs.put(docID, hit);


				FeatureVector docVector = index.getDocVector(docID, null);
				Iterator<String> docTerms = docVector.iterator();
				while(docTerms.hasNext()) {
					String docTerm = docTerms.next();
					hit.addFeature(docTerm, docVector.getFeatureWeight(docTerm));
				}
				accumulatedDocVectors.put(docID,  docVector);

				k++;
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void toFullText(Map<Integer,UnscoredSearchHit> toBeFilled) {

		try {

			Map<Integer,UnscoredSearchHit> fullTextHits = new HashMap<Integer,UnscoredSearchHit>(toBeFilled.size());
			Iterator<Integer> u = toBeFilled.keySet().iterator();
			while(u.hasNext()) {
				int docId = u.next();
				UnscoredSearchHit uHit = toBeFilled.get(docId);
				UnscoredSearchHit fullTextHit = new UnscoredSearchHit(
						uHit.getDocno(), 
						uHit.getDocID(),
						uHit.getLength(),
						uHit.getEpoch());

				FeatureVector docVector = index.getDocVector(uHit.getDocID(), null);
				Iterator<String> docTerms = docVector.iterator();
				while(docTerms.hasNext()) {
					String docTerm = docTerms.next();
					fullTextHit.addFeature(docTerm, docVector.getFeatureWeight(docTerm));
				}
				fullTextHits.put(docId, fullTextHit);
				accumulatedDocVectors.put(uHit.getDocID(),  docVector);
			}	
			accumulatedFilteredDocs = fullTextHits;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}






	public Map<Integer,UnscoredSearchHit> getAccumulatedDocs() {
		return accumulatedFilteredDocs;
	}
	public FeatureVector getDocVector(int docID) {
		if(! accumulatedDocVectors.containsKey(docID))
			return new FeatureVector(null);

		return accumulatedDocVectors.get(docID);
	}

	public List<UnscoredSearchHit> getChronologicallyOrderedDocs() {
		List<UnscoredSearchHit> docsToProcess = new ArrayList<UnscoredSearchHit>(accumulatedFilteredDocs.size());
		Iterator<Integer> docIt = accumulatedFilteredDocs.keySet().iterator();
		while(docIt.hasNext()) {
			docsToProcess.add(accumulatedFilteredDocs.get(docIt.next()));
		}
		// sort chronologically
		RealTimeDocChronologicalComparator comparator = new RealTimeDocChronologicalComparator(false);
		Collections.sort(docsToProcess, comparator);

		return docsToProcess;
	}


	public void setQrels(Qrels qrels) {
		this.qrels = qrels;
	}

}
