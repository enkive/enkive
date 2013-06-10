package com.linuxbox.util.dbinfo.mongodb;

import static com.linuxbox.util.mongodb.MongoDbConstants.GRID_FS_FILES_COLLECTION_SUFFIX;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;

public class MongoGridDbInfo extends MongoDbInfo {
	final protected GridFS gridFs;

	public MongoGridDbInfo(String serviceName, MongoClient mongo, String dbName,
			String bucketName) {
		this(serviceName, mongo, mongo.getDB(dbName), bucketName);
	}

	public MongoGridDbInfo(String serviceName, MongoClient mongo, DB db,
			String bucketName) {
		super(serviceName, mongo, db, bucketName
				+ GRID_FS_FILES_COLLECTION_SUFFIX);
		this.gridFs = new GridFS(db, bucketName);
	}

	public GridFS getGridFs() {
		return gridFs;
	}
}
