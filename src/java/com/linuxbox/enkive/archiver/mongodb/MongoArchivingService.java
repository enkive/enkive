package com.linuxbox.enkive.archiver.mongodb;

import com.linuxbox.enkive.archiver.AbstractMessageArchivingService;
import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.message.ContentHeader;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MultiPartHeader;
import com.linuxbox.enkive.message.SinglePartHeader;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBRef;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;

public class MongoArchivingService extends AbstractMessageArchivingService {

	protected Mongo m = null;
	protected DB messageDb;
	protected DBCollection messageColl;
	
	public MongoArchivingService(Mongo m, String dbName, String collName) {
		this.m = m;
		try {
			messageDb = m.getDB(dbName);
			messageColl = messageDb.getCollection(collName);
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String storeMessage(Message message) throws CannotArchiveException {
		String messageUUID = null;
		try {
			BasicDBObject messageObject = new BasicDBObject();
			messageObject.put("_id", message.getMessageId());
			messageObject.put("headers", message.getOriginalHeaders());
			
			ContentHeader contentHeader = message.getContentHeader();
			
			if(contentHeader.isMultipart()){
				MultiPartHeader multipartHeader = (MultiPartHeader)contentHeader;
				for(ContentHeader partHeader : multipartHeader.getPartHeaders()){
					BasicDBObject partHeaderObject = new BasicDBObject();
					partHeaderObject.put("headers", partHeader.getOriginalHeaders());
					//Document Storage is done here
					messageObject.put("singlePartHeader", partHeaderObject);				}
			} else {
				SinglePartHeader partHeader = (SinglePartHeader)contentHeader;
				BasicDBObject partHeaderObject = new BasicDBObject();
				partHeaderObject.put("headers", partHeader.getOriginalHeaders());
				//Document Storage is done here
				messageObject.put("singlePartHeader", partHeaderObject);
			}
			
			messageColl.insert(messageObject);
			messageUUID = messageObject.getString("_id");
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return messageUUID;
	}

	@Override
	public String findMessage(Message message) {
		BasicDBObject searchObject = new BasicDBObject();
		searchObject.put("_id", message.getMessageId());
		if(messageColl.findOne(searchObject) != null)
			return message.getMessageId();
		return null;
	}

}
