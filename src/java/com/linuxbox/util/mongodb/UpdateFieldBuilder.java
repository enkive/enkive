package com.linuxbox.util.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

public class UpdateFieldBuilder {
	BasicDBObjectBuilder setBuilder = new BasicDBObjectBuilder();
	BasicDBObjectBuilder unsetBuilder = new BasicDBObjectBuilder();
	BasicDBObjectBuilder incBuilder = new BasicDBObjectBuilder();
	BasicDBObjectBuilder renameBuilder = new BasicDBObjectBuilder();

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

	public UpdateFieldBuilder inc(String key, int value) {
		setBuilder.add(key, value);
		return this;
	}

	public UpdateFieldBuilder rename(String oldName, String newName) {
		setBuilder.add(oldName, newName);
		return this;
	}

	public DBObject get() {
		BasicDBObject result = new BasicDBObject();

		if (!incBuilder.isEmpty()) {
			result.append("$inc", incBuilder.get());
		}
		if (!setBuilder.isEmpty()) {
			result.append("$set", setBuilder.get());
		}
		if (!unsetBuilder.isEmpty()) {
			result.append("$unset", unsetBuilder.get());
		}
		if (!renameBuilder.isEmpty()) {
			result.append("$rename", renameBuilder.get());
		}

		return result;
	}
}