/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
package com.linuxbox.enkive.docstore;

import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.exception.StorageException;

public interface DocStoreService {
	/**
	 * Starts up the service after it has been instantiated and all properties
	 * have been set by the container (e.g., Spring).
	 */
	void startup() throws DocStoreException;

	/**
	 * Shut down the service and release any resources used by the service.
	 */
	void shutdown() throws DocStoreException;

	/**
	 * Stores the given document and generates a unique identifier for the
	 * document, which is returned. If the document is already stored, it is not
	 * stored a second time, but instead the existing identifier is returned.
	 * This inherently does de-duplication.
	 * 
	 * @param document
	 * @return unique identifier for document
	 * @throws StorageException
	 */
	StoreRequestResult store(Document document) throws DocStoreException;

	/**
	 * Retrieves a document given its unique identifier.
	 * 
	 * @param identifier
	 * @return
	 * @throws DocumentNotFoundException
	 * @throws DocStoreException
	 */
	Document retrieve(String identifier) throws DocStoreException;

	/**
	 * Removes the specified document.
	 * 
	 * @param identifier
	 * @return true if the file was found and removed, false if the file was not
	 *         found, or throws an exception if there was an issue (for which a
	 *         retry might work)
	 * @throws DocStoreException
	 */
	boolean remove(String identifier) throws DocStoreException;

	/**
	 * The given document perhaps cannot be removed because another thread is
	 * controlling it (e.g., creating it). An exception is thrown, and this will
	 * retry a few times after waiting the specified time.
	 * 
	 * @param identifier
	 * @param numberOfAttempts
	 * @param millisecondsBetweenAttempts
	 * @return
	 */
	boolean removeWithRetries(String identifier, int numberOfAttempts,
			int millisecondsBetweenAttempts) throws DocStoreException;

	/**
	 * Retrieve the (earliest) un-indexed document. May mark the document as
	 * being in the process of being indexed, which is different than having
	 * been indexed.
	 * 
	 * @return The identifier of a document that's not been indexed.
	 */
	String nextUnindexed();

	/**
	 * Retrieve the (earliest) un-indexed document for the specific index server
	 * (assuming the indexing is sharded). Each server will have an index (0 to
	 * n-1) if there are n indexing servers.
	 * 
	 * @return The identifier of a document that's not been indexed.
	 */
	String nextUnindexed(int serverNumber, int serverCount);

	/**
	 * Marks the given document as having been indexed, so it will not be
	 * retrieved as un-indexed again.
	 * 
	 * @param identifier
	 * @throws DocStoreException
	 */
	void markAsIndexed(String identifier) throws DocStoreException;

	/**
	 * Marks the given document as having an error during indexing. Another
	 * process may be able to resolve this.
	 * 
	 * @param identifier
	 * @throws DocStoreException
	 */
	void markAsErrorIndexing(String identifier) throws DocStoreException;
}
