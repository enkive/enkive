package com.linuxbox.enkive.workspace.searchFolder.mongo;

import java.util.Iterator;

import org.bson.types.ObjectId;

import com.linuxbox.enkive.retriever.MessageRetrieverService;
import com.linuxbox.enkive.workspace.WorkspaceException;
import com.linuxbox.enkive.workspace.mongo.MongoWorkspaceConstants;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolder;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderBuilder;
import com.linuxbox.enkive.workspace.searchFolder.SearchFolderSearchResultBuilder;
import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoSearchFolderBuilder implements SearchFolderBuilder {

	protected Mongo m;
	protected DB searchFolderDB;
	protected DBCollection searchFolderColl;
	protected SearchFolderSearchResultBuilder searchResultBuilder;
	protected MessageRetrieverService retrieverService;

	public MongoSearchFolderBuilder(Mongo m, String searchFolderDBName,
			String searchFolderCollName,
			SearchFolderSearchResultBuilder searchResultBuilder) {
		this.m = m;
		this.searchFolderDB = m.getDB(searchFolderDBName);
		this.searchFolderColl = searchFolderDB
				.getCollection(searchFolderCollName);
		this.searchResultBuilder = searchResultBuilder;
	}

	@Override
	public SearchFolder getSearchFolder() {
		SearchFolder searchFolder = new MongoSearchFolder(m,
				searchFolderDB.getName(), searchFolderColl.getName(),
				searchResultBuilder);
		searchFolder.setRetrieverService(retrieverService);
		searchFolder.saveSearchFolder();
		return searchFolder;
	}

	@Override
	public SearchFolder getSearchFolder(String searchFolderId)
			throws WorkspaceException {
		SearchFolder searchFolder = new MongoSearchFolder(m,
				searchFolderDB.getName(), searchFolderColl.getName(),
				searchResultBuilder);
		searchFolder.setRetrieverService(retrieverService);
		searchFolder.setID(searchFolderId);

		DBObject folderObject = searchFolderColl.findOne(ObjectId
				.massageToObjectId(searchFolderId));

		BasicDBList searchResults = (BasicDBList) folderObject
				.get(MongoWorkspaceConstants.SEARCHRESULTSLIST);

		Iterator<Object> searchResultsIterator = searchResults.iterator();
		while (searchResultsIterator.hasNext())
			searchFolder.addSearchResult(searchResultBuilder
					.getSearchResult((String) searchResultsIterator.next()));

		return searchFolder;
	}

	public MessageRetrieverService getRetrieverService() {
		return retrieverService;
	}

	public void setRetrieverService(MessageRetrieverService retrieverService) {
		this.retrieverService = retrieverService;
	}

}
