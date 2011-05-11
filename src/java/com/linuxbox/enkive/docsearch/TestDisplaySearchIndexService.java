package com.linuxbox.enkive.docsearch;

import java.io.Reader;

import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;

public class TestDisplaySearchIndexService extends AbstractDocSearchIndexService {

	public TestDisplaySearchIndexService(DocStoreService service,
			ContentAnalyzer analyzer) {
		super(service, analyzer);
	}

	public TestDisplaySearchIndexService(DocStoreService service,
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
	public void subStartup() {
		// empty
	}

	@Override
	public void subShutdown() {
		// nothing to do
	}
}
