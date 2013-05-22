package com.linuxbox.enkive.message.search.mongodb;

import java.util.Map;

import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.mongodb.DBObject;

public interface MongoMessageQueryBuilder {
	DBObject buildQueryPortion(Map<String, String> fields)
			throws EmptySearchResultsException, MessageSearchException;
}
