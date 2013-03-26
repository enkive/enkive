package com.linuxbox.util.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class UpdateFieldBuilder {
	BasicDBObjectBuilder setBuilder = new BasicDBObjectBuilder();
	BasicDBObjectBuilder unsetBuilder = new BasicDBObjectBuilder();

	public UpdateFieldBuilder() {
		// empty
	}

	public UpdateFieldBuilder set(String key, Object value) {
		setBuilder.add(key, value);
		return this;
	}

	public UpdateFieldBuilder unset(String key) {
		unsetBuilder.add(key, "");
		return this;
	}

	public DBObject get() {
		BasicDBObject result = new BasicDBObject();

		if (!unsetBuilder.isEmpty()) {
			result.append("$unset", unsetBuilder.get());
		}

		if (!setBuilder.isEmpty()) {
			result.append("$set", setBuilder.get());
		}

		return result;
	}
}