package edu.gslis.lucene.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import edu.gslis.lucene.indexer.Indexer;
import edu.gslis.lucene.main.config.QueryConfig;
import edu.gslis.lucene.main.config.RunQueryConfig;


public class LuceneRunQuery {
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    RunQueryConfig config;
    
    public LuceneRunQuery(RunQueryConfig config) {
        this.config = config;
    }
    
    public void run() throws Exception {
        String indexPath = config.getIndex();
        StopwordAnalyzerBase analyzer;
        String stopwordsPath = config.getStopwords();
        String analyzerClass = config.getAnalyzer();
        String docnoField = config.getDocno();
        if (StringUtils.isEmpty(docnoField))
            docnoField = "docno";
        
        if (!StringUtils.isEmpty(stopwordsPath))
        {
            @SuppressWarnings("rawtypes")
            Class analyzerCls = loader.loadClass(analyzerClass);
            @SuppressWarnings({ "rawtypes", "unchecked" })
            java.lang.reflect.Constructor analyzerConst = analyzerCls.getConstructor(Version.class, Reader.class);
            analyzer = (StopwordAnalyzerBase)analyzerConst.newInstance(Indexer.VERSION, new FileReader(stopwordsPath));            
        } else {
            analyzer = (StopwordAnalyzerBase)loader.loadClass(analyzerClass).newInstance();            
        }
        
        Similarity similarity = new DefaultSimilarity();
        String similarityClass = config.getSimilarity();
        if (!StringUtils.isEmpty(similarityClass))
            similarity = (Similarity)loader.loadClass(similarityClass).newInstance();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(similarity);
        String field = config.getField();
        if (StringUtils.isEmpty(field))
            field = "text";
        
        Set<QueryConfig> queries = config.getQueries();        
        for (QueryConfig query: queries) {
            QueryParser parser = new QueryParser(Indexer.VERSION, field, analyzer);
            Query q = parser.parse(query.getText());
            TopDocs topDocs = searcher.search(q,  null, 1000);
            ScoreDoc[] docs = topDocs.scoreDocs;
            for (int i=0; i<docs.length; i++) {
                int docid = docs[i].doc;
                double score = docs[i].score;
                Document doc = searcher.doc(docid);
                String docno = doc.getField(docnoField).stringValue();
                long doclen = doc.getField(Indexer.FIELD_DOC_LEN).numericValue().longValue();

                System.out.println(query.getNumber() + " " + docno + " " + score + " " + doclen);
            }

        }
    }
    
    public static void main(String[] args) throws Exception {
        File yamlFile = new File(args[0]);
        if(!yamlFile.exists()) {
            System.err.println("you must specify a configuration file.");
            System.exit(-1);
        }
        
        Yaml yaml = new Yaml(new Constructor(RunQueryConfig.class));
        RunQueryConfig config = (RunQueryConfig)yaml.load(new FileInputStream(yamlFile));

        LuceneRunQuery runner = new LuceneRunQuery(config);
        runner.run();
    }
}
