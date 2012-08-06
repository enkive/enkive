package com.linuxbox.enkive.workspace.mongo;

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHCRITERIA;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHNAME;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.linuxbox.enkive.workspace.searchQuery.SearchQueryBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoSearchQueryBuilder implements SearchQueryBuilder {

	protected Mongo m;
	protected DB searchQueryDB;
	protected DBCollection searchQueryColl;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoSearchQueryBuilder(Mongo m, String searchQueryDBName,
			String searchQueryCollName) {
		this.m = m;
		searchQueryDB = m.getDB(searchQueryDBName);
		searchQueryColl = searchQueryDB.getCollection(searchQueryCollName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SearchQuery getSearchQuery(String searchQueryId)
			throws WorkspaceException {
		SearchQuery query = new MongoSearchQuery(m, searchQueryDB.getName(),
				searchQueryColl.getName());
		DBObject queryObject = searchQueryColl.findOne(ObjectId
				.massageToObjectId(searchQueryId));

		query.setId(searchQueryId);
		query.setName((String) queryObject.get(SEARCHNAME));
		query.setCriteria(((BasicDBObject) queryObject.get(SEARCHCRITERIA))
				.toMap());

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Retrieved Search Query " + query.getName() + " - "
					+ query.getId());
		return query;
	}

	@Override
	public SearchQuery getSearchQuery() throws WorkspaceException {
		return new MongoSearchQuery(m, searchQueryDB.getName(),
				searchQueryColl.getName());
	}

}
