package com.linuxbox.enkive.audit.mongodb;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.linuxbox.enkive.audit.AuditEntry;
import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.AuditTrailException;
import com.linuxbox.util.queueservice.QueueServiceException;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

public class MongoAuditService implements AuditService {
	protected static final Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.audit.mongodb");

	private static final String TIMESTAMP_FIELD = "tstamp";
	private static final String CODE_FIELD = "code";
	private static final String USERNAME_FIELD = "user";
	private static final String DESCRIPTION_FIELD = "desc";

	protected static final String INSERT_STATEMENT = "INSERT INTO events (timestamp,code,username,description) VALUES (?,?,?,?);";
	protected static final String BY_ID_STATEMENT = "SELECT id,timestamp,code,username,description FROM events WHERE id=?;";
	protected static final String SEARCH_STATEMENT = "SELECT id,timestamp,code,username,description FROM events WHERE id=?;";
	protected static final String MOST_RECENT_STATEMENT = "SELECT id,timestamp,code,username,description FROM events ORDER BY timestamp DESC LIMIT ? OFFSET ?";
	protected static final String COUNT_STATEMENT = "SELECT COUNT(id) FROM events";

	private Mongo mongo;
	private DB mongoDB;
	private DBCollection queueCollection;

	/**
	 * Keep track of whether we created the instance of Mongo, as if so, we'll
	 * have to close it.
	 */
	private boolean createdMongo;

	public MongoAuditService(String server, int port, String database,
			String collection) throws UnknownHostException, MongoException {
		this(new Mongo(server, port), database, collection);
		createdMongo = true;
	}

	public MongoAuditService(String database, String collection)
			throws UnknownHostException, MongoException {
		this(new Mongo(), database, collection);
		createdMongo = true;
	}

	public MongoAuditService(Mongo mongo, String database, String collection) {
		this.mongo = mongo;
		this.mongoDB = mongo.getDB(database);
		this.queueCollection = mongoDB.getCollection(collection);

		// For MongoDB multi-key indexes to work the most efficiently, the
		// queried fields should appear before the sorting fields in the
		// indexes.

		final DBObject whenIndex = new BasicDBObject(TIMESTAMP_FIELD, 1);
		queueCollection.ensureIndex(whenIndex, "timestampIndex");

		final DBObject whatWhenIndex = new BasicDBObject(CODE_FIELD, 1).append(
				TIMESTAMP_FIELD, 1);
		queueCollection.ensureIndex(whatWhenIndex, "codeTimestampIndex");

		final DBObject whoWhenIndex = new BasicDBObject(USERNAME_FIELD, 1)
				.append(TIMESTAMP_FIELD, 1);
		queueCollection.ensureIndex(whoWhenIndex, "userTimestampIndex");

		// TODO: do we (will we) need a who, what, when index, so we can select
		// by who/what and sort by when?

		// Make sure data is written out to disk before operation is complete.
		queueCollection.setWriteConcern(WriteConcern.NORMAL);
	}

	public void startup() throws QueueServiceException {
		// empty
	}

	public void shutdown() throws QueueServiceException {
		if (createdMongo) {
			mongo.close();
		}
	}

	@Override
	public void addEvent(int eventCode, String userIdentifier,
			String description) throws AuditTrailException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addEvent(int eventCode, String userIdentifier,
			String description, boolean truncateDescription)
			throws AuditTrailException {
		// TODO Auto-generated method stub

	}

	@Override
	public AuditEntry getEvent(String identifer) throws AuditTrailException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AuditEntry> search(Integer eventCode, String userIdentifer,
			Date startDate, Date endDate) throws AuditTrailException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AuditEntry> getMostRecentByPage(int perPage, int pagesToSkip)
			throws AuditTrailException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAuditEntryCount() throws AuditTrailException {
		// TODO Auto-generated method stub
		return 0;
	}
}
