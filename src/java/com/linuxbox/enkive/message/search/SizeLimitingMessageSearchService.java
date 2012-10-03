/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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
package com.linuxbox.enkive.message.search;

import static com.linuxbox.enkive.search.Constants.LIMIT_PARAMETER;

import java.util.HashMap;
import java.util.concurrent.Future;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;

public class SizeLimitingMessageSearchService implements MessageSearchService {

	protected MessageSearchService messageSearchService;
	protected int sizeLimit = 0;

	public SizeLimitingMessageSearchService(
			MessageSearchService messageSearchService) {
		this.messageSearchService = messageSearchService;
	}

	@Override
	public SearchResult search(HashMap<String, String> fields)
			throws MessageSearchException {
		if (sizeLimit > 0)
			fields.put(LIMIT_PARAMETER, String.valueOf(sizeLimit));
		return messageSearchService.search(fields);
	}

	@Override
	public Future<SearchResult> searchAsync(HashMap<String, String> fields)
			throws MessageSearchException {
		return messageSearchService.searchAsync(fields);
	}

	@Override
	public boolean cancelAsyncSearch(String searchId)
			throws MessageSearchException {
		return messageSearchService.cancelAsyncSearch(searchId);
	}

	public int getSizeLimit() {
		return sizeLimit;
	}

	public void setSizeLimit(int sizeLimit) {
		this.sizeLimit = sizeLimit;
	}

}
