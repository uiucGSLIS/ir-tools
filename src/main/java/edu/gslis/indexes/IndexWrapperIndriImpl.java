package edu.gslis.indexes;

import java.util.Iterator;

import lemurproject.indri.QueryEnvironment;
import lemurproject.indri.ScoredExtentResult;
import lemurproject.lemur.Index;
import lemurproject.lemur.IndexManager;
import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.textrepresentation.FeatureVector;
import edu.gslis.textrepresentation.IndriDocument;
import edu.gslis.utils.Stopper;



public class IndexWrapperIndriImpl implements IndexWrapper{

    private static final String SERVER_PREFIX = "server:";
    
	private QueryEnvironment index;
	private double vocabularySize = -1.0;
	private double docLengthAvg   = -1.0;
	private String timeFieldName  = null;
	
	public IndexWrapperIndriImpl(String pathToIndex) {
		index = new QueryEnvironment();
		addIndex(pathToIndex);
		getVocabularySize(pathToIndex);
	}


	private void addIndex(String pathToIndex) {
		try {
		    // If the index path starts with 'server:', treat it as a server
		    if (pathToIndex.startsWith(SERVER_PREFIX)) {
		        String server = pathToIndex.substring(SERVER_PREFIX.length());
		        index.addServer(server);
		    }
		    else
		        index.addIndex(pathToIndex);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	   /**
     * Initialize the vocabulary size using the lemur API
     * @param pathToIndex
     */
    private void getVocabularySize(String pathToIndex) {
        if (pathToIndex.startsWith(SERVER_PREFIX))
            System.err.println("Unable to get vocabulary size from remote server. Must use local index.");
        else
        {
            try {
                Index lemurIndex = IndexManager.openIndex(pathToIndex);
                vocabularySize = lemurIndex.termCountUnique();
            } catch (Exception e) {
                System.err.println("Error getting vocabulary size: perhaps liblemur library is missing?");
            }         
        }
    }

	public SearchHits runQuery(GQuery query, int count) {
				
		SearchHits hits = new SearchHits();
		try {
			// assumes that this gQuery's text member is all formatted and ready to go.
			StringBuilder queryString = new StringBuilder("#weight(");
			Iterator<String> qt = query.getFeatureVector().iterator();
			while(qt.hasNext()) {
				String term = qt.next();
				queryString.append(query.getFeatureVector().getFeatureWeight(term) + " " + term + " ");
			}
			queryString.append(")");
			
			ScoredExtentResult[] res = index.runQuery(queryString.toString(), count);
			String[] docnos = index.documentMetadata(res, "docno");
			String[] timeStrings  = null;
			double[] times = null;
			if(timeFieldName != null) {
				timeStrings = index.documentMetadata(res, timeFieldName);
				times = new double[timeStrings.length];
				for(int i=0; i<timeStrings.length; i++) {
					times[i] = Double.parseDouble(timeStrings[i]);
				}
			}


			int k=0;
			for(ScoredExtentResult r : res) {
				SearchHit hit = new SearchHit();
				hit.setDocID(r.document);
				hit.setScore(r.score);

				if(times != null)  {
					hit.setMetadataValue(timeFieldName, times[k]);
				}
				hit.setDocno(docnos[k++]);
				hits.add(hit);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hits;
	}

	public SearchHits runQuery(String query, int count) {
		SearchHits hits = new SearchHits();
		try {
			// assumes that this gQuery's text member is all formatted and ready to go.
			ScoredExtentResult[] res = index.runQuery(query, count);
			String[] docnos = index.documentMetadata(res, "docno");
			String[] timeStrings  = null;
			double[] times = null;
			if(timeFieldName != null) {
				timeStrings = index.documentMetadata(res, timeFieldName);
				times = new double[timeStrings.length];
				for(int i=0; i<timeStrings.length; i++) {
					times[i] = Double.parseDouble(timeStrings[i]);
				}
			}


			int k=0;
			for(ScoredExtentResult r : res) {
				SearchHit hit = new SearchHit();
				hit.setDocID(r.document);
				hit.setScore(r.score);

				if(times != null)  {
					hit.setMetadataValue(timeFieldName, times[k]);
				}
				hit.setDocno(docnos[k++]);
				hits.add(hit);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hits;
	}
	
	public void setTimeFieldName(String timeFieldName) {
		System.err.println("setting time to " + timeFieldName);
		this.timeFieldName = timeFieldName;
	}

	public double docCount() {
		try {
			return (double)index.documentCount();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1.0;
	}

	public double termCount() {
		try {
			return (double)index.termCount();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1.0;
	}

	public double termTypeCount() {
		return vocabularySize;
	}

	public double docFreq(String term) {
		try {
			return (double)index.documentCount(term);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1.0;
	}

	public double termFreq(String term) {
		try {
			return (double)index.termCount(term);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1.0;
	}

	public double docLengthAvg() {
		return docLengthAvg;
	}

	public FeatureVector getDocVector(int docID, Stopper stopper) {
		IndriDocument doc = new IndriDocument(index);
		return doc.getFeatureVector(docID, stopper);
	}

	public FeatureVector getDocVector(String docno, Stopper stopper) {
		IndriDocument doc = new IndriDocument(index);
		int docID = 1;
		try {
			docID = doc.getDocID(docno);
			if(docID < 1) {
				System.err.println("no doc ID found for docno " + docno);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.getDocVector(docID, stopper);
	}
	

	public Object getActualIndex() {
		return index;
	}

	public String getMetadataValue(String docno, String metadataName) {
		String value = null;
		String[] d = {docno};
		try {
			int[] i    = index.documentIDsFromMetadata("docno", d);
			String[] v = index.documentMetadata(i, metadataName);
			if(v.length != d.length) {
				System.err.println("got mismatch of metadata and docnos in IndexWrapperIndriImpl.getMetadataValue()");
				return null;
			}
			value = v[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	public double getDocLength(int docID) {
		double length = 0; 
		try {
			length = (double)index.documentLength(docID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return length;
	}

	public int getDocId(String docno) {
		try {
			String[] d = {docno};
			int[] docIds = index.documentIDsFromMetadata("docno", d);
			if(docIds == null || docIds.length == 0)
				return -1;
			return docIds[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * Return a single SearchHit for the specified docno
	 * @param docno
	 * @param stopper
	 * @return
	 */
   public SearchHit getSearchHit(String docno, Stopper stopper) {
       SearchHit hit = new SearchHit();
       FeatureVector dv = getDocVector(docno, stopper);
       int docid = getDocId(docno);
       hit.setFeatureVector(dv);
       hit.setDocID(docid);
        
       String timeString = getMetadataValue(docno, timeFieldName);
       if (timeString != null) {
           double time = Double.parseDouble(timeString);
           hit.setMetadataValue(timeFieldName, time);
       }
       return hit;
   }
}
