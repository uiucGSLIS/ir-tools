package edu.gslis.docscoring;

import java.util.Iterator;

import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHit;


/**
 * Preliminary KL-divergence scorer implementation with Dirichlet smoothing. 
 * If the type parameter is set to 1 (TYPE_LEMUR), will return Lemur-style
 * negative KL-scores.  If type is set to 2 (TYPE_STANDARD), query entropy
 * is not subtracted.
 */
public class ScorerDirichletKL extends QueryDocScorer 
{
	public static final String MU = "mu";
	public static final String TYPE = "type";
	public static final double TYPE_LEMUR = 1;
	public static final double TYPE_STANDARD = 2;
	
	public ScorerDirichletKL() {
		setParameter(MU, 2500);
		setParameter(TYPE, TYPE_LEMUR);
	}
	public void setQuery(GQuery query) {
		this.gQuery = query;
	}

    
    public double scoreZhai(SearchHit doc) 
    {
        double logLikelihood = 0.0;
        Iterator<String> queryIterator = gQuery.getFeatureVector().iterator();
        
        double mu = paramTable.get(MU);

        double dl = doc.getLength();
                        
        double unseen = Math.log(mu / (mu + dl));
        
        while(queryIterator.hasNext()) {            
            String feature = queryIterator.next();
            double cwd = doc.getFeatureVector().getFeatureWeight(feature);
            
            // p(w|Q)
            double pwq =  gQuery.getFeatureVector().getFeatureWeight(feature)/
                               gQuery.getFeatureVector().getFeatureCount();
            if (pwq == 0 || cwd == 0) 
                continue;
            // p(w|C)
            double pwc =  collectionStats.termCount(feature) / collectionStats.getTokCount();

            double seen = Math.log(1 + (cwd/(mu*pwc)));
           
            logLikelihood += pwq*seen;
        }
        
        logLikelihood += unseen;
        return logLikelihood;
    }   
	
    public double scoreStandard(SearchHit doc) 
    {
        double logLikelihood = 0.0;
        Iterator<String> queryIterator = gQuery.getFeatureVector().iterator();
        
        double mu = paramTable.get(MU);

        double dl = doc.getLength();
                        
        while(queryIterator.hasNext()) {            
            String feature = queryIterator.next();
            double cwd = doc.getFeatureVector().getFeatureWeight(feature);
            
            // p(w|C)
            double pwc =  collectionStats.termCount(feature) / collectionStats.getTokCount();
            // p(w|Q)
            double pwq =  gQuery.getFeatureVector().getFeatureWeight(feature)/
                               gQuery.getFeatureVector().getFeatureCount();            
            // p(w|D)
            double pwd = (cwd + mu * pwc)/(dl + mu);
           
            logLikelihood += pwq*Math.log(pwq) - pwq*Math.log(pwd);
        }
        
        double scoreZhai = scoreZhai(doc);
        double scoreLemur = scoreLemur(doc);
        System.out.println(scoreZhai + "," + scoreLemur + "," + -1*logLikelihood);
        return -1*logLikelihood;
    }   
    
    
    public double score(SearchHit doc) {
        double type = paramTable.get(TYPE);
        if (type == TYPE_LEMUR) {
            return scoreLemur(doc);
        } else {
            return scoreStandard(doc);
        }
    }
    
    

    
    public double scoreLemur(SearchHit doc) {
        double logLikelihood = 0.0;
        Iterator<String> queryIterator = gQuery.getFeatureVector().iterator();
        
        double mu = paramTable.get(MU);
        
        double dl = doc.getLength();
        double unseen = Math.log(mu/(mu+dl));
        while(queryIterator.hasNext()) {            
            String feature = queryIterator.next();
            double cwd = doc.getFeatureVector().getFeatureWeight(feature);           
            double pwq =  gQuery.getFeatureVector().getFeatureWeight(feature)/gQuery.getFeatureVector().getFeatureCount();
            
            if (cwd == 0 || pwq == 0) 
                   continue;

            double pwc =  collectionStats.termCount(feature) / collectionStats.getTokCount();
            double seen = Math.log(1 + (cwd/(mu*pwc)));

            logLikelihood += pwq * seen;
        }
        
        logLikelihood += unseen;
        
        queryIterator = gQuery.getFeatureVector().iterator();
        double collDiv = 0;
        while(queryIterator.hasNext()) {            
            String feature = queryIterator.next();
            double pwq =  gQuery.getFeatureVector().getFeatureWeight(feature)/gQuery.getFeatureVector().getFeatureCount();
            double pwc =  collectionStats.termCount(feature) / collectionStats.getTokCount();
            collDiv += pwq * Math.log(pwq/pwc);            
        }
        logLikelihood -= collDiv;
        return logLikelihood;
    }  
    
    /*
    public double scoreAll(SearchHit doc) {
        double logLikelihood = 0.0;
        Iterator<String> queryIterator = gQuery.getFeatureVector().iterator();
        
        double mu = paramTable.get(MU);
        
        
        while(queryIterator.hasNext()) {            
            String feature = queryIterator.next();
            double cwd = doc.getFeatureVector().getFeatureWeight(feature);           

            double pwq =  gQuery.getFeatureVector().getFeatureWeight(feature)/gQuery.getFeatureVector().getFeatureCount();
            double pwc =  collectionStats.termCount(feature) / collectionStats.getTokCount();
            double seen = Math.log(1 + (cwd/(mu*pwc)));
            
            logLikelihood += pwq * seen;
        }
        return logLikelihood;
    } 
    */
}
