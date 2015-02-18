/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.linuxbox.enkive.docsearch;

import java.io.Reader;

import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.Document;

public class TestDisplaySearchIndexService extends
		AbstractDocSearchIndexService {

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

	@Override
	public void doRemoveDocument(String identifier) {
		throw new RuntimeException("unimplemented method");
	}
}
