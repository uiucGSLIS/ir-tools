package edu.gslis.lucene.indexer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;

class PorterAnalyzer extends Analyzer {
	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		Tokenizer source = new LowerCaseTokenizer(Indexer.VERSION, reader);
		return new TokenStreamComponents(source, new PorterStemFilter(source));
	}
}