package edu.gslis.lucene.indexer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringReader;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.gslis.lucene.main.config.FieldConfig;


/**
 * Constructs a Lucene index from a TREC-text formatted collection
 */
public class TrecTextIndexer extends Indexer
{
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    static final String DOC_TAG = "DOC";
    
    public void buildIndex(IndexWriter writer, Set<FieldConfig> fields, File corpus) 
            throws Exception 
    {
        if (corpus.isDirectory()) {
            File[] files = corpus.listFiles();
            for (File file: files) {
                buildIndex(writer, fields, file);
            }
        } else {
            Analyzer analyzer = writer.getAnalyzer();

            String data = FileUtils.readFileToString(corpus);
            // Add a root element
            data = "<root>" + data + "</root>";
            DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.parse(new ByteArrayInputStream(data.getBytes()));
            NodeList docs = xml.getElementsByTagName(DOC_TAG);
            for (int i=0; i<docs.getLength(); i++) {
                Element doc = (Element) docs.item(i);
                org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
                for (FieldConfig field: fields) {
                    String type = field.getType();
                    String fieldName = field.getName();
                    Field.Store stored = field.isStored() ? Field.Store.YES : Field.Store.NO;
                    
                    NodeList elements = doc.getElementsByTagName(fieldName);
                    
                    fieldName = fieldName.toLowerCase();
                    for (int j=0; j < elements.getLength(); j++) {
                        Element elem = (Element) elements.item(j);
                        
                        Node child = elem.getFirstChild();
                        String value = "";
                        if (child != null)
                            value = child.getNodeValue();
                        Field luceneField; 
                        if (type.equals(FIELD_TYPE_STRING)) {
                            luceneField = new StringField(fieldName, value, stored);                            
                        } else if (type.equals(FIELD_TYPE_TEXT)) {
                            FieldType fieldType = new FieldType();
                            fieldType.setIndexed(field.isIndexed());
                            fieldType.setStored(field.isStored());
                            fieldType.setStoreTermVectors(field.isStoredTermVectors());
                            //fieldType.setStoreTermVectorPositions(field.isStoredTermVectors());
                            luceneField = new Field(fieldName, value, fieldType);
                            
                            // Store the document length
                            TokenStream stream = analyzer.tokenStream(fieldName, new StringReader(value));
                            stream.reset();                            
                            long docLength = 0;                            
                            while (stream.incrementToken())
                                docLength++;
                            stream.end();
                            stream.close();
                            luceneDoc.add(new LongField(FIELD_DOC_LEN, docLength, Store.YES));
                            
                        } else if (type.equals(FIELD_TYPE_INT)) {
                            luceneField = new IntField(fieldName, Integer.valueOf(value), stored);
                        } else if (type.equals(FIELD_TYPE_LONG)) {
                            luceneField = new LongField(fieldName, Long.valueOf(value), stored);
                        } else if (type.equals("double")) {
                            luceneField = new DoubleField(fieldName, Double.valueOf(value), stored);
                        }
                        else {
                            throw new Exception("Unsupported field type: " + type);
                        }
                        luceneDoc.add(luceneField); 
                        
                    }
                }
                writer.addDocument(luceneDoc);
            }
        }
    }   
}
