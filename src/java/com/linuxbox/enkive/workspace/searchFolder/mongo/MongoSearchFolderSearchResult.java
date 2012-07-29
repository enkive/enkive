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
