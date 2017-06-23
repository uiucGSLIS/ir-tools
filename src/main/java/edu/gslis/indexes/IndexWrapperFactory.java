package edu.gslis.indexes;

import java.io.File;


/**
 * Attempts to auto-dected index type.
 * @author cwillis
 */
public class IndexWrapperFactory {

    public static IndexWrapper getIndexWrapper(String path) 
    {
        File manifest = new File(path + File.separator + "manifest");
        
        if (manifest.exists()) {
            return new IndexWrapperIndriImpl(path);
        }
        else {
            return new IndexWrapperLuceneImpl(path);
        }
    }
}
