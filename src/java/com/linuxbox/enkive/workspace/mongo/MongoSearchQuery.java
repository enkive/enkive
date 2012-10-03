package com.linuxbox.enkive.workspace.mongo;

import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHCRITERIA;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.SEARCHNAME;
import static com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.searchQuery.SearchQuery;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoSearchQuery extends SearchQuery {

	protected Mongo m;
	protected DB searchQueryDB;
	protected DBCollection searchQueryColl;

	private final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.workspaces");

	public MongoSearchQuery(Mongo m, String searchQueryDBName,
			String searchQueryCollName) {
		this.m = m;
		searchQueryDB = m.getDB(searchQueryDBName);
		searchQueryColl = searchQueryDB.getCollection(searchQueryCollName);
	}

	@Override
	public void saveSearchQuery() throws WorkspaceException {
		BasicDBObject searchQueryObject = new BasicDBObject();
		searchQueryObject.put(SEARCHNAME, getName());
		searchQueryObject.put(SEARCHCRITERIA, getCriteria());

		if (getId() != null && !getId().isEmpty()) {
			DBObject toUpdate = searchQueryColl.findOne(ObjectId
					.massageToObjectId(getId()));
			if (toUpdate != null) {
				searchQueryColl.update(toUpdate, searchQueryObject);
				searchQueryObject.put(UUID, toUpdate.get(UUID));
			}
		}
		if (searchQueryObject.getString(UUID) == null) {
			searchQueryColl.insert(searchQueryObject);
			setId(searchQueryObject.getString(UUID));
		}

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Saved Search Query - "
					+ searchQueryObject.getString(UUID));

	}

	@Override
	public void deleteSearchQuery() throws WorkspaceException {
		DBObject searchQueryObject = searchQueryColl.findOne(ObjectId
				.massageToObjectId(getId()));
		searchQueryColl.remove(searchQueryObject);
	}

}
