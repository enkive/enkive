package com.linuxbox.enkive.search.indri;

import java.util.List;

import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.exception.UnimplementedMethodException;
import com.linuxbox.enkive.search.AbstractSearchService;
import com.linuxbox.enkive.search.contentanalyzer.ContentAnalyzer;

public class IndriSearchService extends AbstractSearchService {

	public IndriSearchService(DocStoreService service, ContentAnalyzer analyzer) {
		super(service, analyzer);
		// TODO Auto-generated constructor stub
	}

	public IndriSearchService(DocStoreService service,
			ContentAnalyzer analyzer, int unindexedDocSearchInterval) {
		super(service, analyzer, unindexedDocSearchInterval);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void indexDocument(String identifer) {
		// TODO Auto-generated method stub
		throw new UnimplementedMethodException();
	}

	@Override
	public List<String> search(String query) {
		// TODO Auto-generated method stub
		throw new UnimplementedMethodException();
	}
}
