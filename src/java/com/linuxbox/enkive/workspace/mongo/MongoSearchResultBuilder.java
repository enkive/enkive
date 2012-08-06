package com.linuxbox.enkive.workspace.mongo;

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.EXECUTEDBY;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.EXECUTIONTIMESTAMP;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHISSAVED;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHQUERYID;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHRESULTS;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHSTATUS;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.UUID;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;
import com.linuxbox.enkive.workspace.searchResult.SearchResult;
import com.linuxbox.enkive.workspace.searchResult.SearchResultBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoSearchResultBuilder implements SearchResultBuilder {

	Mongo m;
	DB searchResultsDB;
	DBCollection searchResultsColl;
	SearchQueryBuilder queryBuilder;
	MongoSearchResultUtils searchResultUtils;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoSearchResultBuilder(Mongo m, String searchResultsDBName,
			String searchResultsCollName, SearchQueryBuilder queryBuilder) {
		this.m = m;
		searchResultsDB = m.getDB(searchResultsDBName);
		searchResultsColl = searchResultsDB
				.getCollection(searchResultsCollName);
		this.queryBuilder = queryBuilder;
	}

	@Override
	public SearchResult getSearchResult() throws WorkspaceException {
		MongoSearchResult searchResult = new MongoSearchResult(m,
				searchResultsDB.getName(), searchResultsColl.getName(),
				queryBuilder);
		searchResult.setSearchResultUtils(searchResultUtils);
		return searchResult;
	}

	public SearchResult getSearchResult(String searchResultId)
			throws WorkspaceException {
		MongoSearchResult result = new MongoSearchResult(m,
				searchResultsDB.getName(), searchResultsColl.getName(),
				queryBuilder);
		DBObject searchResultObject = searchResultsColl.findOne(ObjectId
				.massageToObjectId(searchResultId));

		result.setId(searchResultId);
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

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Retrieved Search Results - " + result.getId());
		result.setSearchResultUtils(searchResultUtils);

		return result;
	}

	@Override
	public Collection<SearchResult> getSearchResults(
			Collection<String> searchResultUUIDs) {

		Collection<SearchResult> results = new HashSet<SearchResult>();

		BasicDBObject query = new BasicDBObject();
		BasicDBList idList = new BasicDBList();
		for (String searchResultUUID : searchResultUUIDs)
			idList.add(ObjectId.massageToObjectId(searchResultUUID));
		query.put("$in", idList);
		DBCursor searchResult = searchResultsColl.find(new BasicDBObject(UUID,
				query));
		while (searchResult.hasNext()) {
			MongoSearchResult result = new MongoSearchResult(m,
					searchResultsDB.getName(), searchResultsColl.getName(),
					queryBuilder);
			DBObject searchResultObject = searchResult.next();
			result.setId(((ObjectId) searchResultObject.get(UUID)).toString());
			result.setTimestamp((Date) searchResultObject
					.get(EXECUTIONTIMESTAMP));
			result.setExecutedBy((String) searchResultObject.get(EXECUTEDBY));

			BasicDBList searchResults = (BasicDBList) searchResultObject
					.get(SEARCHRESULTS);

			Set<String> searchResultMessageUUIDs = new HashSet<String>();
			Iterator<Object> searchResultsIterator = searchResults.iterator();
			while (searchResultsIterator.hasNext())
				searchResultMessageUUIDs.add((String) searchResultsIterator
						.next());

			result.setMessageIds(searchResultMessageUUIDs);

			result.setStatus(SearchResult.Status
					.valueOf((String) searchResultObject.get(SEARCHSTATUS)));
			result.setSearchQueryId((String) searchResultObject
					.get(SEARCHQUERYID));
			if (searchResultObject.get(SEARCHISSAVED) != null)
				result.setSaved((Boolean) searchResultObject.get(SEARCHISSAVED));
			result.setSearchResultUtils(searchResultUtils);
			results.add(result);

		}
		return results;
	}

	public MongoSearchResultUtils getSearchResultUtils() {
		return searchResultUtils;
	}

	public void setSearchResultUtils(MongoSearchResultUtils searchResultUtils) {
		this.searchResultUtils = searchResultUtils;
	}

}
