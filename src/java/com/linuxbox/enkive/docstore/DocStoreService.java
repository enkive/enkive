package com.linuxbox.enkive.docstore;

import com.linuxbox.enkive.docstore.exceptions.DocStoreException;
import com.linuxbox.enkive.docstore.exceptions.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.exceptions.StorageException;

public interface DocStoreService {
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
	String store(Document document) throws DocStoreException;

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
	 * @return A document that has not been indexed.
	 */
	Document retrieveUnindexed();

	/**
	 * Marks the given document as having been indexed, so it will not be
	 * retrieved as un-indexed again.
	 * 
	 * @param identifier
	 */
	void markAsIndexed(String identifier);
}
