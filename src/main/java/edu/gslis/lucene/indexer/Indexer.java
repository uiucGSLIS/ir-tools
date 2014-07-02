package edu.gslis.lucene.indexer;

import java.io.File;
import java.io.StringReader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Version;

import edu.gslis.lucene.main.config.FieldConfig;

/**
 * Abstract class for Lucene-based indexers
 */
public abstract class Indexer
{    
    public static Version VERSION = Version.LUCENE_47;
    
    public static final String FIELD_DOCNO = "docno";
    public static final String FIELD_DOC_LEN = "doclen";
    public static final String FIELD_TEXT = "text"; 
    public static final String FIELD_EPOCH = "epoch"; 
    
    public static final String FORMAT_TRECTEXT = "trectext";
    public static final String FORMAT_WIKI = "wiki";
    public static final String FORMAT_TIKA = "tika";

    public static final String FIELD_TYPE_STRING = "string";
    public static final String FIELD_TYPE_TEXT = "text";
    public static final String FIELD_TYPE_INT = "int";
    public static final String FIELD_TYPE_LONG = "long";
    public static final String FIELD_TYPE_DOUBLE = "double";
    
    public static final String DEFAULT_SIMILARITY = "org.apache.lucene.search.similarities.DefaultSimilarity";
    public static final String DEFAULT_ANALYZER = "org.apache.lucene.analysis.standard.StandardAnalyzer";


    public abstract void buildIndex(IndexWriter writer, Set<FieldConfig> fields, File file)
        throws Exception;
    
    
    protected void addField(Document luceneDoc, FieldConfig fieldConfig, String value, 
            Analyzer defaultAnalyzer) throws Exception
    {        
        String fieldName = fieldConfig.getName();
        String type = fieldConfig.getType();
        
        Field luceneField;
        Field.Store stored = fieldConfig.isStored() ? Field.Store.YES : Field.Store.NO;

        if (type.equals(FieldConfig.TYPE_ID)) {     
            luceneField = new StringField(fieldName, value, stored);
        }
        else if (type.equals(FieldConfig.TYPE_INT)) {                    
            luceneField = new IntField(fieldName, Integer.valueOf(value), stored);
        }
        else if (type.equals(FieldConfig.TYPE_LONG)) {                    
            luceneField = new LongField(fieldName, Long.valueOf(value), stored);
        }
        else if (type.equals(FieldConfig.TYPE_DOUBLE)) {                    
            luceneField = new DoubleField(fieldName, Double.valueOf(value), stored);
        }
        else if (type.equals(FieldConfig.TYPE_STRING)) { 
            luceneField = new StringField(fieldName, value, stored);                            
        }
        else if (type.equals(FieldConfig.TYPE_TEXT)) {  
            FieldType fieldType = new FieldType();
            fieldType.setIndexed(fieldConfig.isIndexed());
            fieldType.setStored(fieldConfig.isStored());
            fieldType.setStoreTermVectors(fieldConfig.isStoredTermVectors());
            fieldType.setStoreTermVectorPositions(fieldConfig.isStoredTermVectorPositions());
            fieldType.setStoreTermVectorOffsets(fieldConfig.isStoredTermVectorOffsets());
            fieldType.setStoreTermVectorPayloads(fieldConfig.isStoredTermVectorPayloads());
            luceneField = new Field(fieldName, value, fieldType);
            
            // Store the document length
            TokenStream stream = defaultAnalyzer.tokenStream(fieldName, new StringReader(value));
            stream.reset();                            
            long docLength = 0;                            
            while (stream.incrementToken())
                docLength++;
            stream.end();
            stream.close();
            luceneDoc.add(new LongField(FIELD_DOC_LEN, docLength, Store.YES));
        }
        else {
            throw new Exception("Unsupported field type: " + type);
        }        
        luceneDoc.add(luceneField);
    }
}