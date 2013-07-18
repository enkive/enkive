/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
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
 *******************************************************************************/
package com.linuxbox.enkive.workspace.migration;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;

import com.linuxbox.util.dbinfo.DbInfo;
import com.linuxbox.util.dbinfo.MultiDbInfo;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.linuxbox.util.dbmigration.AbstractColletionMigrator;
import com.linuxbox.util.dbmigration.AbstractDocumentMigrator;
import com.linuxbox.util.dbmigration.DbMigration;
import com.linuxbox.util.dbmigration.DbMigrationException;
import com.linuxbox.util.dbmigration.DbMigrator;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Migrates the Workspace data from DB version 1 to 2.  This changes the relative uses
 * of query and results.  This means that the UI and workspaces reference
 * queries, and the query references it's result.  Metadata is moved from result
 * to query, and references are updated.
 */
public class WorkspaceMigration1to2 extends DbMigration {

	public static class Workspace1to2Constants {
		public static String UUID = "_id";
		public static String CREATIONDATE = "CreationDate";
		public static String MODIFIEDDATE = "ModifiedUpdate";
		public static String CREATOR = "Creator";
		public static String WORKSPACENAME = "WorkspaceName";
		public static String LASTQUERY = "LastQuery";
		public static String SEARCHES = "SearchQueries";
		public static String SEARCHQUERYID = "SearchQueryId";
		public static String SEARCHRESULTS = "SearchResults";
		public static String SEARCHNAME = "SearchName";
		public static String SEARCHCRITERIA = "SearchCriteria";
		public static String SEARCHRESULTID = "SearchResultId";
		public static String SEARCHSTATUS = "Status";
		public static String SEARCHISSAVED = "IsSaved";
		public static String EXECUTIONTIMESTAMP = "ExecutionTimestamp";
		public static String EXECUTEDBY = "ExecutedBy";
		public static String SEARCHFOLDERID = "SearchFolderId";
		public static String LASTMONOTONICID = "LastMonotonicID";
	}

	public class MongoResultDoc1to2 extends AbstractDocumentMigrator {
		private static final String COLLECTIONNAME = "Workspace Search Results Collection";
		public String id;
		public Date timestamp;
		public String executedBy;
		public Set<String> messageIds;
		public String status;
		public String queryId;
		public Boolean isSaved = false;

		public MongoResultDoc1to2(DbInfo dbInfo) {
			super(WorkspaceMigration1to2.getCollection(dbInfo, COLLECTIONNAME));
		}

		public void load(String id) {
			DBObject resultObject = collection.findOne(ObjectId.massageToObjectId(id));

			this.id = id;
			timestamp = (Date) resultObject.get(Workspace1to2Constants.EXECUTIONTIMESTAMP);
			executedBy = (String) resultObject.get(Workspace1to2Constants.EXECUTEDBY);

			BasicDBList searchResults = (BasicDBList) resultObject
					.get(Workspace1to2Constants.SEARCHRESULTS);

			messageIds = new HashSet<String>();
			Iterator<Object> searchResultsIterator = searchResults.iterator();
			while (searchResultsIterator.hasNext())
				messageIds.add((String) searchResultsIterator.next());

			status = (String) resultObject.get(Workspace1to2Constants.SEARCHSTATUS);
			queryId = (String) resultObject.get(Workspace1to2Constants.SEARCHQUERYID);
			if (resultObject.get(Workspace1to2Constants.SEARCHISSAVED) != null)
				isSaved = (Boolean) resultObject.get(Workspace1to2Constants.SEARCHISSAVED);
		}

		public void migrateDocumentImpl() throws DbMigrationException {
			// Nothing to do; Fields are only deleted, and that's done in MongoResultColl1to2
		}
	}

	public class MongoQueryDoc1to2 extends AbstractDocumentMigrator {
		private static final String COLLECTIONNAME = "Workspace Search Queries Collection";
		private String id;
		private String name;
		private Map<String, String> criteria;
		private String executedBy;
		private Boolean isSaved = false;
		private Date timestamp;
		private String status;
		private String resultId;

		private DbInfo dbInfo;
		private MongoResultDoc1to2 result;

		public MongoQueryDoc1to2(DbInfo dbInfo) {
			super(WorkspaceMigration1to2.getCollection(dbInfo, COLLECTIONNAME));
			this.dbInfo = dbInfo;
		}

		public String getId() {
			return this.id;
		}

		public void loadByResultId(String resultId) {
			result = new MongoResultDoc1to2(dbInfo);
			result.load(resultId);
			load(result.queryId);
		}

		@SuppressWarnings("unchecked")
		public void load(String id) {
			DBObject queryObject = collection.findOne(ObjectId.massageToObjectId(id));

			this.id = id;
			name = (String) queryObject.get(Workspace1to2Constants.SEARCHNAME);
			resultId = (String) queryObject.get(Workspace1to2Constants.SEARCHRESULTID);
			criteria = ((BasicDBObject) queryObject.get(Workspace1to2Constants.SEARCHCRITERIA)).toMap();
			timestamp = (Date) queryObject.get(Workspace1to2Constants.EXECUTIONTIMESTAMP);
			executedBy = (String) queryObject.get(Workspace1to2Constants.EXECUTEDBY);
			status = (String) queryObject.get(Workspace1to2Constants.SEARCHSTATUS);
			if (queryObject.get(Workspace1to2Constants.SEARCHISSAVED) != null)
				isSaved = (Boolean) queryObject.get(Workspace1to2Constants.SEARCHISSAVED);
		}

		/**
		 * Migrate a SearchQuery document.  Copy all the metadata from the associated
		 * SearchResult, and save the query.
		 */
		public void migrateDocumentImpl() throws DbMigrationException {
			// Load values that are being moved
			this.resultId = result.id;
			timestamp = result.timestamp;
			executedBy = result.executedBy;
			status = result.status;
			isSaved = result.isSaved;
			save();
		}

		private void save() {
			BasicDBObject queryObject = new BasicDBObject();
			queryObject.put(Workspace1to2Constants.SEARCHNAME, name);
			queryObject.put(Workspace1to2Constants.SEARCHRESULTID, resultId);
			queryObject.put(Workspace1to2Constants.SEARCHCRITERIA, criteria);
			queryObject.put(Workspace1to2Constants.EXECUTIONTIMESTAMP, timestamp);
			queryObject.put(Workspace1to2Constants.EXECUTEDBY, executedBy);
			queryObject.put(Workspace1to2Constants.SEARCHSTATUS, status);
			queryObject.put(Workspace1to2Constants.SEARCHISSAVED, isSaved);
			queryObject.put(Workspace1to2Constants.LASTMONOTONICID, null);

			DBObject toUpdate = collection.findOne(ObjectId.massageToObjectId(id));
			collection.update(toUpdate, queryObject);
		}
	}

	public class MongoWorkspaceDoc1to2 extends AbstractDocumentMigrator {
		private static final String COLLECTIONNAME = "Workspaces Collection";
		private String workspaceUUID;
		private String workspaceName;
		private String creator = "";
		private Date creationDate;
		private String lastQueryUUID;
		private Collection<String> searchUUIDs;

		private DbInfo dbInfo;

		public MongoWorkspaceDoc1to2(DBObject workspaceObject, DbInfo dbInfo) {
			super(WorkspaceMigration1to2.getCollection(dbInfo, COLLECTIONNAME));
			this.dbInfo = dbInfo;

			workspaceUUID = workspaceObject.get(Workspace1to2Constants.UUID).toString();
			creationDate = (Date) workspaceObject.get(Workspace1to2Constants.CREATIONDATE);
			creator = (String) workspaceObject.get(Workspace1to2Constants.CREATOR);
			workspaceName = (String) workspaceObject.get(Workspace1to2Constants.WORKSPACENAME);
			lastQueryUUID = (String) workspaceObject.get(Workspace1to2Constants.LASTQUERY);

			BasicDBList searches = (BasicDBList) workspaceObject.get(Workspace1to2Constants.SEARCHES);

			searchUUIDs = new HashSet<String>();
			Iterator<Object> searchesIterator = searches.iterator();
			while (searchesIterator.hasNext())
				searchUUIDs.add((String) searchesIterator.next());
		}

		/**
		 * Migrate a Workspace document.  For each result in the workspace, migrate
		 * the query associated with it, and save the query's ID in a new list.
		 * Replace the list of results with a list of queries, and save.
		 */
		@Override
		public void migrateDocumentImpl() throws DbMigrationException {
			// Need to convert request UUIDs to query UUIDs
			Collection<String> queryUUIDs = new HashSet<String>();

			for (String id: searchUUIDs) {
				MongoQueryDoc1to2 query = new MongoQueryDoc1to2(dbInfo);
				query.loadByResultId(id);
				query.migrateDocument();
				queryUUIDs.add(query.getId());
			}

			searchUUIDs = queryUUIDs;
			save();
		}

		private void save() {
			BasicDBObject workspaceObject = new BasicDBObject();
			workspaceObject.put(Workspace1to2Constants.CREATIONDATE, creationDate);
			workspaceObject.put(Workspace1to2Constants.MODIFIEDDATE,
					new Date(System.currentTimeMillis()));
			workspaceObject.put(Workspace1to2Constants.CREATOR, creator);
			workspaceObject.put(Workspace1to2Constants.WORKSPACENAME, workspaceName);
			workspaceObject.put(Workspace1to2Constants.SEARCHES, searchUUIDs);
			workspaceObject.put(Workspace1to2Constants.LASTQUERY, lastQueryUUID);
			workspaceObject.put(Workspace1to2Constants.LASTQUERY, lastQueryUUID);

			DBObject toUpdate = collection.findOne(ObjectId.massageToObjectId(workspaceUUID));
			collection.update(toUpdate, workspaceObject);
		}
	}

	public class MongoResultColl1to2 extends AbstractColletionMigrator {
		// Dead Fields in collection
		public final String[] DEADFIELDS = {
				Workspace1to2Constants.EXECUTIONTIMESTAMP,
				Workspace1to2Constants.EXECUTEDBY,
				Workspace1to2Constants.SEARCHSTATUS,
				Workspace1to2Constants.SEARCHISSAVED,
				};
		private static final String COLLECTIONNAME = "Workspace Search Results Collection";

		public MongoResultColl1to2(DbInfo dbInfo) {
			super(WorkspaceMigration1to2.getCollection(dbInfo, COLLECTIONNAME));

			setDeadFields(Arrays.asList(DEADFIELDS));
			renamedFields = null;
		}

		@Override
		public void migrateCollectionImpl() throws DbMigrationException {
			// Nothing to do here; only field names modified
		}

	}

	public class MongoWorkspaceColl1to2 extends AbstractColletionMigrator {
		// Dead Fields in collection
		public final String[] DEADFIELDS = { Workspace1to2Constants.SEARCHFOLDERID };
		// Old changed Fields in collection
		public static final String SEARCHRESULTS = "SearchResults";
		private static final String COLLECTIONNAME = "Workspaces Collection";

		private DbInfo dbInfo;

		public MongoWorkspaceColl1to2(DbInfo dbInfo) {
			super(WorkspaceMigration1to2.getCollection(dbInfo, COLLECTIONNAME));

			this.dbInfo = dbInfo;
			setDeadFields(Arrays.asList(DEADFIELDS));
			renamedFields = new HashMap<String, String>();
			renamedFields.put(SEARCHRESULTS, Workspace1to2Constants.SEARCHES);
		}

		/**
		 * Migrate the Workspace collection.  Just walk the workspaces migrating them.
		 */
		@Override
		public void migrateCollectionImpl() throws DbMigrationException {
			DBCursor cursor = collection.find();

			while (cursor.hasNext()) {
				MongoWorkspaceDoc1to2 workspace = new MongoWorkspaceDoc1to2(cursor.next(), dbInfo);
				workspace.migrateDocument();
			}
		}

	}

	public WorkspaceMigration1to2(DbMigrator migrator) throws DbMigrationException {
		super(migrator, 1, 2);
	}

	@Override
	public void migrate(DbInfo dbInfo) throws DbMigrationException {
		LOGGER.info("Running Workspace migration 1 to 2");

		MongoWorkspaceColl1to2 wsMigrator = new MongoWorkspaceColl1to2(dbInfo);
		MongoResultColl1to2 resultMigrator = new MongoResultColl1to2(dbInfo);
		try {
			wsMigrator.migrateCollection();
			resultMigrator.migrateCollection();
			// We also need to drop the (now unused) searchFolders and
			// searchFoldersSearchResults collections.
			WorkspaceMigration1to2.getCollection(dbInfo, "Workspace Search Folders Collection").drop();
			WorkspaceMigration1to2.getCollection(dbInfo, "Workspace Search Folder Results Collection").drop();
		} catch (Exception e) {
			throw new DbMigrationException("Failed to migrate Workspace from 1 to 2: ", e);
		}
	}

	public static DBCollection getCollection(DbInfo dbInfo, String serviceName) {
		MultiDbInfo multiDbInfo = (MultiDbInfo) dbInfo;
		MongoDbInfo mongoDbInfo = (MongoDbInfo)multiDbInfo.getByServiceName(serviceName);
		return mongoDbInfo.getCollection();
	}


}
