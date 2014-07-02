package edu.gslis.lucene.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import edu.gslis.lucene.main.config.FieldConfig;


public class TikaIndexer extends Indexer {

    @Override
    public void buildIndex(IndexWriter writer, Set<FieldConfig> fields,
            File file) throws Exception {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f: files) {
                buildIndex(writer, fields, f);
            }
        } else {
            Analyzer analyzer = writer.getAnalyzer();
            Document luceneDoc = new Document();
                                    
            InputStream is = new FileInputStream(file);
            
            // Use Tika to parse text from different file types
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            Parser parser = new AutoDetectParser();
            parser.parse(is, handler, metadata, new ParseContext());
            
            String output = handler.toString();
            
            for (FieldConfig field: fields) {
                String source = field.getSource();

                if (!StringUtils.isEmpty(source))
                {
                    if (source.equals(FieldConfig.SOURCE_FILENAME)) 
                    {
                        // Index the current file, assume the file name is the document identifier
                        String name = file.getName();
                        if (name.contains("."))
                            name = name.substring(0, name.indexOf("."));
                        
                        addField(luceneDoc, field, name, analyzer);
                    }
                    else if (source.equals(FieldConfig.SOURCE_FILE)) {
                        addField(luceneDoc, field, output, analyzer);
                    }                    
                }
            }
            writer.addDocument(luceneDoc);
                        
        }
    }
}
