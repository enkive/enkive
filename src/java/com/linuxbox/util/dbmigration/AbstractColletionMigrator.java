/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 *
 * This file is part of Enkive CE (Community Edition).
 *
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.linuxbox.util.dbmigration;

import java.util.Collection;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

/**
 * Abstract base class for migrating a MongoDB collection  It provides 2 main
 * services, before calling the implementation: removing and renaming fields.
 * First, a list of dead fields is removed from every document in the collection.
 * Next, a map of <old-name, new-name> pairs is run, where each old-name is
 * renamed to it's new-name.  Finally, the child class's implementation is
 * called to do complex work.
 * @author dang
 *
 */
public abstract class AbstractColletionMigrator {
	protected DBCollection collection;
	protected Collection<String> deadFields;
	protected Map<String, String> renamedFields;

	public AbstractColletionMigrator(DBCollection collection) {
		this.collection = collection;
	}

	public void setDeadFields(Collection<String> deadFields) {
		this.deadFields = deadFields;
	}
	public Collection<String> getDeadFields() {
		return this.deadFields;
	}

	/**
	 * Set the map of fields to rename.  For each entry, the field represented by
	 * key() is renamed to value().
	 * @param renamedFields
	 */
	public void setRenamedFields(Map<String, String> renamedFields) {
		this.renamedFields = renamedFields;
	}
	public Map<String, String> getRenamedFields() {
		return this.renamedFields;
	}

	public void migrateCollection() throws DbMigrationException {
		migrateFields();
		migrateCollectionImpl();
	}

	/**
	 * Class specific migration code
	 * @throws DbMigrationException
	 */
	public abstract void migrateCollectionImpl() throws DbMigrationException;

	private void migrateFields() throws DbMigrationException {
		// Remove all dead fields
		if (deadFields != null) {
			for (String deadField: deadFields) {
				collection.update(new BasicDBObject(), new BasicDBObject("$unset",
						new BasicDBObject(deadField, 1)), false, true);
			}
		}

		// Rename changed fields
		if (renamedFields != null) {
			BasicDBObject rename = new BasicDBObject();
			for (Map.Entry<String, String> entry: renamedFields.entrySet()) {
				rename.append(entry.getKey(), entry.getValue());
			}
			collection.update(new BasicDBObject(), new BasicDBObject("$rename", rename), true, true);
		}
	}
}
