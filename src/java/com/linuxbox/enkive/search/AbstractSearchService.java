package com.linuxbox.enkive.search;

import java.util.Collection;

import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.search.contentanalyzer.ContentAnalyzer;

public abstract class AbstractSearchService implements SearchService {
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

	@Override
	public void indexDocuments(Collection<String> identifers) {
		for (String identifer : identifers) {
			indexDocument(identifer);
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
