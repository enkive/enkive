package com.linuxbox.enkive.docsearch;

import java.io.IOException;
import java.util.Collection;

import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.exception.DocStoreException;

public interface DocSearchIndexService {
	/**
	 * Shut down the service and release any resources used by the service.
	 * @throws DocSearchException 
	 */
	void startup() throws DocSearchException;
	
	/**
	 * Shut down the service and release any resources used by the service.
	 * @throws DocSearchException 
	 */
	void shutdown() throws DocSearchException;

	// INITIALIZATION

	void setDocStoreService(DocStoreService service);

	void setContentAnalyzer(ContentAnalyzer analyzer);

	// PUSH INDEXING

	void indexDocument(String identifier) throws DocSearchException, DocStoreException;

	void indexDocuments(Collection<String> identifiers) throws IOException,
			DocStoreException, DocSearchException;

	// PULL INDEXING

	void setUnindexedDocSearchInterval(int milliseconds);
}
