package edu.gslis.indexes;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.SmallFloat;

import edu.gslis.docscoring.ScorerDirichlet;
import edu.gslis.docscoring.support.CollectionStats;
import edu.gslis.docscoring.support.IndexBackedCollectionStatsLucene;
import edu.gslis.lucene.indexer.Indexer;
import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.textrepresentation.FeatureVector;
import edu.gslis.utils.Stopper;

/**
 * IndexWrapper implementation backed by Lucene. See the edu.gslis.lucene package
 * for index-builder and query applications.
 * 
 * A few things to note about this implementation:
 *   
 *   1. DefaultSimilarity: Lucene requires the scorer (Similarity) to be 
 *      set during both indexing and retrieval. Fortunately, we rescore
 *      everything. For now, the DefaultSimilarity is used for both.
 *      
 *   2. Fields: To mimic Indri, the LuceneBuildIndex assumes a unique
 *      identifier called "docno" (Indexer.FIELD_DOCNO) and content field 
 *      called "text" (Indexer.FIELD_TEXT).
 * 
 *   3. Document length: Lucene doesn't store the document length in a useful
 *      way for use. LuceneBuildIndex calculates the document length and
 *      stores it in a separate field called "doclen" (Indexer.FIELD_DOC_LEN).
 *
 */
public class IndexWrapperLuceneImpl implements IndexWrapper 
{
    Logger logger = Logger.getLogger(IndexWrapperLuceneImpl.class.getName());
    
    IndexReader index;
    
	double vocabularySize = -1.0;
	double docLengthAvg   = -1.0;
	String timeFieldName  =  Indexer.FIELD_EPOCH;
	
	public IndexWrapperLuceneImpl(String pathToIndex) {
	    try
	    {
	        index = DirectoryReader.open(FSDirectory.open(new File(pathToIndex)));            
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

	/**
	 * Execute a query given a GQuery object
	 * @param gquery GQuery object
	 * @param count  Number of hits
	 */
    public SearchHits runQuery(GQuery gquery, int count) {
        StringBuilder queryString = new StringBuilder();
        Iterator<String> qt = gquery.getFeatureVector().iterator();
        while(qt.hasNext()) {
            String term = qt.next();
            queryString.append(" ");
            queryString.append(term+"^"+gquery.getFeatureVector().getFeatureWeight(term));
        }
        return runQuery(queryString.toString(), count);
    }
    
    /**
     * Execute a query given a query string
     * @param q   Query string
     * @param count Number of hits
     */
	public SearchHits runQuery(String q, int count) {
				
	    SearchHits hits = new SearchHits();
	    
	    try
	    {            
            IndexSearcher searcher = new IndexSearcher(index);
            Similarity similarity = new DefaultSimilarity();
            Analyzer analyzer = new SimpleAnalyzer(Indexer.VERSION);
            QueryParser parser = new QueryParser(Indexer.VERSION, Indexer.FIELD_TEXT, analyzer);
            Query query = parser.parse(q);
            searcher.setSimilarity(similarity);
            TopDocs topDocs = searcher.search(query,  null, count);
            ScoreDoc[] docs = topDocs.scoreDocs;

            for (int i=0; i<docs.length; i++) {
                SearchHit hit = new SearchHit();
                int docid = docs[i].doc;
                Document d = index.document(docid);
                
                String docno = d.get(Indexer.FIELD_DOCNO);
                hit.setDocID(docid);
                hit.setDocno(docno);
                hit.setScore(docs[i].score);
                IndexableField dl = d.getField(Indexer.FIELD_DOC_LEN);
                if (dl != null) 
                    hit.setLength(dl.numericValue().longValue());
                if(timeFieldName != null) {
                    String timeString = d.get(timeFieldName);
                    double time = Double.parseDouble(timeString);
                    hit.setMetadataValue(timeFieldName, time);
                }
                FeatureVector dv = getDocVector(docid, null);
                hit.setFeatureVector(dv);
                hit.setLength(dv.getLength());
                hits.add(hit);                
            }
	    } catch (Exception e) {
	        logger.log(Level.SEVERE, e.getMessage(), e);
	    }
	    return hits;
	}

	/**
	 * Set the field name used to store the document time.  Defaults
	 * to "epoch".
	 * @param timeFieldName
	 */
	public void setTimeFieldName(String timeFieldName) {
	    logger.info("setting time to " + timeFieldName);
		this.timeFieldName = timeFieldName;
	}

	/**
	 * Returns the total number of documents in the index.
	 */
	public double docCount() {
		try {
			return (double)index.numDocs();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return -1.0;
	}

	/**
	 * Returns the total number of terms across all fields
	 */
	public double termCount() {
	    double count = 0;
		try {
            Fields fields = MultiFields.getFields(index);  
            Iterator<String> it = fields.iterator();
            while (it.hasNext()) {
                String field = it.next();
                count += index.getSumTotalTermFreq(field);
            }
		} catch (Exception e) {
		    logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return count;
	}
	
	/**
	 * Returns the total vocabulary size in text field only.
	 * 
	 */
	public double termTypeCount() {
	    if (vocabularySize == -1) {
	        try {
                Fields fields = MultiFields.getFields(index);  
                Terms terms = fields.terms(Indexer.FIELD_TEXT);
                vocabularySize = terms.size();
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
	    }
	    return vocabularySize;
	}

	/**
	 * Returns the number of documents containing the specified term.
	 * Assumes content in text field.
	 * @param term  Term
	 */
	public double docFreq(String term) {
		try {
			return (double)index.docFreq(new Term(Indexer.FIELD_TEXT, term));
		} catch (Exception e) {
		    logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return -1.0;
	}

	/**
	 * Returns total term frequency in text field.
	 */
	public double termFreq(String term) {
		try {
			return (double)index.totalTermFreq(new Term(Indexer.FIELD_TEXT, term));
		} catch (Exception e) {
		    logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return -1.0;
	}

	/**
	 * Returns average document length
	 */
	public double docLengthAvg() {
	    // TODO: Not implemented
		return docLengthAvg;
	}

	/**
	 * Returns a feature vector for the specified intermal document ID
	 * @param docid    Lucene internal identifier
	 * @param stopper  Stopper
	 */
	public FeatureVector getDocVector(int docID, Stopper stopper) {
	   
	    FeatureVector fv = new FeatureVector(stopper);
	    try
	    {
    	    Terms terms = index.getTermVector(docID, Indexer.FIELD_TEXT);
    	    //Map<Integer, String> dv = new TreeMap<Integer, String>();
    	    if (terms != null) { 
    	        TermsEnum termsEnum = terms.iterator(null); 
    	        //DocsAndPositionsEnum dp = null; 
    	        while (termsEnum.next() != null) { 
    	            String term = termsEnum.term().utf8ToString();
    	           
    	            /*
    	            dp = termsEnum.docsAndPositions(null, dp);
    	            dp.nextDoc();
    	            int freq = dp.freq();
    	            for (int i=0; i<freq; i++) {
    	                int pos = dp.nextPosition();
    	                dv.put(pos, term);
    	            }
    	            */
    	            long f = termsEnum.totalTermFreq();
    	            fv.addTerm(term, f);
    	        }
    	        /*
    	        for (int pos: dv.keySet()) 
    	            System.out.println(pos + " " + dv.get(pos));
    	        */
    	    }
	    } catch (Exception e) {
	        logger.log(Level.SEVERE, e.getMessage(), e);
	    }
	    return fv;
	}

	/**
	 * Returns the internal document identifier given a docno
	 */
	public int getDocId(String docno) {
	    int docid = -1;
        IndexSearcher searcher = new IndexSearcher(index);
        TermQuery q = new TermQuery(new Term(Indexer.FIELD_DOCNO, docno));
	                
	    try
	    {
	        TopDocs docs = searcher.search(q,  1);
	        if (docs.totalHits > 0)
	            docid = docs.scoreDocs[0].doc;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
	        
	    return docid;
	}
	
	/**
	 * Returns a document vector given the docno
	 */
	public FeatureVector getDocVector(String docno, Stopper stopper) {
	    int docid = getDocId(docno);
	    	    
    	return getDocVector(docid, stopper);
	}
	

	/**
	 * Returns the underlying Lucene IndexReader
	 */
	public Object getActualIndex() {
		return index;
	}

	
	/**
	 * Returns the value of a specific field as a string
	 * @param docno  Document number
	 * @param metadataName  Field name
	 */
	public String getMetadataValue(String docno, String metadataName) {
	    int docid = getDocId(docno);
	    String value = null;
	    try {
    	    Document doc = index.document(docid);	    
    	    value = doc.get(metadataName);
	    } catch (Exception e) {
	        logger.log(Level.SEVERE, e.getMessage(), e);
	    }
	    return value;
	}
	


	/**
	 * Returns the document length. Note: this is stored in a custom field during indexing.
	 * @param docid Lucene internal identifier
	 * @see edu.gslis.lucene.main.LuceneBuildIndex
	 */
	public double getDocLength(int docID) {
		double length = -1; 
		try {
		    Document doc = index.document(docID);
		    if (doc != null)
		        length = doc.getField(Indexer.FIELD_DOC_LEN).numericValue().longValue();
		} catch (Exception e) {
		    logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return length;
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
   
   public static void main(String[] args) throws IOException 
   {
       File indexDir = new File("/Users/cwillis/dev/uiucGSLIS/indexes/lucene/FT.train");
       IndexWrapper index = new IndexWrapperLuceneImpl(indexDir.getAbsolutePath());
       double dc = index.docCount();
       assert(dc == 53356);
       
       double df = index.docFreq("cranwell");
       assert (df == 2);
       //double dlavg = index.docLengthAvg();
       int docid = index.getDocId("FT911-1");
       FeatureVector fv1 = index.getDocVector(docid, null);       
       assert(fv1.getFeatureWeight("the") == 11);
       
       double tc = index.termCount();
       assert(tc == 22058714);
       double tf = index.termFreq("the");
       assert(tf == 1425270);
       
       FeatureVector qv = new FeatureVector(null);
       qv.addTerm("raf", 0.8);  
       qv.addTerm("cranwell", 0.5);       

       GQuery query = new GQuery();
       query.setText("raf cranwell");
       query.setFeatureVector(qv);
       
       double ttc = index.termTypeCount();
       assert (ttc == 163279);
//       FeatureVector fv2 = index.getDocVector("test-docno2", null);
//       String epoch = index.getMetadataValue("test-docno1", "epoch");
       // -6.68693  FT911-382   0   1510
       // -6.80837    FT911-1 0   217
       SearchHits hits = index.runQuery(query, 10);
       assert(hits.size() == 10);
       ScorerDirichlet scorer = new ScorerDirichlet();
       scorer.setParameter("mu", 2000);
       scorer.setQuery(query);

       CollectionStats stat = new IndexBackedCollectionStatsLucene();
       stat.setStatSource(indexDir.getAbsolutePath());
       scorer.setCollectionStats(stat);
       
       hits.rank();
       for (int i=0; i<hits.size(); i++) {
           SearchHit hit = hits.getHit(i);
           double score = scorer.score(hit);
           System.out.println(hit.getDocno() + "\t" + hit.getScore() + "\t" + score);
       }
   }
}
