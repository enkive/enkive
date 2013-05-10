/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
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
package com.linuxbox.enkive.workspace.searchFolder.mongo;

import java.util.Collection;
import java.util.HashSet;

import org.bson.types.ObjectId;

import com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolder;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResult;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResultBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;

public class MongoSearchFolder extends SearchFolder {
	DBCollection searchFolderColl;

	public MongoSearchFolder(DBCollection searchFolderColl,
			SearchFolderSearchResultBuilder searchResultBuilder) {
		super(searchResultBuilder);
		this.searchFolderColl = searchFolderColl;
	}

	public void saveSearchFolder() {
		BasicDBObject searchFolderObject = new BasicDBObject();
		Collection<String> searchResultIds = new HashSet<String>();
		for (SearchFolderSearchResult result : results) {
			searchResultIds.add(result.getId());
		}
		searchFolderObject.put(MongoWorkspaceConstants.SEARCHRESULTSLIST,
				searchResultIds);
		if (getID() != null && !getID().isEmpty()) {
			searchFolderObject.put("_id", ObjectId.massageToObjectId(getID()));
			BasicDBObject searchFolderQueryObject = new BasicDBObject();
			searchFolderQueryObject.put("_id",
					ObjectId.massageToObjectId(getID()));
			searchFolderColl.findAndModify(searchFolderQueryObject,
					searchFolderObject);
		} else {
			searchFolderColl.insert(searchFolderObject);
			setID(searchFolderObject.getString(MongoWorkspaceConstants.UUID));
		}
	}

}
