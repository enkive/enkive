package com.linuxbox.enkive.message.search.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.MESSAGE_ID;
import static com.linuxbox.enkive.search.Constants.MESSAGE_ID_PARAMETER;

import java.util.Map;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MessageIdQueryBuilder extends AbstractMongoMessageQueryBuilder {

	@Override
	public DBObject buildQueryPortion(Map<String, String> fields) {
		String messageIdStr = fields.get(MESSAGE_ID_PARAMETER);
		if (null == messageIdStr || messageIdStr.isEmpty()) {
			return null;
		}

		messageIdStr = messageIdStr.trim();

		// TODO: Do we really want this to be a regular expression search? Would
		// the end-user be doing a search on a portion of the message ID?
		Pattern messageIdRegEx = Pattern.compile(messageIdStr,
				Pattern.CASE_INSENSITIVE);

		BasicDBObject result = new BasicDBObject();
		result.put(MESSAGE_ID, messageIdRegEx);
		return result;
	}
}