package com.linuxbox.enkive.docstore;

import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.exception.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.exception.StorageException;

public interface DocStoreService {
	/**
	 * Shut down the service and release any resources used by the service.
	 */
	void shutdown();

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
	 * Retrieve the (earliest) un-indexed document. May mark the document as
	 * being in the process of being indexed, which is different than having
	 * been indexed.
	 * 
	 * @return The identifier of a document that's not been indexed.
	 */
	String nextUnindexed();

	/**
	 * Marks the given document as having been indexed, so it will not be
	 * retrieved as un-indexed again.
	 * 
	 * @param identifier
	 * @throws DocumentNotFoundException 
	 */
	void markAsIndexed(String identifier) throws DocumentNotFoundException;
}
