package edu.gslis.lucene.indexer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.gslis.lucene.main.config.FieldConfig;


public class JSONIndexer extends Indexer {
    
    @Override
    public long buildIndex(IndexWriter writer, Set<FieldConfig> fields,
            File file) throws Exception 
    {
        long count = 0;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f: files) {
                count += buildIndex(writer, fields, f);
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
                        is.close();
                    } catch (Exception e) {
                        System.err.println("Error processing entry " + entry.getName());
                        e.printStackTrace();
                    }
                }
            }
            tis.close();
        }
        else {
            InputStream is = null;
            try
            { 
                String parent = "";
                if (file.getParentFile() != null)
                    parent = file.getParentFile().getName();
                String name = file.getName();
                name = name.substring(0, name.lastIndexOf("."));
                is = new FileInputStream(file);
                buildIndex(writer, fields, parent, name, is);
                count++;
            } catch (Exception e) { 
                System.out.println("Error processing " + file.getAbsolutePath());
                e.printStackTrace();              
            }
            finally {
                if (is != null) 
                    is.close();
            }
        }
        return count;

    }
    
    public void buildIndex(IndexWriter writer, Set<FieldConfig> fields, String name,
        InputStream is) throws Exception  
    {
        buildIndex(writer, fields, "", name, is);
    }
        
    public void buildIndex(IndexWriter writer, Set<FieldConfig> fields, String parentDir, String name,
            InputStream is) throws Exception 
    {
        Analyzer analyzer = writer.getAnalyzer();
        Document luceneDoc = new Document();
                                              

        JsonParser parser = new JsonParser();
        JsonElement elem = parser.parse(new InputStreamReader(is));
        JsonObject json = elem.getAsJsonObject();
        String output = json.toString();
        
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
                    
                    if (parentDir != null)
                        name = parentDir + "_" + name;
                    
                    addField(luceneDoc, field, name, analyzer);
                }
                else if (source.equals(FieldConfig.SOURCE_FILE)) {
                    addField(luceneDoc, field, output, analyzer);
                }      
                else {
                	// Source is element
                	String element = field.getElement();
                	if (json.get(element) != null) {
                		String value = "";
                		if (json.get(element) instanceof JsonObject)
                			value = json.get(element).toString();
                		else
                			value = json.get(element).getAsString();	

//	                	System.out.println("Value : " + value);
	                    if (value != null)
	                        addField(luceneDoc, field, value, analyzer);
                	} else {
                		System.out.println("Element " + element + " is null");
                	}
                }

            }
        }
        writer.addDocument(luceneDoc);
      
    }
}
