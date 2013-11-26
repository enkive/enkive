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
package com.linuxbox.enkive.docstore.migration;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.docstore.mongo.ConvenienceFileDocStoreService;
import com.linuxbox.enkive.docstore.mongogrid.ConvenienceMongoGridDocStoreService;
import com.linuxbox.util.dbinfo.DbInfo;
import com.linuxbox.util.dbinfo.MultiDbInfo;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.linuxbox.util.dbinfo.mongodb.MongoGridDbInfo;
import com.linuxbox.util.dbmigration.AbstractColletionMigrator;
import com.linuxbox.util.dbmigration.AbstractDocumentMigrator;
import com.linuxbox.util.dbmigration.DbMigration;
import com.linuxbox.util.dbmigration.DbMigrationException;
import com.linuxbox.util.dbmigration.DbMigrator;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;

/**
 * Migrates the DocStore data from DB version 2 to 3.  This adds a monotonic
 * ID field to every message.
 */
public class DocStoreMigration2to3 extends DbMigration {
	public static final String FILESTORE_INFO = "Document Storage File Service";
	public static final String GRIDSTORE_INFO = "Document Storage GridFS Service";

	public static final String FILE_BASE_PROP = "enkive.docstore.basepath";

	public static final String OBJECT_ID_KEY = "_id";
	public static final String FILENAME_KEY = "filename";
	public static final String METADATA_KEY = "metadata";
	public static final String MIME_TYPE_KEY = "contentType";
	public static final String GRID_FS_FILES_COLLECTION_SUFFIX = ".files";
	public static final String GRID_FS_CHUNKS_COLLECTION_SUFFIX = ".chunks";

	public DBCollection gridCollection;
	public ConvenienceFileDocStoreService fileDocStore;
	public ConvenienceMongoGridDocStoreService gridDocStore;

	public static class Doc2to3Constants {
		public static String MESSAGE_ID = "_id";
		public static String MONOTONIC_ID = "monotonic_id";
	}

	public class Doc2to3 extends AbstractDocumentMigrator {
		private DBObject object;

		public Doc2to3(DBObject object, DBCollection gridColl) {
			super(gridColl);
			this.object = object;
		}

		public void migrateDocumentImpl() throws DbMigrationException {
			String gridID = (String)object.get(FILENAME_KEY);

			try {
				Document doc = gridDocStore.retrieve(gridID);
				StoreRequestResult result = fileDocStore.store(doc);
				final String fileID = result.getIdentifier();

				// Don't re-index if it was already indexed
				if (gridDocStore.isIndexed(gridID)) {
					fileDocStore.markAsIndexed(fileID);
				}

				gridDocStore.remove(gridID);
			} catch (DocStoreException e) {
				throw new DbMigrationException("Failed to migrate " + gridID, e);
			}
		}
	}

	public class DocStoreColl2to3 extends AbstractColletionMigrator {
		public DocStoreColl2to3(DBCollection gridColl) {
			super(gridColl);

			setDeadFields(null);
			setRenamedFields(null);
		}

		/**
		 * Migrate the DocStore collection.  Just walk the messages migrating them.
		 */
		@Override
		public void migrateCollectionImpl() throws DbMigrationException {
			DBCursor cursor = collection.find();

			while (cursor.hasNext()) {
				Doc2to3 doc = new Doc2to3(cursor.next(), collection);
				doc.migrateDocument();
			}
		}

	}

	public DocStoreMigration2to3(DbMigrator migrator) throws DbMigrationException {
		super(migrator, 2, 3);
	}

	@Override
	public void migrate(DbInfo dbInfo) throws DbMigrationException {
		LOGGER.info("Running DocStore migration 2 to 3");

		MultiDbInfo multiDbInfo = (MultiDbInfo) dbInfo;
		MongoDbInfo fileDbInfo = (MongoDbInfo)multiDbInfo.getByServiceName(FILESTORE_INFO);
		MongoGridDbInfo gridDbInfo = (MongoGridDbInfo)multiDbInfo.getByServiceName(GRIDSTORE_INFO);
		gridCollection = gridDbInfo.getCollection();
		fileDocStore = new ConvenienceFileDocStoreService(
				props.getProperty(FILE_BASE_PROP, null), fileDbInfo.getCollection());
		gridDocStore = new ConvenienceMongoGridDocStoreService(gridDbInfo.getGridFs(), gridCollection);

		try {
			fileDocStore.startup();
			gridDocStore.startup();
		} catch (DocStoreException e) {
			throw new DbMigrationException("Failed to migrate DocStore from 2 to 3: failed to start FileDocStoreService: ", e);
		}

		try {
			DocStoreColl2to3 msMigrator = new DocStoreColl2to3(gridCollection);
			msMigrator.migrateCollection();
			gridCollection.drop();
		} catch (Exception e) {
			throw new DbMigrationException("Failed to migrate DocStore from 2 to 3: ", e);
		} finally {
			try {
				fileDocStore.shutdown();
			} catch (DocStoreException e) {
				// Shutting down... don't care
			}
			try {
				gridDocStore.shutdown();
			} catch (DocStoreException e) {
				// Shutting down... don't care
			}
		}
	}


}
