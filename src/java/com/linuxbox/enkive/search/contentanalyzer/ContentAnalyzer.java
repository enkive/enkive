package com.linuxbox.enkive.search.contentanalyzer;

import java.io.IOException;
import java.io.Reader;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.exceptions.DocStoreException;

/**
 * Interface for a generalized content analzyer that turns documents from the
 * document storage service into text.
 * 
 * @author ivancich
 * 
 */
public interface ContentAnalyzer {
	Reader parseIntoText(Document document) throws IOException,
			DocStoreException;
}
