package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_STORAGE_COLLECTION;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.gathering.GathererInterface;
import com.linuxbox.enkive.statistics.gathering.StatsMsgSearchGatherer;
import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoAttachmentsGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoCollectionGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoDBGatherer;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoMsgGatherer;
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
	private static final String msgSearchPropName = "msgSearchGatherer";
	private static final String attPropName = "attGatherer";
	private static final String msgStatPropName = "msgGatherer";
	
	public static DBCollection GetTestCollection(){
		if(m == null){
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

	public static MongoStatsRetrievalService BuildRetrievalService(){
		if(m == null){
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
				TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_COLL);
	}
	
	public static MongoStatsStorageService BuildStorageService(){
		if(m == null){
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
				TestingConstants.MONGODB_TEST_DATABASE, TestingConstants.MONGODB_TEST_COLL);
	}
	
	public static StatsGathererService BuildGathererService() throws ParseException, GathererException{
		if(m == null){
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
		keys.add("db::Database Name:");
		keys.add("numObj:avg,max,min:Number of Objects:");
		keys.add("nColls:avg,max,min:Number of Collections:");
		keys.add("avgOSz:avg,max,min:Average Object Size:bytes");
		keys.add("dataSz:avg,max,min:Data Size:bytes");
		keys.add("totSz:avg,max,min:Total Size:bytes");
		keys.add("numInd:avg,max,min:Number of Indexes");
		keys.add("indSz:avg,max,min:Index Size:objects");
		keys.add("numExt:avg,max,min:Number of Extents:");
		keys.add("fileSz:avg,max,min:File Size:bytes");
		GathererInterface dbProp = new StatsMongoDBGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE, dbPropName, "Database Statistics",
				"* * * * * ?", keys);
		
		keys = new LinkedList<String>();
		keys.add("*.ns::Namespace:");
		keys.add("*.numObj:avg,max,min:Number of Objects:");
		keys.add("*.avgOSz:avg,max,min:Average Object Size:bytes");
		keys.add("*.dataSz:avg,max,min:Data Size:bytes");
		keys.add("*.totSz:avg,max,min:Total Size:bytes");
		keys.add("*.numExt:avg,max,min:Number of Extents:");
		keys.add("*.lExSz:avg,max,min:Last Extent Size:bytes");
		keys.add("*.numInd:avg,max,min:Number of Indexes:");
		keys.add("*.indSz:avg,max,min:Index Size:objects");
		keys.add("*.indSzs.*:avg,max,min:Index Sizes:objects");
		GathererInterface collProp = new StatsMongoCollectionGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE, collPropName, "Collection Statistics",
				"* * * * * ?", keys);
		
		keys = new LinkedList<String>();
		keys.add("freeM:avg,max,min:Free Memory:bytes");
		keys.add("maxM:avg,max,min:Max Memory:bytes");
		keys.add("totM:avg,max,min:Total Memory:bytes");
		keys.add("cores:avg,max,min:Processors:");
		GathererInterface runProp = new StatsRuntimeGatherer(runPropName, "Runtime Statistics",
				"* * * * * ?", keys);
		
		keys = new LinkedList<String>();
		keys.add("numMsg:avg,max,min:Number of Messages:messages");
		StatsMsgSearchGatherer msgProp = new StatsMsgSearchGatherer(
				msgSearchPropName, "Message Statistics", "* * * * * ?", keys);
		MongoMessageSearchService searchService = null;
		try {
			searchService = new MongoMessageSearchService(m,
					TestingConstants.MONGODB_TEST_DATABASE,
					TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		} catch (MongoException e) {
			e.printStackTrace();
		}
		searchService.setDocSearchService(new IndriDocSearchQueryService());
		msgProp.setSearchService(searchService);
		
		keys = new LinkedList<String>();
		keys.add("avgAtt:avg:Average Attachments:number of attachments");
		keys.add("maxAtt:max:Maximum Attachments:number of attachments");
		StatsMongoAttachmentsGatherer attProp = new StatsMongoAttachmentsGatherer(
				m, TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_DOCUMENTS_COLLECTION,
				attPropName, "Attachment Statistics", "* * * * * ?", false, keys);
		attProp.setLowerDate(new Date(0L));
		attProp.setUpperDate(new Date());
		
		keys = new LinkedList<String>();
		keys.add("msgArchive:avg,max,min:Archive Size:messages");
		GathererInterface msgStatProp = new StatsMongoMsgGatherer(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION,
				msgStatPropName, "Archive Statistics", "* * * * * ?", keys);

		HashMap<String, GathererInterface> gatherers = new HashMap<String, GathererInterface>();
		gatherers.put("DatabaseStatsService", dbProp);
		gatherers.put("CollStatsService", collProp);
		gatherers.put("RuntimeStatsService", runProp);
		gatherers.put("MsgEntriesStatsService", msgProp);
		gatherers.put("AttachstatsService", attProp);
		gatherers.put("msgStatStatsService", msgStatProp);
		return new StatsGathererService(gatherers);
	}
	
	public static StatsClient BuildClient() throws GathererException, ParseException{
		MongoStatsRetrievalService retrievalTester = BuildRetrievalService();
		MongoStatsStorageService storageTester = BuildStorageService();
		StatsGathererService gatherTester = BuildGathererService();
		return new StatsClient(gatherTester, storageTester, retrievalTester);
	}
	
	public static void main(String args[]){
		StatsClient client = null;
		try {
			client = TestHelper.BuildClient();
		} catch (GathererException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(client.gathererNames().size() == 6){
			System.out.println("Success!");
		} else {
			System.out.println("Warning! (not enough gatherers)");
		}
		System.out.println("Gatherers:");
		for(String name: client.gathererNames()){
			System.out.println(name);
		}
	}	
}
