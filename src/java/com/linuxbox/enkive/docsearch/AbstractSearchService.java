package com.linuxbox.enkive.docsearch;

import java.util.Collection;
import java.util.List;

import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.exception.DocStoreException;

public abstract class AbstractSearchService implements SearchService {
	/**
	 * The maximum search results to return by default.
	 */
	private static final int DEFAULT_MAX_SEARCH_RESULTS = 25;

	/**
	 * The document storage service we're feeding off of.
	 */
	protected DocStoreService docStoreService;

	protected ContentAnalyzer contentAnalyzer;

	/**
	 * In milliseconds; non-positive values indicate that there is no automated
	 * query for un-indexed documents
	 */
	protected int unindexedDocSearchInterval = -1;

	@Override
	public List<String> search(String query) throws DocSearchException {
		return search(query, DEFAULT_MAX_SEARCH_RESULTS);
	}

	public AbstractSearchService(DocStoreService service,
			ContentAnalyzer analyzer) {
		setDocStoreService(service);
		setContentAnalyzer(analyzer);
	}

	public AbstractSearchService(DocStoreService service,
			ContentAnalyzer analyzer, int unindexedDocSearchInterval) {
		this(service, analyzer);
		setUnindexedDocSearchInterval(unindexedDocSearchInterval);
	}

	public abstract void doIndexDocument(String identifier)
			throws DocStoreException, DocSearchException;

	@Override
	public final void indexDocument(String identifier)
			throws DocStoreException, DocSearchException {
		doIndexDocument(identifier);
		docStoreService.markAsIndexed(identifier);
	}

	@Override
	public void indexDocuments(Collection<String> identifiers)
			throws DocStoreException, DocSearchException {
		for (String identifier : identifiers) {
			indexDocument(identifier);
		}
	}

	@Override
	public void setDocStoreService(DocStoreService service) {
		this.docStoreService = service;
	}

	@Override
	public void setContentAnalyzer(ContentAnalyzer analyzer) {
		this.contentAnalyzer = analyzer;
	}

	@Override
	public void setUnindexedDocSearchInterval(int milliseconds) {
		unindexedDocSearchInterval = milliseconds;
	}
}
