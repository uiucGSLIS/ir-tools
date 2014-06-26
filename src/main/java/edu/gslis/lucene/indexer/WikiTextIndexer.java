package edu.gslis.lucene.indexer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.gslis.lucene.main.TextConverter;
import edu.gslis.lucene.main.config.FieldConfig;


public class WikiTextIndexer extends Indexer {
    
    static final String PAGE_TAG = "page";
    public void buildIndex(IndexWriter writer, Set<FieldConfig> fields, File corpus) 
            throws Exception 
    {

        SimpleWikiConfiguration wikicfg = new SimpleWikiConfiguration(
                "classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml");
        org.sweble.wikitext.engine.Compiler compiler = new org.sweble.wikitext.engine.Compiler(wikicfg);


        if (corpus.isDirectory()) {
            File[] files = corpus.listFiles();
            for (File file: files) {
                buildIndex(writer, fields, file);
            }
        } else {
            Analyzer analyzer = writer.getAnalyzer();

            DocumentBuilderFactory factory =  DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document xml;
            InputStream ois = new BufferedInputStream(new FileInputStream(corpus));
            try {
                // Use commons-compress to autodetect compressed formats
                CompressorInputStream cis = new CompressorStreamFactory().createCompressorInputStream(ois);
                xml = builder.parse(cis);
            } catch (Exception e) {
                // Assume that this isn't compressed
                xml = builder.parse(ois);
            }
            
            NodeList docs = xml.getElementsByTagName(PAGE_TAG);
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
                            
                            String wikitext = StringEscapeUtils.unescapeHtml(value);
                            String title = luceneDoc.getField("title").stringValue();
                            PageTitle pageTitle = PageTitle.make(wikicfg, title);
                            PageId pageId = new PageId(pageTitle, -1);
                            CompiledPage cp = compiler.postprocess(pageId, wikitext, null);
                            
                            TextConverter p = new TextConverter(wikicfg, 80);
                            String output = (String) p.go(cp.getPage());
                            //System.out.println(output);

                            FieldType fieldType = new FieldType();
                            fieldType.setIndexed(field.isIndexed());
                            fieldType.setStored(field.isStored());
                            fieldType.setStoreTermVectors(field.isStoredTermVectors());
                            luceneField = new Field(fieldName, output, fieldType);
                            
                            
                            // Store the document length
                            TokenStream stream = analyzer.tokenStream(fieldName, new StringReader(output));
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
                        } else if (type.equals(FIELD_TYPE_DOUBLE)) {
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
