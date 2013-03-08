package com.linuxbox.enkive.testing;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.QueryBuilder;

public class AttachmentVerifier {
	protected final static int ACCESS_INCREMENT = 60;

	protected final static String PROP_MONGODB_HOST = "enkive.mongodb.host";
	protected final static String PROP_MONGODB_PORT = "enkive.mongodb.port";
	protected final static String PROP_DOCSTORE_DB_NAME = "enkive.docstore.db.name";
	protected final static String PROP_DOCSTORE_FS_COLLECTION_NAME = "enkive.docstore.fs.collection.name";
	protected final static String PROP_MESSAGE_COLLECTION_NAME = "enkive.messagestore.collection.name";
	protected final static String FIELD_FILENAME = "filename";
	protected final static String FIELD_ENCODING = "binaryEncoding";
	protected final static String FIELD_ATTACH_MIME_TYPE = "contentType";
	protected final static String FIELD_HEADER_MIME_TYPE = "content_type";
	protected final static String FIELD_KEY = "_id";
	protected final static String FIELD_CONTENT_HEADERS = "content_header";
	protected final static String FIELD_PART_HEADERS = "partHeaders";
	protected final static String FIELD_TYPE = "type";
	protected final static String VALUE_MULTI = "multiPartHeader";
	protected final static String VALUE_SINGLE = "singlePartHeader";
	protected final static String FIELD_ATTACH_ID = "attachment_id";

	protected static boolean initializationErrors = false;

	static Properties loadConfigProperties() throws IOException {
		Properties properties = new Properties();
		properties
				.load(new FileInputStream("config/default/enkive.properties"));
		properties.load(new FileInputStream("config/enkive.properties"));
		return properties;
	}

	static String getProperty(Properties props, String name) {
		return getProperty(props, name, null);
	}

	static String getProperty(Properties props, String name, String defaultValue) {
		final String value = props.getProperty(name, defaultValue);
		if (null == value) {
			System.err.println("Error: Could not find value for property \""
					+ name + "\".");
			initializationErrors = true;
		}
		return value;
	}

	static void processHeader(DBObject header) {
		String type = (String) header.get(FIELD_TYPE);
		if (type.equals(VALUE_SINGLE)) {
			String attachmentId = (String) header.get(FIELD_ATTACH_ID);
			String contentType = (String) header.get(FIELD_HEADER_MIME_TYPE);
			if (!contentType.equals("text/plain")) {
				System.out.print(attachmentId + " : " + contentType);
				String fileName = (String) header.get(FIELD_FILENAME);
				if (fileName != null) {
					System.out.println(" : " + fileName);
				} else {
					System.out.println();
				}
			}
		} else if (type.equals(VALUE_MULTI)) {
			BasicDBList parts = (BasicDBList) header.get(FIELD_PART_HEADERS);
			for (Object partObj : parts) {
				DBObject part = (DBObject) partObj;
				processHeader(part);
			}
		} else {
			System.out.println("Unknown type : " + type);
		}
	}

	static void processMessages(DBCollection c) {
		DBObject search = new BasicDBObject();
		DBObject sortFields = new BasicDBObject().append(FIELD_KEY, 1);
		DBObject extractFields = new BasicDBObject().append(
				FIELD_CONTENT_HEADERS, 1);
		int counter = 0;

		while (true) {
			DBCursor cursor = c.find(search, extractFields).sort(sortFields)
					.limit(ACCESS_INCREMENT);
			if (cursor.count() == 0) {
				break;
			}
			String id = null;
			for (DBObject o : cursor) {
				++counter;
				id = (String) o.get(FIELD_KEY);
				// System.out.println("-- " + counter + " : " + id + " --");
				DBObject header = (DBObject) o.get(FIELD_CONTENT_HEADERS);
				processHeader(header);
			}
			cursor.close();

			search = QueryBuilder.start(FIELD_KEY).greaterThan(id).get();
		}
	}

	static void processFsCollection(DBCollection c) {
		DBObject search = new BasicDBObject();
		DBObject sortFields = new BasicDBObject().append(FIELD_FILENAME, 1);
		DBObject extractFields = new BasicDBObject().append(FIELD_FILENAME, 1)
				.append(FIELD_ENCODING, 1).append(FIELD_ATTACH_MIME_TYPE, 1);
		int counter = 0;

		while (true) {
			DBCursor cursor = c.find(search, extractFields).sort(sortFields)
					.limit(ACCESS_INCREMENT);
			if (cursor.count() == 0) {
				break;
			}
			String lastFileName = "";
			for (DBObject o : cursor) {
				++counter;
				// ObjectId i = (ObjectId) o.get(FIELD_KEY);
				final String fileName = (String) o.get(FIELD_FILENAME);
				final String encoding = (String) o.get(FIELD_ENCODING);
				final String mimeType = (String) o.get(FIELD_ATTACH_MIME_TYPE);
				lastFileName = fileName;

				if (!mimeType.equals("text/plain")) {
					System.out.println(counter + ": " + fileName + " / "
							+ mimeType + " / " + encoding);
				}
			}
			cursor.close();

			search = QueryBuilder.start("filename").greaterThan(lastFileName)
					.get();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Properties props = loadConfigProperties();
			String mongoDbHost = getProperty(props, PROP_MONGODB_HOST);
			String mongoDbPortString = getProperty(props, PROP_MONGODB_PORT);
			String dbName = getProperty(props, PROP_DOCSTORE_DB_NAME);
			String fsCollectionName = getProperty(props,
					PROP_DOCSTORE_FS_COLLECTION_NAME);
			String emailCollectionName = getProperty(props,
					PROP_MESSAGE_COLLECTION_NAME);

			int mongoDbPort = 27017;

			try {
				mongoDbPort = Integer.parseInt(mongoDbPortString);
			} catch (NumberFormatException e) {
				System.err.println("Error: Port number \"" + mongoDbPortString
						+ "\" could not be understood as a number.");
				initializationErrors = true;
			}

			if (initializationErrors) {
				System.err
						.println("Quitting as could not procede due to errors.");
				System.exit(1);
			}

			Mongo mongo = new Mongo(mongoDbHost, mongoDbPort);
			DB enkiveDb = mongo.getDB(dbName);
			enkiveDb.setReadOnly(true);
			DBCollection fsCollection = enkiveDb.getCollection(fsCollectionName
					+ ".files");
			DBCollection messageCollection = enkiveDb
					.getCollection(emailCollectionName);

			// processFsCollection(fsCollection);
			processMessages(messageCollection);

			mongo.close();
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace(System.err);
		}
	}
}
