package com.linuxbox.util.mongodb;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class Dropper {
	public static void dropDatabase(String dbName) throws UnknownHostException,
			MongoException {
		Mongo m = new Mongo();
		DB db = m.getDB(dbName);
		db.dropDatabase();
	}

	public static void dropCollection(String dbName, String collection)
			throws UnknownHostException, MongoException {
		Mongo m = new Mongo();
		DB db = m.getDB(dbName);
		DBCollection c = db.getCollection(collection);
		c.drop();
	}
}
