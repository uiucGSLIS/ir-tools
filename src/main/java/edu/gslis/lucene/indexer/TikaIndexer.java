package edu.gslis.lucene.indexer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import edu.gslis.lucene.main.config.FieldConfig;


public class TikaIndexer extends Indexer {
    @Override
    public void buildIndex(IndexWriter writer, Set<FieldConfig> fields,
            File file) throws Exception 
    {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f: files) {
                buildIndex(writer, fields, f);
            }
        }
        else if (file.getName().endsWith("tgz")) {
            TarArchiveInputStream tis = 
                    new TarArchiveInputStream(
                            new GzipCompressorInputStream(
                                    new BufferedInputStream(
                                            new FileInputStream(file))));
            TarArchiveEntry entry;
            while (null != (entry = tis.getNextTarEntry())) 
            {
                if (entry.isFile()) {
                    try
                    {
                        int size = 0;
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        int c;
                        while (size < entry.getSize()) {
                            c = tis.read();
                            size++;
                            bos.write(c);
                        }
                        
                        String name = entry.getName();
                        name = name.substring(0, name.lastIndexOf("."));
                        name = name.replaceAll("/", "_");
                        InputStream is = new ByteArrayInputStream(bos.toByteArray());
                        buildIndex(writer, fields, name, is);
                    } catch (Exception e) {
                        System.err.println("Error processing entry " + entry.getName());
                        e.printStackTrace();
                    }
                }
            }
            tis.close();
        }
        else {
            try
            { 
                String name = file.getName();
//                if (! (name.endsWith(".png") || name.endsWith(".gif") || name.endsWith(".jpg")) ) {
                    name = name.substring(0, name.lastIndexOf("."));
                    name = name.replaceAll("/", "_");
                    InputStream is = new FileInputStream(file);
                    buildIndex(writer, fields, name, is);
                    is.close();
     //           }
            } catch (Exception e) { 
                System.out.println("Error processing " + file.getAbsolutePath());
                e.printStackTrace();              
            }
        }

    }
        
    public void buildIndex(IndexWriter writer, Set<FieldConfig> fields, String name,
            InputStream is) throws Exception 
    {
        Analyzer analyzer = writer.getAnalyzer();
        Document luceneDoc = new Document();
                                        
        // Use Tika to parse text from different file types
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        Parser parser = new AutoDetectParser();
        parser.parse(is, handler, metadata, new ParseContext());
        
        String output = handler.toString();
        
        if (StringUtils.isEmpty(output))
            return;
        
        for (FieldConfig field: fields) {
            String source = field.getSource();

            if (!StringUtils.isEmpty(source))
            {
                if (source.equals(FieldConfig.SOURCE_FILENAME)) 
                {
                    // Index the current file, assume the file name is the document identifier
                    if (name.contains("."))
                        name = name.substring(0, name.indexOf("."));
                    
                    addField(luceneDoc, field, name, analyzer);
                }
                else if (source.equals(FieldConfig.SOURCE_FILE)) {
                    addField(luceneDoc, field, output, analyzer);
                }      
                else if (source.equals("title"))
                {
                    String title = metadata.get(TikaCoreProperties.TITLE);
                    if (title != null)
                        addField(luceneDoc, field, title, analyzer);
                }
                else if (source.equals("description"))
                {
                    String description = metadata.get(TikaCoreProperties.DESCRIPTION);
                    if (description != null)
                        addField(luceneDoc, field, description, analyzer);
                }
                else if (source.equals("creator"))
                {
                    String creator = metadata.get(TikaCoreProperties.CREATOR);
                    if (creator != null)
                        addField(luceneDoc, field, creator, analyzer);
                }
                else if (source.equals("date"))
                {
                    String date= metadata.get(TikaCoreProperties.METADATA_DATE);
                    if (date != null)
                        addField(luceneDoc, field, date, analyzer);
                }
                else if (source.equals("keywords"))
                {
                    String keyword = metadata.get(TikaCoreProperties.KEYWORDS);
                    if (keyword != null)
                        addField(luceneDoc, field, keyword, analyzer);                    
                }
            }
        }
        writer.addDocument(luceneDoc);
      
    }
}
