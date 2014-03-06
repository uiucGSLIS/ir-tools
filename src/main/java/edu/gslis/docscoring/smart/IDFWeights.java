package edu.gslis.docscoring.smart;

import java.util.Iterator;

import edu.gslis.indexes.IndexWrapper;
import edu.gslis.textrepresentation.FeatureVector;

/** 
 * Below are the various weighting procedures implemented in the SMART 11.0
 * system from libconvert library:

 * weights_idf.c
 * convert.wt_idf.x Reweighting procedure no-op for idf weights.
 * convert.wt_idf.n Reweighting procedure no-op for idf weights.
 * convert.wt_idf.t Reweight vector by multiplying weight by normal idf factor
 * convert.wt_idf.i Reweight vector by multiplying weight by normal idf factor
 * convert.wt_idf.p Reweight vector by multiplying weight by probabilistic idf factor
 * convert.wf_idf.s Reweight vector by multiplying weight by normal idf factor sqared
 * convert.wf_idf.f Reweight vector by dividing weight by collection freq
 * convert.wf_idf.P Reweight phrase vector by multiplying weight by average of term idf factors
 */
public class IDFWeights {
    

    public static IDFWeight getIDFWeight(char type) throws Exception
    { 
        if (type == 't')
            return new IDFWeights().new LogIDFWeight();
        else if (type == 'p')
            return new IDFWeights().new IDFProbWeight();
        else if (type == 'f')
            return new IDFWeights().new IDFFreqWeight();
        else if (type == 's')
            return new IDFWeights().new IDFSquareWeight();
        else if (type == 'P')
            throw new Exception("IDF weight not implemented '" + type + "'");
        else if (type == 'x' || type == 'n')
            return new IDFWeights().new IDFWeight();
        else 
            throw new Exception("Unsupported IDF weight type '" + type + "'");
    }    

    // Simple interface for IDFWeights
    public class IDFWeight {
        double numDocs = 0;
        IndexWrapper index = null;

        public void weight(FeatureVector fv) throws Exception {}
        public void setNumDocs(double numDocs) {
            this.numDocs = numDocs;
        }
        
        public void setIndex(IndexWrapper index) {
            this.index = index;
        }
    }
    
    // tfidf    new_wt = new_tf * log (num_docs / coll_freq_of_term)
    class LogIDFWeight extends IDFWeight {
        
        public void weight(FeatureVector fv) throws Exception {
            Iterator<String> it = fv.iterator();
            while (it.hasNext()) {
                String term = it.next();
                double weight = fv.getFeatureWeight(term);
                double docFreq = index.docFreq(term);
                weight = weight * Math.log(numDocs/docFreq);
                fv.setTerm(term, weight);
            } 
        }
    }
    
    // prob    new_wt = new_tf * log ((num_docs - coll_freq)   / coll_freq))
    class IDFProbWeight extends IDFWeight {
        
        public void weight(FeatureVector fv) throws Exception {
            Iterator<String> it = fv.iterator();
            while (it.hasNext()) {
                String term = it.next();
                double weight = fv.getFeatureWeight(term);
                double docFreq = index.docFreq(term);
                weight = weight * Math.log((numDocs - docFreq)/docFreq);
                fv.setTerm(term, weight);
            }         
        }
    }    
    
    //  freq            new_wt = new_tf / n
    class IDFFreqWeight extends IDFWeight {        
        public void weight(FeatureVector fv) throws Exception {
            Iterator<String> it = fv.iterator();
            while (it.hasNext()) {
                String term = it.next();
                double weight = fv.getFeatureWeight(term);
                weight = weight / numDocs;
                fv.setTerm(term, weight);
            }          
        }
    }
    
  //squared     new_wt = new_tf * log(num_docs/coll_freq_of_term)**2
    class IDFSquareWeight extends IDFWeight {
        
        public void weight(FeatureVector fv) throws Exception {
            Iterator<String> it = fv.iterator();
            while (it.hasNext()) {
                String term = it.next();
                double weight = fv.getFeatureWeight(term);
                double docFreq = index.docFreq(term);
                weight = weight * Math.pow(Math.log(numDocs/docFreq), 2);
                fv.setTerm(term, weight);
            }         
        }
    }
    
}
