package com.linuxbox.enkive.message.search.mongodb;

import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.MONOTONIC_ID;
import static com.linuxbox.enkive.search.Constants.INITIAL_MESSAGE_UUID_PARAMETER;

import java.util.Map;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class InitialMessageUUIDQueryBuilder extends AbstractMongoMessageQueryBuilder {

	@Override
	public DBObject buildQueryPortion(Map<String, String> fields) {
		String messageIdStr = fields.get(INITIAL_MESSAGE_UUID_PARAMETER);
		if (null == messageIdStr || messageIdStr.isEmpty()) {
			return null;
		}

		messageIdStr = messageIdStr.trim();

		BasicDBObject result = new BasicDBObject(MONOTONIC_ID, new
				BasicDBObject("$gt", ObjectId.massageToObjectId(messageIdStr)));
		return result;
	}
}