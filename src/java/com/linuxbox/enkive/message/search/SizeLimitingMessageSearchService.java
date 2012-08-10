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
	public int countSearch(HashMap<String, String> fields)
			throws MessageSearchException {
		// TODO Auto-generated method stub
		return 0;
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
