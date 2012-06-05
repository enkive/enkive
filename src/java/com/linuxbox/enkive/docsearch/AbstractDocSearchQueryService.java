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
 *******************************************************************************/
package com.linuxbox.enkive.docsearch;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.docsearch.exception.DocSearchException;

public abstract class AbstractDocSearchQueryService implements
		DocSearchQueryService {
	@SuppressWarnings("unused")
	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.docsearch");

	/**
	 * The maximum search results to return by default.
	 */
	private static final int DEFAULT_MAX_SEARCH_RESULTS = 100;

	protected int maxSearchResults;

	public AbstractDocSearchQueryService() {
		maxSearchResults = DEFAULT_MAX_SEARCH_RESULTS;
	}

	@Override
	public List<String> search(String query) throws DocSearchException {
		return search(query, maxSearchResults, false);
	}

	@Override
	public List<String> search(String query, int maxSearchResultsParam)
			throws DocSearchException {
		return search(query, maxSearchResultsParam, false);
	}

	@Override
	public List<String> search(String query, boolean rawSearch)
			throws DocSearchException {
		return search(query, maxSearchResults, rawSearch);
	}

	public int getMaxSearchResults() {
		return maxSearchResults;
	}

	public void setMaxSearchResults(int maxSearchResults) {
		this.maxSearchResults = maxSearchResults;
	}
}
