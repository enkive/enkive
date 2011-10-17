package com.linuxbox.enkive.message.search;

import java.util.HashMap;
import java.util.Set;

import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;

public interface MessageSearchService {

	/**
	 * Perform a search and return a set of message identifiers that match.
	 * 
	 * @param fields
	 * 
	 * @return
	 * @throws DocSearchException
	 */
	Set<String> search(HashMap<String, String> fields)
			throws MessageSearchException;

	/**
	 * Perform an asynchronous search and return a list of message identifiers
	 * that match.
	 * 
	 * @param fields
	 * 
	 * @return
	 * @throws DocSearchException
	 */
	Set<String> searchAsync(HashMap<String, String> fields)
			throws MessageSearchException;

}
