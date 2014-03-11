package edu.gslis.docscoring;

import java.util.Iterator;

import edu.gslis.docscoring.smart.IDFWeights;
import edu.gslis.docscoring.smart.IDFWeights.IDFWeight;
import edu.gslis.docscoring.smart.Normalizers;
import edu.gslis.docscoring.smart.Normalizers.Normalizer;
import edu.gslis.docscoring.smart.TFWeights;
import edu.gslis.docscoring.smart.TFWeights.TFWeight;
import edu.gslis.indexes.IndexWrapper;
import edu.gslis.indexes.IndexWrapperIndriImpl;
import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.textrepresentation.FeatureVector;
import edu.gslis.utils.Stopper;

/**
 * Implements a SMART-style scorer with support for different
 * query and document TF, IDF, and normalizer functions.
 * 
 * The weights and normalizers are specified in the constructor
 * via the "spec" parameter using the SMART notation:
 * 
 *   ddd.qqq
 * 
 * First triplet:   term weighting of document vector
 * Second triplet:  gives term weighting of query vector
 * First position:  term frequency component
 * Second position: document/collection frequency component
 * Third position:  form of normalization used
 * 
 * For example:
 *    ltu.Lnu    
 *              Document: log tf, idf, pivoted unique normalization
 *              Query:    log average tf, no idf, pivoted unique normalization
 *          
 *    lnc.ltc
 *              Document: log tf, log no idf, cosine normalization
 *              Query:    log tf, log idf, cosine normalization
 *    
 * Term frequency component:
 *  n,x (no-op)                          
 *  t (raw)                              
 *  l (logarithm)       TFLogWeight         
 *  a (augmented)       TFAugmentedWeight 
 *  b (binary)          TFBinaryWeight 
 *  L (log avg)         TFAverageWeight
 *  m (max)             TFMaxWeight
 *  s (square)          TFSquareWeight
 *                      
 * Collection frequency component:
 *  x,n (none)                       
 *  t,i (idf)           LogIDFWeight 
 *  p (prob idf)        IDFProbWeight 
 *  f (coll freq)       IDFFreqWeight
 *  s (square)          IDFSquareWeight
 *  P (phrase)          (Not implemented)                    
 *  
 * Normalization
 *  x,n (none)          
 *  c (cosine)          CosineNormalizer
 *  u (pivoted unique)  PivotedUniqueNormalizer (Incomplete)
 *  b (byte size)       (Not implemented)
 *  f (fourth)          FourthNormalizer
 *  m (max)             MaxNormalizer
 *  s (sum)             SumWeightNormalizer
 *                      
 *                      
 * Sources:
 *  SMART 11.0 (see details at bottom of this file)
 *  Salton, G. and Buckley, C. (1988). Term-weighting approaches in
 *     automatic text retrieval. Information Processing and Management 24 (5). 
 *  Singhal, A. (1997). Term weighting revisited. Dissertation.
 *  
 */


public class ScorerSMART implements Scorer 
{    
    /* Query for this scorer instance */
    GQuery gQuery;

    /* Query environment to for collection statistics */ 
    IndexWrapper index = null;
   
    /* Number of documents in collection*/
    double numDocs = 0;
    /* Total terms in collection */
    double totalTerms = 0;
    /* Average document length */
    double avgDocLength = 0;
    /* Average unique terms per document */
    double avgUniqueTerms = 0;
    
    /* Weights and normalizers */
    FeatureVector qfv = null;
    TFWeight queryTF = null;
    TFWeight docTF =null;
    IDFWeight queryIDF = null;
    IDFWeight docIDF = null;
    Normalizer queryNormalizer = null;
    Normalizer docNormalizer = null;

    Stopper stopper = new Stopper();
    
    public static void main(String[] args) throws Exception {
        // Quick test
        Stopper stopper = new Stopper();
        
        FeatureVector qfv = new FeatureVector(stopper);
        qfv.addTerm("falkland");
        qfv.addTerm("petroleum");
        qfv.addTerm("exploration");
        
        GQuery query = new GQuery();
        query.setTitle("351");
        query.setText("falkland petroleum exploration");
        query.setFeatureVector(qfv);
        
        String indexPath = "/Users/cwillis/dev/uiucGSLIS/indexes/FT.test";
        double uniqueTerms = 263757;
        IndexWrapper index = new IndexWrapperIndriImpl(indexPath);
        String spec = "ltc.lnc";
        Scorer scorer = new ScorerSMART(query, index, spec, stopper, uniqueTerms);
        SearchHits hits = index.runQuery(query, 10);
        Iterator<SearchHit> it = hits.iterator();
        while (it.hasNext()) {
            SearchHit hit = it.next();
            double score = scorer.score(hit);
            System.out.println (hit.getDocID() + ": " + score);
        }
    }
    
    /**
     * Constructor
     * @param query     Query for this instance
     * @param qe        Query environment for collection statistics
     * @param spec      SMART notation specification (e.g., ltc.lnc)
     * @param uniqeTerms    Number of unique terms (output from dumpindex)
     * @throws Exception
     */
    public ScorerSMART(GQuery query, IndexWrapper index, 
            String spec, Stopper stopper, double uniqueTerms) 
            throws Exception 
    {
        this.gQuery = query;
        this.index = index;
        this.numDocs = index.docCount();
        this.totalTerms = index.termCount();
        this.avgDocLength = totalTerms/numDocs;
        this.avgUniqueTerms = uniqueTerms/numDocs;
        this.stopper = stopper;
        initWeights(spec);
    }
    
    /**
     * Instantiates weights and normalizers based on the
     * SMART notation spec.
     * @param spec
     * @throws Exception
     */
    void initWeights(String spec) throws Exception 
    {
        this.docTF = TFWeights.getTFWeight(spec.charAt(0));
        this.docIDF = IDFWeights.getIDFWeight(spec.charAt(1));
        docIDF.setNumDocs(numDocs);
        docIDF.setIndex(index);
        this.docNormalizer = Normalizers.getNormalizer(spec.charAt(2));
        docNormalizer.setAvgDocLen(avgDocLength);
        docNormalizer.setAvgUniqueTerms(avgUniqueTerms);
        this.queryTF = TFWeights.getTFWeight(spec.charAt(4));
        this.queryIDF = IDFWeights.getIDFWeight(spec.charAt(5));
        queryIDF.setNumDocs(numDocs);
        queryIDF.setIndex(index);
        this.queryNormalizer = Normalizers.getNormalizer(spec.charAt(6));
        queryNormalizer.setAvgDocLen(avgDocLength); 
        queryNormalizer.setAvgUniqueTerms(avgUniqueTerms);
        
        qfv = gQuery.getFeatureVector();
        queryTF.weight(qfv);
        queryIDF.weight(qfv);
        queryNormalizer.normalize(qfv);

    }
    
    
    /**
     * Get the document vector for the current document.
     * @param doc
     * @return
     * @throws Exception
     */
    public FeatureVector getDocumentVector(SearchHit doc) throws Exception 
    {    
        return index.getDocVector(doc.getDocID(), stopper);
    }

    /**
     * CosineSimilarity score
     * Given a document, for each query term,
     * calculate the document and query weight
     * and normalize
     * @return
     */
    public double score(SearchHit doc) 
    {
        double score = 0.0;        
        try 
        {
            
            FeatureVector dfv = getDocumentVector(doc);
            
            docTF.weight(dfv);
            docIDF.weight(dfv);
            docNormalizer.normalize(dfv);
            
            Iterator<String> queryIterator = qfv.iterator();        
            while(queryIterator.hasNext()) {
                String feature = queryIterator.next();
                double wt_q = qfv.getFeatureWeight(feature);
                double wft_d = dfv.getFeatureWeight(feature); 
                score += wft_d * wt_q;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return score;        
    }
   

    public void setParameter(String paramName, double paramValue) {
        // Not used 
    }
    /* Set/get the query TF weight*/
    public void setQueryTFWeight(TFWeight weight) {
        this.queryTF = weight;
    }
    public TFWeight getQueryTFWeight() {
        return queryTF;
    }
    
    
    /* Set/get the query IDF weight */
    public void setQueryIDFWeight(IDFWeight weight) {
        this.queryIDF = weight;
    }
    public IDFWeight getQueryIDFWeight() {
        return queryIDF;
    }
    
    /* Set/get the query normalizer */
    public void setQueryNormalizer(Normalizer normalizer) {
        this.queryNormalizer = normalizer;
    }
    public Normalizer getQueryNormalizer() {
        return queryNormalizer;
    }
    
    /* Set the doc TF weight */
    public void setDocTFWeight(TFWeight weight) {
        this.docTF = weight;
    }
    public TFWeight getDocTFWeight() {
        return docTF;
    }
    
    /* Set the doc IDF weight */
    public void setDocIDFWeight(IDFWeight weight) {
        this.docIDF = weight;
    }
    public IDFWeight getDocIDFWeight() {
        return docIDF;
    }
    
    /* Set/get the doc normalizer */
    public void setDocNormalizer(Normalizer normalizer) {
        this.docNormalizer = normalizer;
    }
    public Normalizer getDocNormalizer() {
        return docNormalizer;
    }
}
