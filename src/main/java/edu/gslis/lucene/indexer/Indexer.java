package edu.gslis.lucene.indexer;

import java.io.File;
import java.util.Set;

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

    public static final String FIELD_TYPE_STRING = "string";
    public static final String FIELD_TYPE_TEXT = "text";
    public static final String FIELD_TYPE_INT = "int";
    public static final String FIELD_TYPE_LONG = "long";
    public static final String FIELD_TYPE_DOUBLE = "double";


    public abstract void buildIndex(IndexWriter writer, Set<FieldConfig> fields, File file)
        throws Exception;
}