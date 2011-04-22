package com.linuxbox.enkive.search;

import java.util.Collection;
import java.util.List;

import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.search.contentanalyzer.ContentAnalyzer;

public interface SearchService {

	// INITIALIZATION

	void setDocStoreService(DocStoreService service);

	void setContentAnalyzer(ContentAnalyzer analyzer);

	// SEARCHING

	/**
	 * Perform a search and return a list of document identifiers that match.
	 * 
	 * @param query
	 * @return
	 */
	List<String> search(String query);

	// PUSH INDEXING

	void indexDocument(String identifer);

	void indexDocuments(Collection<String> identifers);

	// PULL INDEXING

	void setUnindexedDocSearchInterval(int milliseconds);
}
