package edu.gslis.lucene.main.config;

/**
 * YAML configuration binding for a Lucene field. 
 * @see http://lucene.apache.org/core/4_0_0/core/org/apache/lucene/document/Field.html
 *
 */
public class FieldConfig {
    String name = "";
    boolean indexed = false;
    boolean stored = false;
    boolean storedTermVectors = false;
    boolean storedTermVectorPositions = false;
    boolean storedTermVectorOffsets= false;
    boolean storedTermVectorPayloads = false;
    String analyzer = "";
    String type = "";
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isIndexed() {
        return indexed;
    }
    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }
    public boolean isStored() {
        return stored;
    }
    public void setStored(boolean stored) {
        this.stored = stored;
    }
    public boolean isStoredTermVectors() {
        return storedTermVectors;
    }
    public void setStoredTermVectors(boolean storedTermVectors) {
        this.storedTermVectors = storedTermVectors;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public boolean isStoredTermVectorPositions() {
        return storedTermVectorPositions;
    }
    public void setStoredTermVectorPositions(boolean storedTermVectorPositions) {
        this.storedTermVectorPositions = storedTermVectorPositions;
    }
    public boolean isStoredTermVectorOffsets() {
        return storedTermVectorOffsets;
    }
    public void setStoredTermVectorOffsets(boolean storedTermVectorOffsets) {
        this.storedTermVectorOffsets = storedTermVectorOffsets;
    }
    public boolean isStoredTermVectorPayloads() {
        return storedTermVectorPayloads;
    }
    public void setStoredTermVectorPayloads(boolean storedTermVectorPayloads) {
        this.storedTermVectorPayloads = storedTermVectorPayloads;
    }
    public String getAnalyzer() {
        return analyzer;
    }
    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }
    
}
