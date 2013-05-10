/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
package com.linuxbox.enkive.workspace.searchFolder.mongo;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.mongo.MongoSearchResultBuilder;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResult;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResultBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoSearchFolderSearchResultBuilder implements
		SearchFolderSearchResultBuilder {
	DBCollection searchFolderSearchResultColl;
	MongoSearchResultBuilder mSearchResultBuilder;

	// XXX Need to set mongosearchutils

	MongoSearchFolderSearchResultBuilder(Mongo m,
			String searchFolderSearchResultDBName,
			String searchFolderSearchResultCollName) {
		this(m.getDB(searchFolderSearchResultDBName).getCollection(
				searchFolderSearchResultCollName));
	}

	MongoSearchFolderSearchResultBuilder(MongoDbInfo dbInfo) {
		this(dbInfo.getCollection());
	}

	MongoSearchFolderSearchResultBuilder(
			DBCollection searchFolderSearchResultColl) {
		this.searchFolderSearchResultColl = searchFolderSearchResultColl;
		mSearchResultBuilder = new MongoSearchResultBuilder(
				searchFolderSearchResultColl, null);
	}

	@Override
	public SearchFolderSearchResult getSearchResult() {
		return new MongoSearchFolderSearchResult(searchFolderSearchResultColl);
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
				searchFolderSearchResultColl);
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
