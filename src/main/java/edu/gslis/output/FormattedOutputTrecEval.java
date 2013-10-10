package edu.gslis.output;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;

import edu.gslis.searchhits.SearchHit;
import edu.gslis.searchhits.SearchHits;


public class FormattedOutputTrecEval {

	private static SearchHits results;
	private static Writer writer;
	private static String runId;
	private static FormattedOutputTrecEval formattedOutputTrecEval;

	private FormattedOutputTrecEval() {
		;
	}
	private void setResults(SearchHits results) {
		FormattedOutputTrecEval.results = results;
	}
	private void setWriter(Writer writer) {
		FormattedOutputTrecEval.writer = writer;
	}
	private void setRunId(String runId) {
		FormattedOutputTrecEval.runId = runId;
	}
	public static FormattedOutputTrecEval getInstance(String runId, Writer writer) {
		if(formattedOutputTrecEval==null) {
			formattedOutputTrecEval = new FormattedOutputTrecEval();
			formattedOutputTrecEval.setRunId(runId);
			formattedOutputTrecEval.setWriter(writer);
		}
		return formattedOutputTrecEval;		
	}

	public void write(SearchHits results, String queryName) {
		formattedOutputTrecEval.setResults(results);
		Iterator<SearchHit> hitIterator = results.iterator();
		int k=1;
		try {
			while(hitIterator.hasNext()) {
				SearchHit hit = hitIterator.next();
				String r = queryName + " Q0 " + hit.getDocno() + " " + k++ + " " + 
						hit.getScore() + " "  + " " + runId + System.getProperty("line.separator");
				
				//writer.printf("%s Q0 %s %i %f %s", queryName, hit.getDocno(), k++, 
				//		hit.getScore(), runId);
				
				IOUtils.write(r, writer);
			}
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
