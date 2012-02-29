/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
 ******************************************************************************/
package com.linuxbox.enkive.docsearch;

import java.util.Collection;

import com.linuxbox.enkive.docsearch.contentanalyzer.ContentAnalyzer;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.exception.DocStoreException;

public interface DocSearchIndexService {
	/**
	 * Shut down the service and release any resources used by the service.
	 * 
	 * @throws DocSearchException
	 */
	void startup() throws DocSearchException;

	/**
	 * Shut down the service and release any resources used by the service.
	 * 
	 * @throws DocSearchException
	 */
	void shutdown() throws DocSearchException;

	// INITIALIZATION

	void setDocStoreService(DocStoreService service);

	void setContentAnalyzer(ContentAnalyzer analyzer);

	// PUSH INDEXING

	void indexDocument(String identifier) throws DocSearchException,
			DocStoreException;

	void indexDocuments(Collection<String> identifiers)
			throws DocStoreException, DocSearchException;

	// PULL INDEXING

	void setUnindexedDocRePollInterval(int milliseconds);

	// REMOVE

	/**
	 * Remove a document from the index.
	 */
	void removeDocument(String identifier) throws DocSearchException;
}
