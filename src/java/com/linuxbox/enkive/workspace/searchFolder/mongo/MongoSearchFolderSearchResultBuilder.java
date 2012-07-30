package com.linuxbox.enkive.workspace.searchFolder.mongo;

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.EXECUTEDBY;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.EXECUTIONTIMESTAMP;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHISSAVED;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHQUERYID;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHRESULTS;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHSTATUS;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.mongo.MongoSearchResult;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResult;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResultBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoSearchFolderSearchResultBuilder implements
		SearchFolderSearchResultBuilder {

	Mongo m;
	DB searchFolderSearchResultDB;
	DBCollection searchFolderSearchResultColl;

	MongoSearchFolderSearchResultBuilder(Mongo m,
			String searchFolderSearchResultDBName,
			String searchFolderSearchResultCollName) {
		this.m = m;
		searchFolderSearchResultDB = m.getDB(searchFolderSearchResultDBName);
		searchFolderSearchResultColl = searchFolderSearchResultDB
				.getCollection(searchFolderSearchResultCollName);

	}

	@Override
	public SearchFolderSearchResult getSearchResult() {
		return new MongoSearchFolderSearchResult(m,
				searchFolderSearchResultDB.getName(),
				searchFolderSearchResultColl.getName());
	}

	@Override
	public SearchFolderSearchResult getSearchResult(String id) {
		MongoSearchFolderSearchResult result = new MongoSearchFolderSearchResult(m,
				searchFolderSearchResultDB.getName(),
				searchFolderSearchResultColl.getName());
		DBObject searchResultObject = searchFolderSearchResultColl.findOne(ObjectId
				.massageToObjectId(id));

		result.setId(id);
		result.setTimestamp((Date) searchResultObject.get(EXECUTIONTIMESTAMP));
		result.setExecutedBy((String) searchResultObject.get(EXECUTEDBY));

		BasicDBList searchResults = (BasicDBList) searchResultObject
				.get(SEARCHRESULTS);

		Set<String> searchResultUUIDs = new HashSet<String>();
		Iterator<Object> searchResultsIterator = searchResults.iterator();
		while (searchResultsIterator.hasNext())
			searchResultUUIDs.add((String) searchResultsIterator.next());

		result.setMessageIds(searchResultUUIDs);

		result.setStatus(SearchResult.Status
				.valueOf((String) searchResultObject.get(SEARCHSTATUS)));
		result.setSearchQueryId((String) searchResultObject.get(SEARCHQUERYID));
		if (searchResultObject.get(SEARCHISSAVED) != null)
			result.setSaved((Boolean) searchResultObject.get(SEARCHISSAVED));

		//if (LOGGER.isInfoEnabled())
		//	LOGGER.info("Retrieved Search Results - " + result.getId());
		//result.setSearchResultUtils(searchResultUtils);

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
		mSearchResult.saveSearchResult();
		return mSearchResult;
	}

}
