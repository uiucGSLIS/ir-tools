package edu.gslis.lucene.main;

import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import edu.gslis.indexes.IndexWrapperLuceneImpl;
import edu.gslis.lucene.indexer.Indexer;
import edu.gslis.textrepresentation.FeatureVector;




public class LuceneDumpIndex 
{
 
    public static void main(String[] args) throws Exception {

        Options options = createOptions();
        CommandLineParser parser = new GnuParser();
        CommandLine cl = parser.parse( options, args);
        
        if (args.length == 0 || cl.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "dumpindex", options );
            return;
        }
        
        // Path to the index
        String path = cl.getOptionValue("index"); 
        // Field that we're searching
        String field = cl.getOptionValue("field", null);
        // Field containing the document identifier
        String docno = cl.getOptionValue("docno", "docno")
                ;
        // Command
        String cmd = cl.getOptionValue("cmd");
        String arg = cl.getOptionValue("arg");

        IndexWrapperLuceneImpl index = new IndexWrapperLuceneImpl(path);
        IndexReader lucene = (IndexReader)index.getActualIndex();
        if (cmd.equals("documentid") || cmd.equals("di")) {
            // Return the document ID given the specified field/value
            System.out.println(index.getDocId(field, arg));
        }
        else if (cmd.equals("stats") || cmd.equals("s")) {
            
            Fields fields = MultiFields.getFields(lucene); 
            System.out.println("Documents: \t" + (long)index.docCount());
            System.out.println("Unique terms: \t" + (long)index.termTypeCount());
            System.out.println("Total terms: \t" + (long)index.termCount());
            
            String fieldNames = "";
            Iterator<String> it = fields.iterator();
            while (it.hasNext()) {
                String fieldName = it.next();
                fieldNames +=  fieldName + " ";
            }

            System.out.println("Fields: \t" + fieldNames);
        }
        else if (cmd.equals("documenttext") || cmd.equals("dt")) {
            int docId = index.getDocId(docno, arg);
            System.out.println(index.getDocText(docId, field));
        }
        else if (cmd.equals("documentvector") || cmd.equals("dv")) {
            int docId = index.getDocId(docno, arg);
            FeatureVector fv = index.getDocVector(docId, field, null);
            Iterator<String> it = fv.iterator();
            System.out.println("Feature count: " + fv.getFeatureCount());
            System.out.println("Length " + fv.getLength());
            while (it.hasNext()) {
                String f = it.next();
                double w = fv.getFeatureWeight(f);
                System.out.println(f + "\t" + w) ;
            }
        }
        else if (cmd.equals("vocabulary") || cmd.equals("v")) {
            // 1
            Fields fields = MultiFields.getFields(lucene); 
            Terms terms = fields.terms(Indexer.FIELD_TEXT);
            TermsEnum termsEnum = terms.iterator(null);
            BytesRef byteRef = null;
            while((byteRef = termsEnum.next()) != null) {
                System.out.println(byteRef.utf8ToString() + "\t" + termsEnum.docFreq());
            }
        }
        else if (cmd.equals("xcount") || cmd.equals("x")) {
            // 3
            // dxcount
        }
    }
    
    public static Options createOptions()
    {
        Options options = new Options();
        options.addOption("index", true, "Path to index");
        options.addOption("field", true, "Target field (default: all)");
        options.addOption("docno", true, "Field containing document ID (default: docno)");
        options.addOption("cmd", true, "Command (e.g., dv)");
        options.addOption("arg", true, "Command argument (e.g., docno)");
        options.addOption("help", false, "Print this help message");

        return options;
    }
}
