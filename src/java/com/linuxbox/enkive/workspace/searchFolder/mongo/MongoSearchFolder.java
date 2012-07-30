package com.linuxbox.enkive.workspace.searchFolder.mongo;

import java.util.Collection;
import java.util.HashSet;

import com.linuxbox.enkive.workspace.searchFolder.SearchFolder;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResult;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResultBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoSearchFolder extends SearchFolder {

	Mongo m;
	DB searchFolderDB;
	DBCollection searchFolderColl;

	public MongoSearchFolder(Mongo m, String searchFolderDBName,
			String searchFolderCollName,
			SearchFolderSearchResultBuilder searchResultBuilder) {
		super(searchResultBuilder);
		this.m = m;
		this.searchFolderDB = m.getDB(searchFolderDBName);
		this.searchFolderColl = searchFolderDB
				.getCollection(searchFolderCollName);
	}

	public void saveSearchFolder() {
		BasicDBObject searchFolderObject = new BasicDBObject();
		Collection<String> searchResultIds = new HashSet<String>();
		for (SearchFolderSearchResult result : results){
			System.out.println(result.getId());
			searchResultIds.add(result.getId());
		}
		searchFolderObject.put("searchResultIds", searchResultIds);
		searchFolderColl.insert(searchFolderObject);
	}

}
