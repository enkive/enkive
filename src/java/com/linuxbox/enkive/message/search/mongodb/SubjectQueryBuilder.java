package com.linuxbox.enkive.message.search.mongodb;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.SUBJECT;
import static com.linuxbox.enkive.search.Constants.SUBJECT_PARAMETER;

import java.util.Map;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class SubjectQueryBuilder extends AbstractMongoMessageQueryBuilder {

	@Override
	public DBObject buildQueryPortion(Map<String, String> fields) {
		String subjectStr = fields.get(SUBJECT_PARAMETER);
		if (null == subjectStr || subjectStr.isEmpty()) {
			return null;
		}

		subjectStr = subjectStr.trim();

		Pattern subjectRegEx = Pattern.compile(subjectStr,
				Pattern.CASE_INSENSITIVE);

		BasicDBObject result = new BasicDBObject();
		result.put(SUBJECT, subjectRegEx);
		return result;
	}
}
