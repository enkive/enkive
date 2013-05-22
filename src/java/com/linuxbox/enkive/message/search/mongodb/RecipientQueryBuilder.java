package com.linuxbox.enkive.message.search.mongodb;

import java.util.Map;
import java.util.regex.Pattern;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class RecipientQueryBuilder extends AbstractMongoMessageQueryBuilder {
	final protected String key;
	final protected String[] queries;

	public RecipientQueryBuilder(String fieldKey, String[] queries) {
		this.key = fieldKey;
		this.queries = queries;
	}

	@Override
	public DBObject buildQueryPortion(Map<String, String> fields)
			throws EmptySearchResultsException, MessageSearchException {
		final String addressListStr = fields.get(key);
		if (null == addressListStr) {
			return null;
		}

		String[] addresses = addressListStr.trim().split(";");

		BasicDBList queryList = new BasicDBList();

		for (String address : addresses) {
			address = address.trim();
			if (!address.isEmpty()) {
				Pattern addressRegex = Pattern.compile(address,
						Pattern.CASE_INSENSITIVE);
				for (String query : queries) {
					queryList.add(new BasicDBObject(query, addressRegex));
				} // for
			} // if
		} // for

		if (queryList.isEmpty()) {
			return null;
		} else {
			BasicDBObject result = new BasicDBObject();
			result.put("$or", queryList);
			return result;
		}
	}
}
