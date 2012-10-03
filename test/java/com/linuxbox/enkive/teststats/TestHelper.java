/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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
package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.statistics.gathering.Gatherer;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.StatsMsgGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.MongoGathererMessageSearchService;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.services.StatsClient;
import com.linuxbox.enkive.statistics.services.StatsGathererService;
import com.linuxbox.enkive.statistics.services.retrieval.mongodb.MongoStatsRetrievalService;
import com.linuxbox.enkive.statistics.services.storage.mongodb.MongoStatsStorageService;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

public class TestHelper {
	private static Mongo m;
	private static DB db;
	private static DBCollection coll;
	private static final String dbPropName = "dbGatherer";
	private static final String collPropName = "collGatherer";
	private static final String runPropName = "rtGatherer";
	private static final String msgSearchPropName = "msgGatherer";
	private static final String attPropName = "attGatherer";

	public static DBCollection GetTestCollection() {
		if (m == null) {
			try {
				m = new Mongo();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (MongoException e) {
				e.printStackTrace();
			}
		}
		db = m.getDB(TestingConstants.MONGODB_TEST_DATABASE);
		return db.getCollection(STAT_STORAGE_COLLECTION);
	}

	public static MongoStatsRetrievalService BuildRetrievalService() {
		if (m == null) {
			try {
				m = new Mongo();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (MongoException e) {
				e.printStackTrace();
			}
			db = m.getDB(TestingConstants.MONGODB_TEST_DATABASE);
			coll = db.getCollection(STAT_STORAGE_COLLECTION);
			coll.drop();
		}
		return new MongoStatsRetrievalService(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_COLL);
	}

	public static MongoStatsStorageService BuildStorageService() {
		if (m == null) {
			try {
				m = new Mongo();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (MongoException e) {
				e.printStackTrace();
			}
			db = m.getDB(TestingConstants.MONGODB_TEST_DATABASE);
			coll = db.getCollection(STAT_STORAGE_COLLECTION);
			coll.drop();
		}
		return new MongoStatsStorageService(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_COLL);
	}

	public static StatsGathererService BuildGathererService()
			throws ParseException, GathererException {
		if (m == null) {
			try {
				m = new Mongo();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (MongoException e) {
				e.printStackTrace();
			}
			db = m.getDB(TestingConstants.MONGODB_TEST_DATABASE);
			coll = db.getCollection(STAT_STORAGE_COLLECTION);
			coll.drop();
		}

		List<String> keys = new LinkedList<String>();
		keys.add("db::Database Name::");
		keys.add("numObj:avg,max,min:Number of Objects::point");
		keys.add("nColls:avg,max,min:Number of Collections::point");
		keys.add("avgOSz:avg,max,min:Average Object Size:bytes:point");
		keys.add("dataSz:avg,max,min:Data Size:bytes:point");
		keys.add("totSz:avg,max,min:Total Size:bytes:point");
		keys.add("numInd:avg,max,min:Number of Indexes::point");
		keys.add("indSz:avg,max,min:Index Size:objects:point");
		keys.add("numExt:avg,max,min:Number of Extents::point");
		keys.add("fileSz:avg,max,min:File Size:bytes:point");
		Gatherer dbProp = new StatsMongoDBGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE, dbPropName,
				"Database Statistics", keys);

		keys = new LinkedList<String>();
		keys.add("*.ns::Namespace::");
		keys.add("*.numObj:avg,max,min:Number of Objects::point");
		keys.add("*.avgOSz:avg,max,min:Average Object Size:bytes:point");
		keys.add("*.dataSz:avg,max,min:Data Size:bytes:point");
		keys.add("*.totSz:avg,max,min:Total Size:bytes:point");
		keys.add("*.numExt:avg,max,min:Number of Extents::point");
		keys.add("*.lExSz:avg,max,min:Last Extent Size:bytes:point");
		keys.add("*.numInd:avg,max,min:Number of Indexes::point");
		keys.add("*.indSz:avg,max,min:Index Size:objects:point");
		keys.add("*.indSzs.*:avg,max,min:Index Sizes:objects:point");
		Gatherer collProp = new StatsMongoCollectionGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE, collPropName,
				"Collection Statistics", keys);

		keys = new LinkedList<String>();
		keys.add("freeM:avg,max,min:Free Memory:bytes:point");
		keys.add("maxM:avg,max,min:Max Memory:bytes:point");
		keys.add("totM:avg,max,min:Total Memory:bytes:point");
		keys.add("cores:avg,max,min:Processors::point");
		Gatherer runProp = new StatsRuntimeGatherer(runPropName,
				"Runtime Statistics", keys);

		keys = new LinkedList<String>();
		keys.add("numMsg:avg:Number of Messages::interval");
		keys.add("totMsg:avg:Total Number of Messages::point");
		StatsMsgGatherer msgProp = new StatsMsgGatherer(msgSearchPropName,
				"Message Statistics", keys);
		MongoGathererMessageSearchService searchService = null;
		try {
			searchService = new MongoGathererMessageSearchService(m,
					TestingConstants.MONGODB_TEST_DATABASE,
					TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		} catch (MongoException e) {
			e.printStackTrace();
		}
		searchService.setDocSearchService(new IndriDocSearchQueryService());
		msgProp.setSearchService(searchService);

		keys = new LinkedList<String>();
		keys.add("avgAtt:avg:Average Attachments:number of attachments:interval");
		keys.add("maxAtt:max:Maximum Attachments:number of attachments:interval");
		StatsMongoAttachmentsGatherer attProp = new StatsMongoAttachmentsGatherer(
				m, TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_DOCUMENTS_COLLECTION,
				attPropName, "Attachment Statistics", keys);

		HashMap<String, Gatherer> gatherers = new HashMap<String, Gatherer>();
		gatherers.put("DatabaseStatsService", dbProp);
		gatherers.put("CollStatsService", collProp);
		gatherers.put("RuntimeStatsService", runProp);
		gatherers.put("MsgStatsService", msgProp);
		gatherers.put("AttachstatsService", attProp);
		return new StatsGathererService(gatherers);
	}

	public static StatsClient BuildClient() throws GathererException,
			ParseException {
		MongoStatsRetrievalService retrievalTester = BuildRetrievalService();
		MongoStatsStorageService storageTester = BuildStorageService();
		StatsGathererService gatherTester = BuildGathererService();
		return new StatsClient(gatherTester, storageTester, retrievalTester);
	}

	public static void main(String args[]) {
		StatsClient client = null;
		try {
			client = TestHelper.BuildClient();
		} catch (GathererException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (client.gathererNames().size() == 6) {
			System.out.println("Success!");
		} else {
			System.out.println("Warning! (not enough gatherers)");
		}
		System.out.println("Gatherers:");
		for (String name : client.gathererNames()) {
			System.out.println(name);
		}
	}
}
