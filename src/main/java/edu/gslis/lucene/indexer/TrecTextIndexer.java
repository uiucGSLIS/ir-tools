package edu.gslis.lucene.indexer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.drew.lang.StringUtil;

import edu.gslis.lucene.main.config.FieldConfig;


/**
 * Constructs a Lucene index from a TREC-text formatted collection
 */
public class TrecTextIndexer extends Indexer
{
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    static final String DOC_TAG = "DOC";
    
    public void buildIndex(IndexWriter writer, Set<FieldConfig> fields, String name,
            InputStream is) throws Exception 
    { 
        Analyzer analyzer = writer.getAnalyzer();

        String data = IOUtils.toString(is, "UTF-8");
        data = data.replace("&amp;", "&");
        data = data.replace("&", "&amp;");
        
        // Add a root element
        String xml = "<root>" + data + "</root>";
        DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document xmlDoc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
        NodeList docs = xmlDoc.getElementsByTagName(DOC_TAG);
        for (int i=0; i<docs.getLength(); i++) {
            Element doc = (Element) docs.item(i);
            org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
            for (FieldConfig field: fields) {
                
                String fieldName = field.getName();
                String elementName = field.getElement();
                NodeList elements = doc.getElementsByTagName(elementName);
                
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
