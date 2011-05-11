package com.linuxbox.enkive.docsearch;

import java.io.Reader;
import java.util.List;

import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.exception.UnimplementedMethodException;

public class TestDisplaySearchService extends AbstractSearchService {

	public TestDisplaySearchService(DocStoreService service,
			ContentAnalyzer analyzer) {
		super(service, analyzer);
	}

	public TestDisplaySearchService(DocStoreService service,
			ContentAnalyzer analyzer, int unindexedDocSearchInterval) {
		super(service, analyzer, unindexedDocSearchInterval);
	}

	@Override
	public void doIndexDocument(String identifier) throws DocSearchException {
		try {
			Document d = docStoreService.retrieve(identifier);
			Reader r = contentAnalyzer.parseIntoText(d);
			int c;
			while ((c = r.read()) >= 0) {
				System.out.print((char) c);
			}
		} catch (Exception e) {
			throw new DocSearchException(e);
		}
	}

	@Override
	public List<String> search(String query) {
		throw new UnimplementedMethodException();
	}

	@Override
	public List<String> search(String query, int maxResults) {
		throw new UnimplementedMethodException();
	}

	@Override
	public void subStartup() {
		// empty
	}

	@Override
	public void subShutdown() {
		// nothing to do
	}
}
