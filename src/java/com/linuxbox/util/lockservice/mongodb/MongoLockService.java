/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
 ******************************************************************************/
package com.linuxbox.util.lockservice.mongodb;

import java.net.UnknownHostException;
import java.util.Date;

import com.linuxbox.util.lockservice.LockAcquisitionException;
import com.linuxbox.util.lockservice.LockReleaseException;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;

public class MongoLockService extends AbstractRetryingLockService {
	public static class LockRequestFailure {
		public String identifier;
		public Date holderTimestamp;
		public String holderNote;

		public LockRequestFailure(String identifier, Date holderTimestamp,
				String holderNote) {
			this.identifier = identifier;
			this.holderTimestamp = holderTimestamp;
			this.holderNote = holderNote;
		}
	}

	/**
	 * If we try to acquire the lock and fail, we'll return information about
	 * the existing lock. But that requires a separate query. What if that query
	 * says there is no existing lock? The implication is that the lock was
	 * released right after our attempt to create it. So try again to acquire
	 * that lock. If we fail after this number of times, assume it's hopless and
	 * throw an exception.
	 */
	private static int LOCK_RETRY_ATTEMPTS = 4;

	public static int MONGO_DUPLICATE_KEY_ERROR_CODE = 11000;
	private static String LOCK_IDENTIFIER_KEY = "identifier";
	private static String LOCK_NOTE_KEY = "note";
	private static String LOCK_TIMESTAMP_KEY = "timestamp";

	private Mongo mongo;
	private DB mongoDB;
	private DBCollection lockCollection;
	private boolean mongoCreated;

	public MongoLockService(String server, int port, String database,
			String collection) throws UnknownHostException, MongoException {
		this(new Mongo(server, port), database, collection);
		mongoCreated = true;
	}

	public MongoLockService(String database, String collection)
			throws UnknownHostException, MongoException {
		this(new Mongo(), database, collection);
		mongoCreated = true;
	}

	public MongoLockService(Mongo mongo, String database, String collection) {
		this.mongo = mongo;
		mongoDB = mongo.getDB(database);
		lockCollection = mongoDB.getCollection(collection);

		lockCollection.setWriteConcern(WriteConcern.FSYNC_SAFE);

		// We want the identifier index to be unique, as that's how we
		// atomically detect when someone tries to create an already-existing
		// lock record
		final boolean mustBeUnique = true;
		DBObject lockIndex = BasicDBObjectBuilder.start()
				.add(LOCK_IDENTIFIER_KEY, 1).get();
		lockCollection.ensureIndex(lockIndex, "lockIndex", mustBeUnique);
	}

	public void startup() {
		lockCollection.remove(new BasicDBObject());
	}

	public void shutdown() {
		if (mongoCreated) {
			mongo.close();
		}
	}

	/**
	 * Attempts to create the specified lock. If it fails it returns a record
	 * describing the existing lock.
	 * 
	 * @param identifier
	 * @param notation
	 * @return
	 * @throws LockAcquisitionException
	 */
	public LockRequestFailure lockWithFailureData(String identifier,
			String notation) throws LockAcquisitionException {
		final DBObject query = QueryBuilder.start(LOCK_IDENTIFIER_KEY)
				.is(identifier).get();

		for (int i = 0; i < LOCK_RETRY_ATTEMPTS; i++) {
			if (lock(identifier, notation)) {
				return null;
			} else {
				final DBObject existingLockRecord = lockCollection
						.findOne(query);
				if (existingLockRecord != null) {
					return new LockRequestFailure(identifier,
							(Date) existingLockRecord.get(LOCK_TIMESTAMP_KEY),
							(String) existingLockRecord.get(LOCK_NOTE_KEY));
				}

				// if we could not find the record, we'll loop back up and
				// re-attempt
			}
		}

		throw new LockAcquisitionException(identifier, "failed after "
				+ LOCK_RETRY_ATTEMPTS + " attempts");
	}

	/**
	 * Request sole access to a lock. Returns true if sole access is granted,
	 * false otherwise.
	 * 
	 * @param identifier
	 * @return
	 */
	@Override
	public boolean lock(String identifier, Object notation)
			throws LockAcquisitionException {
		try {
			final DBObject controlRecord = BasicDBObjectBuilder
					.start(LOCK_IDENTIFIER_KEY, identifier)
					.add(LOCK_TIMESTAMP_KEY, new Date())
					.add(LOCK_NOTE_KEY, notation).get();
			lockCollection.insert(controlRecord);
			return true;
		} catch (MongoException e) {
			if (e.getCode() == MONGO_DUPLICATE_KEY_ERROR_CODE) {
				// because the index for identifier is unique, trying to create
				// another record for the same file will generate an exception
				// that we catch here
				return false;
			} else {
				throw new LockAcquisitionException(identifier, e);
			}
		}
	}

	/**
	 * Releases control of the identifier by removing the record. If the record
	 * does not exist then throw a ControlReleaseException.
	 * 
	 * @param identifier
	 * @throws LockReleaseException
	 */
	public void releaseLock(String identifier) throws LockReleaseException {
		final DBObject identifierQuery = new QueryBuilder()
				.and(LOCK_IDENTIFIER_KEY).is(identifier).get();
		final DBObject lockRecord = lockCollection
				.findAndRemove(identifierQuery);
		if (lockRecord == null) {
			throw new LockReleaseException(identifier);
		}
	}
}
