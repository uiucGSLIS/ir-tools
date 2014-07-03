package edu.gslis.lucene.main.config;

public class CorpusConfig {
    String path = "";
    // html, trecweb, trectext, trecalt, doc, ppt, pdf, txt
    String type = "";
    String filter = "";
    
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getFilter() {
        return filter;
    }
    public void setFilter(String filter) {
        this.filter = filter;
    }
    
}
