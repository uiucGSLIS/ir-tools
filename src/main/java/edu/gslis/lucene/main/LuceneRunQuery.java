package edu.gslis.lucene.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import edu.gslis.indexes.IndexWrapper;
import edu.gslis.indexes.IndexWrapperFactory;
import edu.gslis.lucene.expansion.Rocchio;
import edu.gslis.lucene.indexer.Indexer;
import edu.gslis.lucene.main.config.RunQueryConfig;
import edu.gslis.queries.GQueries;
import edu.gslis.queries.GQueriesFedwebImpl;
import edu.gslis.queries.GQueriesIndriImpl;
import edu.gslis.queries.GQueriesJsonImpl;
import edu.gslis.queries.GQuery;
import edu.gslis.queries.expansion.Feedback;
import edu.gslis.queries.expansion.FeedbackRelevanceModel;
import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.textrepresentation.FeatureVector;
import edu.gslis.utils.Stopper;

/**
 * Lucene query runner modeled after IndriRunQuery
 */
public class LuceneRunQuery {
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    RunQueryConfig config;
    
    public LuceneRunQuery(RunQueryConfig config) {
        this.config = config;
    }
    

    public static void main(String[] args) throws Exception {
        RunQueryConfig config = new RunQueryConfig();
        if (args.length == 0) 
        {
            System.err.println("you must specify a configuration file.");
            System.exit(-1);
        }
        else if (args.length == 1) {
            File yamlFile = new File(args[0]);
            if(!yamlFile.exists()) {
                System.err.println("Configuration file not found.");
                System.exit(-1);
            }
            Yaml yaml = new Yaml(new Constructor(RunQueryConfig.class));
            config = (RunQueryConfig)yaml.load(new FileInputStream(yamlFile));
        }        
        else {
        	// No yaml file specified, use command line options
            Options options = createOptions();
            CommandLineParser parser = new GnuParser();
            CommandLine cmd = parser.parse( options, args);
            
            String analyzer = cmd.getOptionValue("analyzer", Indexer.DEFAULT_ANALYZER);
            String docno = cmd.getOptionValue("docno", Indexer.FIELD_DOCNO);
            String field = cmd.getOptionValue("field", Indexer.FIELD_TEXT);
            String querynum = cmd.getOptionValue("querynum", "1");
            String runname = cmd.getOptionValue("name", "default");
            
            String similarity = cmd.getOptionValue("similarity", Indexer.DEFAULT_SIMILARITY);

            String query = cmd.getOptionValue("query");
            String queryfile = cmd.getOptionValue("queryfile");
            String format = cmd.getOptionValue("format");
            String index = cmd.getOptionValue("index");
            String stopwords = cmd.getOptionValue("stopwords");
            int fbDocs = Integer.parseInt(cmd.getOptionValue("fbDocs", "0"));
            int fbTerms = Integer.parseInt(cmd.getOptionValue("fbTerms", "0"));
            double fbOrigWeight  = Double.parseDouble(cmd.getOptionValue("fbOrigWeight", "0"));
            
            GQueries gqueries = new GQueriesJsonImpl();
            if (!StringUtils.isEmpty(query)) {
            	FeatureVector qv = new FeatureVector(query, null);            	
            	GQuery gquery = new GQuery();
            	gquery.setFeatureVector(qv);;
            	gquery.setText(query);
            	gquery.setTitle(querynum);
            	gqueries.addQuery(gquery);
            } else {
            	gqueries = readQueries(queryfile, format);
            }

            config.setAnalyzer(analyzer);
            config.setField(field);
            config.setIndex(index);
            config.setQueries(gqueries);
            config.setDocno(docno);
            config.setSimilarity(similarity);
            config.setStopwords(stopwords);
            config.setRunName(runname);
            config.setFbDocs(fbDocs);
            config.setFbTerms(fbTerms);
            config.setFbOrigWeight(fbOrigWeight);
        }            
        LuceneRunQuery runner = new LuceneRunQuery(config);
        runner.run();
    }
    
    /**
     * Read index metadata, if present
     * @param indexPath Path to index directory
     * @return Map of metadata properties and values
     */
    private Map<String, String> readIndexMetadata(String indexPath) 
    {
        Map<String, String> map = new HashMap<String, String>();
        try
        {
            File metadata = new File(indexPath + File.separator + "index.metadata");
            List<String> lines = FileUtils.readLines(metadata);
            for (String line: lines) {
                String[] fields = line.split("=");
                map.put(fields[0], fields[1]);            
            }
        } catch (IOException e) {
            // Can't find the index.metadata file, use overrides
        }
        return map;
    }
    
    /**
     * Read the query files given the specified format
     * @param file Path to topics
     * @param format One of fedweb, json, indri
     * @return Set of QueryConfig objects
     * @throws IOException 
     */
    private static GQueries readQueries(String file, String format) throws IOException 
    {
        GQueries gqueries = null;
        if (format.equals("fedweb")) {
           gqueries = new GQueriesFedwebImpl();
        }
        else if (format.equals("json")) {
            gqueries = new GQueriesJsonImpl();
        }
        else if (format.equals("indri")) {
            gqueries = new GQueriesIndriImpl();
        }
        
        gqueries.read(file);
        return gqueries;
    }
    
    /**
     * Run the specified query configuration
     * @throws Exception
     */
    private void run() throws Exception 
    {
        String indexPath = config.getIndex();
        String docnoField = config.getDocno();
        if (StringUtils.isEmpty(docnoField))
            docnoField = "docno";
        String similarityModel = config.getSimilarity();
        String stopwordsPath = config.getStopwords();
        
        Stopper stopper = null;
        if (!StringUtils.isEmpty(stopwordsPath))
        	stopper = new Stopper(stopwordsPath);
        
        Map<String, String> indexMetadata = readIndexMetadata(indexPath);
        if (StringUtils.isEmpty(similarityModel) && indexMetadata.get("similarity") != null) 
        	similarityModel = indexMetadata.get("similarity");
                                
        if (!StringUtils.isEmpty(config.getSimilarity()))
        	similarityModel = config.getSimilarity();
        
        // Setup the index searcher
        IndexWrapper index = IndexWrapperFactory.getIndexWrapper(indexPath);

        
        // Run each query
        for (int i=0; i<config.getQueries().numQueries(); i++) {
        	GQuery query = config.getQueries().getIthQuery(i);
        	if (stopper != null)
        		query.applyStopper(stopper);
        	
        	SearchHits hits = index.runQuery(query, 1000, similarityModel);
            
            if (config.getFbDocs() > 0 && config.getFbTerms() > 0) {
            	FeatureVector qv = new FeatureVector(query.getText(), null);
            	qv.normalize();
            	
        		Map<String, String> params = getParamsFromModel(config.getSimilarity());
        		double b= Double.parseDouble(params.get("b"));
        		double k1= Double.parseDouble(params.get("k1"));
            	
            	Rocchio rocchioFb = new Rocchio(config.getFbOrigWeight(), (1-config.getFbOrigWeight()), b, k1);
            	rocchioFb.expandQuery(index, query, config.getFbDocs(), config.getFbTerms());      	
            	
            	hits = index.runQuery(query, 1000, similarityModel);
            }
            hits.rank();
            
            int rank=0;
            for (SearchHit hit: hits.hits()) {
            	System.out.println(query.getTitle() + " Q0 " + hit.getDocno() + " " + rank + " "  + hit.getScore() + " " + config.getRunName());
            	rank++;
            }
            
        }
    }
    
    public Map<String, String> getParamsFromModel(String model) {
		Map<String, String> params = new HashMap<String, String>();
		String[] fields = model.split(",");
		// Parse the model spec
		for (String field : fields) {
			String[] nvpair = field.split(":");
			params.put(nvpair[0], nvpair[1]);
		}
		return params;
    }
    
    public String getLuceneQueryString(FeatureVector fv) {
    	StringBuilder queryString = new StringBuilder();
    	for (String term: fv.getFeatures()) {
            queryString.append(" ");
            queryString.append(term+"^"+fv.getFeatureWeight(term));
        }
        return queryString.toString();
    }

    
    public static Options createOptions()
    {
        Options options = new Options();
        options.addOption("analyzer", true, "Analyzer class");
        options.addOption("index", true, "Path to index");
        options.addOption("field", true, "Field to search");
        options.addOption("docno", true, "Docno field");
        options.addOption("querynum", true, "Query identifier");
        options.addOption("query", true, "Query string");
        options.addOption("queryfile", true, "Query file");
        options.addOption("format", true, "Query file format");
        options.addOption("similarity", true, "Similarity class");
        options.addOption("stopwords", true, "Stopwords list");
        options.addOption("name", true, "Run name");
        options.addOption("fbDocs", true, "Number of feedback documents");
        options.addOption("fbTerms", true, "Number of feedback terms");
        options.addOption("fbOrigWeight", true, "Weight of original query");

        return options;
    }

}
