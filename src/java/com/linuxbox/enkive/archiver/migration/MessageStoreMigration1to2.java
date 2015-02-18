/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.archiver.migration;

import org.bson.types.ObjectId;

import com.linuxbox.util.dbinfo.DbInfo;
import com.linuxbox.util.dbinfo.MultiDbInfo;
import com.linuxbox.util.dbinfo.mongodb.MongoDbInfo;
import com.linuxbox.util.dbmigration.AbstractColletionMigrator;
import com.linuxbox.util.dbmigration.AbstractDocumentMigrator;
import com.linuxbox.util.dbmigration.DbMigration;
import com.linuxbox.util.dbmigration.DbMigrationException;
import com.linuxbox.util.dbmigration.DbMigrator;
import com.linuxbox.util.mongodb.UpdateFieldBuilder;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Migrates the MessageStore data from DB version 1 to 2.  This adds a monotonic
 * ID field to every message.
 */
public class MessageStoreMigration1to2 extends DbMigration {

	public static class Message1to2Constants {
		public static String MESSAGE_ID = "_id";
		public static String MONOTONIC_ID = "monotonic_id";
	}

	public class MessageDoc1to2 extends AbstractDocumentMigrator {
		private DBObject object;

		public MessageDoc1to2(DBObject object, DbInfo dbInfo) {
			super(((MongoDbInfo)dbInfo).getCollection());
			this.object = object;
		}

		public void migrateDocumentImpl() throws DbMigrationException {
			UpdateFieldBuilder builder = new UpdateFieldBuilder().set(
					Message1to2Constants.MONOTONIC_ID, new ObjectId());
			collection.update(object, builder.get());
		}
	}

	public class MessageStoreColl1to2 extends AbstractColletionMigrator {
		private DbInfo dbInfo;

		public MessageStoreColl1to2(DbInfo dbInfo) {
			super(((MongoDbInfo)dbInfo).getCollection());

			this.dbInfo = dbInfo;
			setDeadFields(null);
			setRenamedFields(null);
		}

		/**
		 * Migrate the MessageStore collection.  Just walk the messages migrating them.
		 */
		@Override
		public void migrateCollectionImpl() throws DbMigrationException {
			DBCursor cursor = collection.find();

			while (cursor.hasNext()) {
				MessageDoc1to2 message = new MessageDoc1to2(cursor.next(), dbInfo);
				message.migrateDocument();
			}
		}

	}

	public MessageStoreMigration1to2(DbMigrator migrator) throws DbMigrationException {
		super(migrator, 1, 2);
	}

	@Override
	public void migrate(DbInfo dbInfo) throws DbMigrationException {
		LOGGER.info("Running MessageStore migration 1 to 2");

		MessageStoreColl1to2 msMigrator = new MessageStoreColl1to2(dbInfo);
		try {
			msMigrator.migrateCollection();
		} catch (Exception e) {
			throw new DbMigrationException("Failed to migrate MessageStore from 1 to 2: ", e);
		}
	}

	public static DBCollection getCollection(DbInfo dbInfo, String serviceName) {
		MultiDbInfo multiDbInfo = (MultiDbInfo) dbInfo;
		MongoDbInfo mongoDbInfo = (MongoDbInfo)multiDbInfo.getByServiceName(serviceName);
		return mongoDbInfo.getCollection();
	}


}
