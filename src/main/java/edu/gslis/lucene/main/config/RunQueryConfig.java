package edu.gslis.lucene.main.config;

import edu.gslis.queries.GQueries;


public class RunQueryConfig {
    String index = "";
    GQueries queries;
    String stopwords = "";
    String analyzer = "";
    String similarity = "";
    String field = "";
    String docno = "";
    QueryFile queryFile;
    String runname = "";
    int fbDocs = 0;
    int fbTerms = 0;
    double fbAlpha = 0;
    double fbBeta = 0;
    int numResults = 0;

    
    
    public int getNumResults() {
		return numResults;
	}
	public void setNumResults(int numResults) {
		this.numResults = numResults;
	}
	public int getFbDocs() {
		return fbDocs;
	}
	public void setFbDocs(int fbDocs) {
		this.fbDocs = fbDocs;
	}
	public int getFbTerms() {
		return fbTerms;
	}
	public void setFbTerms(int fbTerms) {
		this.fbTerms = fbTerms;
	}
	public double getFbAlpha() {
		return fbAlpha;
	}
	public void setFbAlpha(double fbAlpha) {
		this.fbAlpha = fbAlpha;
	}
	public double getFbBeta() {
		return fbBeta;
	}
	public void setFbBeta(double fbBeta) {
		this.fbBeta = fbBeta;
	}	
	public String getIndex() {
        return index;
    }
    public void setIndex(String index) {
        this.index = index;
    }
    public GQueries getQueries() {
        return queries;
    }
    public void setQueries(GQueries gqueries) {
        this.queries = gqueries;
    }
    public String getStopwords() {
        return stopwords;
    }
    public void setStopwords(String stopwords) {
        this.stopwords = stopwords;
    }
    public String getAnalyzer() {
        return analyzer;
    }
    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }
    public String getSimilarity() {
        return similarity;
    }
    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }
    public String getField() {
        return field;
    }
    public void setField(String field) {
        this.field = field;
    }
    public String getDocno() {
        return docno;
    }
    public void setDocno(String docno) {
        this.docno = docno;
    }
    public QueryFile getQueryFile() {
        return queryFile;
    }
    public void setQueryFile(QueryFile queryFile) {
        this.queryFile = queryFile;
    }
	public String getRunName() {
		return runname;
	}
	public void setRunName(String runname) {
		this.runname = runname;
	}
    
}