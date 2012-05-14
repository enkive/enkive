/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
 ******************************************************************************/

package com.linuxbox.util.mongodb;

import java.util.List;

import com.mongodb.DBObject;
import com.mongodb.MongoException;

/**
 * This interface can be applied to a service with a MongoDB back-end, allowing
 * it to describe the indexes it has in place. This would allow a system
 * administration tool to see if the right indexes are in place.
 * 
 * @author eric
 * 
 */
public interface MongoIndexable {
	public static class IndexDescription {
		String name;
		DBObject description;
		boolean isUnique;

		public IndexDescription(String name, DBObject description,
				boolean isUnique) {
			this.name = name;
			this.description = description;
			this.isUnique = isUnique;
		}

		public String getName() {
			return name;
		}

		public DBObject getDescription() {
			return description;
		}

		public boolean isUnique() {
			return isUnique;
		}
	}

	public List<DBObject> getIndexInfo();

	public List<IndexDescription> getPreferredIndexes();

	public void ensureIndex(DBObject index, DBObject options)
			throws MongoException;

	public long getDocumentCount() throws MongoException;
}
