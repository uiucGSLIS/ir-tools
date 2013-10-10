package edu.gslis.demo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;

import edu.gslis.indexes.IndexWrapper;
import edu.gslis.indexes.IndexWrapperIndriImpl;
import edu.gslis.output.FormattedOutputTrecEval;
import edu.gslis.queries.GQueries;
import edu.gslis.queries.GQueriesJsonImpl;
import edu.gslis.queries.GQuery;
import edu.gslis.searchhits.SearchHits;
import edu.gslis.utils.ParameterBroker;
import edu.gslis.utils.Stopper;




public class GslisRunQueries {

	
	/**
	 * imitates IndriRunQuery
	 * 
	 * @param args[0] /path/to/json/param/file
	 */
	public static void main(String[] args) {
		File paramFile = new File(args[0]);
		if(!paramFile.exists()) {
			System.err.println("you must specify a parameter file to run against.");
			System.exit(-1);
		}
		
		ParameterBroker params = new ParameterBroker(args[0]);

		
		GQueries        queries   = new GQueriesJsonImpl();
					    queries.read(params.getParamValue(params.QUERY_PATH_PARAM));
		IndexWrapper    index     = new IndexWrapperIndriImpl(params.getParamValue(params.INDEX_PATH_PARAM));
		String          runId     = "gslis";

		String      countString   = params.getParamValue(params.COUNT_PARAM);
		if(countString==null)
			countString="1000";
		int count = Integer.parseInt(countString);
		Stopper stopper = new Stopper(params.getParamValue(params.STOPPER_PARAM));
		
		Writer outputWriter = new BufferedWriter(new OutputStreamWriter(System.out));
		FormattedOutputTrecEval output = FormattedOutputTrecEval.getInstance(runId, outputWriter);
		Iterator<GQuery> queryIterator = queries.iterator();
		while(queryIterator.hasNext()) {
			GQuery query = queryIterator.next();
			
			System.err.println(query.getTitle());
			
			SearchHits results = index.runQuery(query, count);
			output.write(results, query.getTitle());
		}
		output.close();
	}

}
