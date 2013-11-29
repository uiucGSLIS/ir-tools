package edu.gslis.indexes;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * assumes the supplied text file is raw output from dumpindex or similar:
 * TOTAL <total_tok_count> <total_doc_count>
 * term1 <term1_tok_count> <term1_doc_count>
 * term2 <term2_tok_count> <term2_doc_count>
 * ...
 * termN <termN_tok_count> <termN_doc_count>
 * 
 * N.B. we do not get the vocabulary size from this!
 * 
 * @author Miles Efron
 *
 */
public class CorpusStatsFromTextFileImpl implements CorpusStats {

	public static final int TERM_INDEX = 0;
	public static final int TOK_COUNT_INDEX = 1;
	public static final int DOC_COUNT_INDEX  = 2;
	public static final Pattern SPACE_PATTERN = Pattern.compile(" ", Pattern.DOTALL);
	private double tokCount = 0.0;
	private double docCount = 0.0;
	private Map<String,int[]> termCounts;	// structure is term -> [tok_count, doc_count]

	public CorpusStatsFromTextFileImpl(String pathToDumpIndexFile) {
		List<String> lines = null;
		try {
			lines = IOUtils.readLines(new FileInputStream(pathToDumpIndexFile));
		} catch (Exception e) {
			e.printStackTrace();
		}

		termCounts = new HashMap<String,int[]>(lines.size()-1);
		Iterator<String> lineIterator = lines.iterator();
		try {
			// first line is aggregate stats
			String line = lineIterator.next();

			String[] toks = SPACE_PATTERN.split(line);
			tokCount = Double.parseDouble(toks[TOK_COUNT_INDEX]);
			docCount = Double.parseDouble(toks[DOC_COUNT_INDEX]);


			while(lineIterator.hasNext()) {
				line = lineIterator.next();	


				toks = SPACE_PATTERN.split(line);
				String term     = toks[TERM_INDEX];
				int t = Integer.parseInt(toks[TOK_COUNT_INDEX]);
				int d = Integer.parseInt(toks[DOC_COUNT_INDEX]);
				int[] entry = new int[2];
				entry[0] = t;
				entry[1] = d;
				termCounts.put(term, entry);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public double docCount() {
		return this.docCount;
	}

	public double termCount() {
		return this.tokCount;
	}

	public double docFreq(String term) {
		if(!termCounts.containsKey(term)) {
			return 0.0;
		}
		return termCounts.get(term)[0];
	}

	public double termFreq(String term) {
		if(!termCounts.containsKey(term)) {
			return 0.0;
		}
		return termCounts.get(term)[1];	
	}

}
