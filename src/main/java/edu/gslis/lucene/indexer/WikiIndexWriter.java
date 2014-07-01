package edu.gslis.lucene.indexer;

import java.io.IOException;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.index.IndexWriter;
import org.mediawiki.importer.DumpWriter;
import org.mediawiki.importer.Page;
import org.mediawiki.importer.Revision;
import org.mediawiki.importer.Siteinfo;
import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;

import edu.gslis.lucene.main.TextConverter;
import edu.gslis.lucene.main.config.FieldConfig;



/**
 * Implements the Mediawiki DumpWriter interface for 
 * SAX-based parsing of a wiki dumpfile.  Writes to a given 
 * Lucene index
 */
public class WikiIndexWriter  implements DumpWriter {

    Document currentDoc = null;
    IndexWriter index = null;
    Map<String, FieldConfig> fields = new HashMap<String, FieldConfig>();
    
    org.sweble.wikitext.engine.Compiler compiler = null;
    Analyzer analyzer = null;
    SimpleWikiConfiguration wikicfg = null;
    
    public WikiIndexWriter(IndexWriter index, Set<FieldConfig> fieldSet) throws IOException {
        this.index = index;
        for (FieldConfig field: fieldSet) {
            fields.put(field.getName(), field);
        }
        
        wikicfg = new SimpleWikiConfiguration(
                "classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml");
        
        compiler = new org.sweble.wikitext.engine.Compiler(wikicfg);
        analyzer = index.getAnalyzer();

    }
    
    public void close() throws IOException {        
    }

    public void writeStartWiki() throws IOException{        
    }
    public void writeEndWiki() throws IOException{
        
    }

    public void writeSiteinfo(Siteinfo info) throws IOException {
        
    }

    public void writeStartPage(Page page) throws IOException {
        currentDoc = new Document();
        //String pageId = String.valueOf(page.Id);
        String pageTitle = page.Title.Text;
        //boolean isRedirect = page.isRedirect;        
        //addField("id", pageId);
        System.out.println("Indexing " + pageTitle);
        addField("title", pageTitle);
    }
    
    public void writeEndPage() throws IOException {
        index.addDocument(currentDoc);
    }

    public void writeRevision(Revision revision) throws IOException {
        String text = revision.Text;
        addField("text", text);        
    }
    
    private void addField(String name, String value) throws IOException {
        try
        {
            
            FieldConfig field = fields.get(name);
            String type = field.getType();
            String fieldName = field.getName();
            
            Field.Store stored = field.isStored() ? Field.Store.YES : Field.Store.NO;
            
            Field luceneField; 
            if (type.equals(Indexer.FIELD_TYPE_STRING)) {
                FieldType fieldType = new FieldType();
                fieldType.setIndexed(field.isIndexed());
                fieldType.setStored(field.isStored());
                fieldType.setStoreTermVectors(field.isStoredTermVectors());
                fieldType.setStoreTermVectorPositions(field.isStoredTermVectorPositions());
                fieldType.setStoreTermVectorOffsets(field.isStoredTermVectorOffsets());
                fieldType.setStoreTermVectorPayloads(field.isStoredTermVectorPayloads());
                luceneField = new Field(fieldName, value, fieldType);
                                
            } else if (type.equals(Indexer.FIELD_TYPE_TEXT)) {
                                    
                String wikitext = StringEscapeUtils.unescapeHtml(value);
                String title = currentDoc.getField("title").stringValue();
                String output = "";
                try
                {
                    PageTitle pageTitle = PageTitle.make(wikicfg, title);
                    PageId pageId = new PageId(pageTitle, -1);
                    CompiledPage cp = compiler.postprocess(pageId, wikitext, null);
                    
                    TextConverter p = new TextConverter(wikicfg, 80);
                    output = (String) p.go(cp.getPage());
                    output = title + "\n" + output;
                } catch (Exception e) {
                    System.out.println("Failed to parse page" + title);
                    e.printStackTrace();
                }
            
                
                FieldType fieldType = new FieldType();
                fieldType.setIndexed(field.isIndexed());
                fieldType.setStored(field.isStored());
                fieldType.setStoreTermVectors(field.isStoredTermVectors());
                fieldType.setStoreTermVectorPositions(field.isStoredTermVectorPositions());
                fieldType.setStoreTermVectorOffsets(field.isStoredTermVectorOffsets());
                fieldType.setStoreTermVectorPayloads(field.isStoredTermVectorPayloads());
                luceneField = new Field(fieldName, output, fieldType);
                
                
                // Store the document length
                TokenStream stream = analyzer.tokenStream(fieldName, new StringReader(output));
                stream.reset();                            
                long docLength = 0;                            
                while (stream.incrementToken())
                    docLength++;
                stream.end();
                stream.close();
                currentDoc.add(new LongField(Indexer.FIELD_DOC_LEN, docLength, Store.YES));

            } else if (type.equals(Indexer.FIELD_TYPE_INT)) {
                luceneField = new IntField(fieldName, Integer.valueOf(value), stored);
            } else if (type.equals(Indexer.FIELD_TYPE_LONG)) {
                luceneField = new LongField(fieldName, Long.valueOf(value), stored);
            } else if (type.equals(Indexer.FIELD_TYPE_DOUBLE)) {
                luceneField = new DoubleField(fieldName, Double.valueOf(value), stored);
            }
            else {
                throw new IOException("Unsupported field type: " + type);
            }
            currentDoc.add(luceneField);
        } catch (Exception e) { 
            throw new IOException(e);
        }
    }
}
