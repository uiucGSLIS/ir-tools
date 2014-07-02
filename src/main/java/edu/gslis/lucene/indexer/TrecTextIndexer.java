package edu.gslis.lucene.indexer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
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
    
    public void buildIndex(IndexWriter writer, Set<FieldConfig> fields, File file) 
            throws Exception 
    {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f: files) {
                buildIndex(writer, fields, f);
            }
        } else {
            Analyzer analyzer = writer.getAnalyzer();

            String data = FileUtils.readFileToString(file);
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
                    
                    String fieldName = field.getName();
                    
                    NodeList elements = doc.getElementsByTagName(fieldName);
                    
                    fieldName = fieldName.toLowerCase();
                    for (int j=0; j < elements.getLength(); j++) {
                        Element elem = (Element) elements.item(j);
                        
                        Node child = elem.getFirstChild();
                        String value = "";
                        if (child != null)
                            value = child.getNodeValue();
                        
                        addField(luceneDoc, field, value, analyzer);

                    }
                }
                writer.addDocument(luceneDoc);
            }
        }
    }   
}
