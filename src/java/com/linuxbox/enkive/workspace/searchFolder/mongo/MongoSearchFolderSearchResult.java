package com.linuxbox.enkive.workspace.searchFolder.mongo;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.mongo.MongoSearchResult;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoSearchFolderSearchResult extends SearchFolderSearchResult {

	Mongo m;
	DB searchFolderDB;
	DBCollection searchFolderSearchResultsColl;

	public MongoSearchFolderSearchResult(Mongo m,
			String searchFolderSearchResultsDBName,
			String searchFolderSearchResultsCollName) {
		this.m = m;
		this.searchFolderDB = m.getDB(searchFolderSearchResultsDBName);
		this.searchFolderSearchResultsColl = searchFolderDB
				.getCollection(searchFolderSearchResultsCollName);
	}

	@Override
	public void saveSearchResult() throws WorkspaceException {
		MongoSearchResult mSearchResult = new MongoSearchResult(m,
				searchFolderDB.getName(),
				searchFolderSearchResultsColl.getName(), queryBuilder);

		mSearchResult.setExecutedBy(getExecutedBy());
		mSearchResult.setMessageIds(getMessageIds());
		mSearchResult.setSearchQueryBuilder(getSearchQueryBuilder());
		mSearchResult.setSearchQueryId(getSearchQueryId());
		mSearchResult.setTimestamp(getTimestamp());
		mSearchResult.setId(getId());
		mSearchResult.saveSearchResult();
		// Set the ID in case the ID was not set previously
		setId(mSearchResult.getId());

	}

	@Override
	public void deleteSearchResult() throws WorkspaceException {
		MongoSearchResult mSearchResult = new MongoSearchResult(m,
				searchFolderDB.getName(),
				searchFolderSearchResultsColl.getName(), queryBuilder);

		mSearchResult.setExecutedBy(getExecutedBy());
		mSearchResult.setMessageIds(getMessageIds());
		mSearchResult.setSearchQueryBuilder(getSearchQueryBuilder());
		mSearchResult.setSearchQueryId(getSearchQueryId());
		mSearchResult.setTimestamp(getTimestamp());
		mSearchResult.setId(getId());
		mSearchResult.deleteSearchResult();

	}

}
