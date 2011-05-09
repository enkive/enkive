package com.linuxbox.enkive.docsearch;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.exception.DocStoreException;

public interface SearchService {
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

	// SEARCHING

	/*
	 * NOTE: We will likely want to abstract the 'search' methods further after
	 * additional thought, so as not to have to send in a query via the specific
	 * implementation.
	 */

	/**
	 * Perform a search and return a list of document identifiers that match.
	 * 
	 * @param query
	 * 
	 * @return
	 * @throws DocSearchException 
	 */
	List<String> search(String query) throws DocSearchException;

	/**
	 * Perform a search and return a list of document identifiers that match, at
	 * most maxResults of them.
	 * 
	 * @param query
	 * @param maxResults
	 * @return
	 */

	List<String> search(String query, int maxResults) throws DocSearchException;

	// PUSH INDEXING

	void indexDocument(String identifier) throws DocSearchException, DocStoreException;

	void indexDocuments(Collection<String> identifiers) throws IOException,
			DocStoreException, DocSearchException;

	// PULL INDEXING

	void setUnindexedDocSearchInterval(int milliseconds);
}
