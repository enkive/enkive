package com.linuxbox.enkive.message.search;

import java.util.HashMap;
import java.util.concurrent.Future;

import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.SearchResult;

public interface MessageSearchService {

	/**
	 * Perform a search and return a set of message identifiers that match.
	 * 
	 * @param fields
	 * 
	 * @return
	 * @throws DocSearchException
	 */
	SearchResult search(HashMap<String, String> fields)
			throws MessageSearchException;

	/**
	 * Perform an asynchronous search and return a Future<SearchResult> object.
	 * 
	 * @param fields
	 * @return
	 * 
	 * @throws DocSearchException
	 */
	Future<SearchResult> searchAsync(HashMap<String, String> fields)
			throws MessageSearchException;

	/**
	 * Cancel an asynchronous search based on the search identifier
	 * 
	 * @param searchId
	 * @return
	 * @throws MessageSearchException
	 */
	boolean cancelAsyncSearch(String searchId) throws MessageSearchException;

}
