package edu.gslis.lucene.indexer;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

class SnowballAnalyzer extends StopwordAnalyzerBase {

	private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

	public SnowballAnalyzer(Version version) {
		super(version);
	}

	public SnowballAnalyzer(Version version, CharArraySet stopwords) {
		super(version, stopwords);
	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		final StandardTokenizer src = new StandardTokenizer(matchVersion, reader);
		src.setMaxTokenLength(maxTokenLength);
		TokenStream tok = new StandardFilter(matchVersion, src);
		tok = new LowerCaseFilter(matchVersion, tok);
		tok = new StopFilter(matchVersion, tok, stopwords);
		tok = new SnowballFilter(tok, "English");
		return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) throws IOException {
				src.setMaxTokenLength(maxTokenLength);
				super.setReader(reader);
			}
		};
	}
}