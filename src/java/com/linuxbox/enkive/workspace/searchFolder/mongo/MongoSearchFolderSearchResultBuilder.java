package com.linuxbox.enkive.workspace.searchFolder.mongo;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.mongo.MongoSearchResultBuilder;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResult;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResultBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoSearchFolderSearchResultBuilder implements
		SearchFolderSearchResultBuilder {

	Mongo m;
	DB searchFolderSearchResultDB;
	DBCollection searchFolderSearchResultColl;
	MongoSearchResultBuilder mSearchResultBuilder;

	// XXX Need to set mongosearchutils

	MongoSearchFolderSearchResultBuilder(Mongo m,
			String searchFolderSearchResultDBName,
			String searchFolderSearchResultCollName) {
		this.m = m;
		searchFolderSearchResultDB = m.getDB(searchFolderSearchResultDBName);
		searchFolderSearchResultColl = searchFolderSearchResultDB
				.getCollection(searchFolderSearchResultCollName);

		mSearchResultBuilder = new MongoSearchResultBuilder(m,
				searchFolderSearchResultDBName,
				searchFolderSearchResultCollName, null);
	}

	@Override
	public SearchFolderSearchResult getSearchResult() {
		return new MongoSearchFolderSearchResult(m,
				searchFolderSearchResultDB.getName(),
				searchFolderSearchResultColl.getName());
	}

	@Override
	public SearchFolderSearchResult getSearchResult(String id)
			throws WorkspaceException {

		SearchResult tempSearchResult = mSearchResultBuilder
				.getSearchResult(id);
		SearchFolderSearchResult result = buildSearchResult(tempSearchResult);

		return result;
	}

	@Override
	public SearchFolderSearchResult buildSearchResult(SearchResult searchResult)
			throws WorkspaceException {

		SearchFolderSearchResult mSearchResult = new MongoSearchFolderSearchResult(
				m, searchFolderSearchResultDB.getName(),
				searchFolderSearchResultColl.getName());
		mSearchResult.setExecutedBy(searchResult.getExecutedBy());
		mSearchResult.setMessageIds(searchResult.getMessageIds());
		mSearchResult.setSearchQueryBuilder(searchResult
				.getSearchQueryBuilder());
		mSearchResult.setSearchQueryId(searchResult.getSearchQueryId());
		mSearchResult.setTimestamp(searchResult.getTimestamp());
		// TODO Can we have a check to see if this is actually a searchfolder
		// result?
		mSearchResult.saveSearchResult();
		return mSearchResult;
	}

}
