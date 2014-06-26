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

}
