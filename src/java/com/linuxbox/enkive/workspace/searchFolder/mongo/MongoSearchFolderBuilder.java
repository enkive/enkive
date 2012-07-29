package com.linuxbox.enkive.workspace.searchFolder.mongo;

import com.linuxbox.enkive.workspace.searchFolder.SearchFolder;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

public class MongoSearchFolderBuilder implements SearchFolderBuilder {
	
	Mongo m;
	DB searchFolderDB;
	DBCollection searchFolderColl;

	public MongoSearchFolderBuilder(Mongo m, String searchFolderDBName, String searchFolderCollName){
		this.m = m;
		this.searchFolderDB = m.getDB(searchFolderDBName);
		this.searchFolderColl = searchFolderDB.getCollection(searchFolderCollName);
	}
	
	@Override
	public SearchFolder getSearchFolder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchFolder getSearchFolder(String searchFolderId) {
		// TODO Auto-generated method stub
		return null;
	}

}
