package com.linuxbox.enkive.docstore;

import com.linuxbox.enkive.docstore.exceptions.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.exceptions.NoEncodingAvailableException;
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
	String store(Document document) throws StorageException;

	/**
	 * Retrieves a document given its unique identifier.
	 * 
	 * @param identifier
	 * @return
	 * @throws DocumentNotFoundException
	 */
	Document retrieve(String identifier) throws DocumentNotFoundException;

	/**
	 * Retrieves the encoded version of a document given its unique identifier.
	 * 
	 * @param identifier
	 * @return
	 * @throws NoEncodingAvailableException
	 */
	EncodedDocument retrieveEncoded(String identifier)
			throws NoEncodingAvailableException;
}
