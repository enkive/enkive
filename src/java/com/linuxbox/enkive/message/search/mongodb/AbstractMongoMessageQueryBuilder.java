package com.linuxbox.enkive.message.search.mongodb;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractMongoMessageQueryBuilder implements
		MongoMessageQueryBuilder {
	protected final static Log LOGGER = LogFactory
			.getLog("com.linuxbox.enkive.search.mongodb");
}
