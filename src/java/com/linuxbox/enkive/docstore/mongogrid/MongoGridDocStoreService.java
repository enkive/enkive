package com.linuxbox.enkive.docstore.mongogrid;

import java.net.UnknownHostException;

import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.EncodedDocument;
import com.linuxbox.enkive.docstore.exceptions.DocumentNotFoundException;
import com.linuxbox.enkive.docstore.exceptions.NoEncodingAvailableException;
import com.linuxbox.enkive.docstore.exceptions.StorageException;
import com.linuxbox.enkive.docstore.exceptions.UnimplementedMethodException;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;

public class MongoGridDocStoreService implements DocStoreService {
	private Mongo mongo;
	private DB db;
	private GridFS gridFS;

	public MongoGridDocStoreService(String host, int port, String dbName, String bucketName)
			throws UnknownHostException, MongoException {
		mongo = new Mongo(host, port);
		db = mongo.getDB(dbName);
		gridFS = new GridFS(db, bucketName);
	}

	@Override
	public Document retrieve(String identifier)
			throws DocumentNotFoundException {
		// TODO Auto-generated method stub
		throw new UnimplementedMethodException();
	}

	@Override
	public EncodedDocument retrieveEncoded(String identifier)
			throws NoEncodingAvailableException {
		// TODO Auto-generated method stub
		throw new UnimplementedMethodException();
	}

	@Override
	public String store(Document document) throws StorageException {
		// TODO Auto-generated method stub
		throw new UnimplementedMethodException();
	}
}
