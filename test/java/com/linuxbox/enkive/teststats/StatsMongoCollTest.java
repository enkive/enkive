package com.linuxbox.enkive.teststats;

import static com.linuxbox.enkive.statistics.StatsConstants.STAT_AVG_OBJ_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_DATA_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_LAST_EXTENT_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_EXTENT;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_INDEX;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_NUM_OBJS;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_SERVICE_NAME;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TIME_STAMP;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_INDEX_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TOTAL_SIZE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE_COLL;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE_RUN;
import static com.linuxbox.enkive.statistics.StatsConstants.STAT_TYPE_DB;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import com.linuxbox.enkive.TestingConstants;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.linuxbox.enkive.statistics.KeyDef;
import com.linuxbox.enkive.statistics.gathering.mongodb.StatsMongoCollectionGatherer;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

@SuppressWarnings("unchecked")
@RunWith(value = Parameterized.class)
public class StatsMongoCollTest {
	private static StatsMongoCollectionGatherer collStats;
	private static Map<String, Object> allStats;
	private static DB db;
	private String collName;
	private static String name = "CollGatherer";

	public StatsMongoCollTest(String collName) {
		this.collName = collName;
	}

	@Parameters
	public static Collection<Object[]> data() {
		Mongo m = null;
		try {
			m = new Mongo();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(0);	
		} catch (MongoException e) {
			e.printStackTrace();
			System.exit(0);
		}
		db = m.getDB(TestingConstants.MONGODB_TEST_DATABASE);
		collStats = new StatsMongoCollectionGatherer(m, TestingConstants.MONGODB_TEST_DATABASE, "CollGatherer", "0 * * * * ?");
		allStats = collStats.getStatistics();
		List<Object[]> data = new ArrayList<Object[]>();
		System.out.println("Not testing the following empty DB's: ");
		for(String name : db.getCollectionNames()){
			if(db.getCollection(name).count() > 0){
				if(name.startsWith("$")){
					name = name.replaceFirst("$", "-");
				}
				name = name.replace('.', '-');
				Object[] thing = { name };
				data.add(thing);
			}
			else{
				System.out.println(name);
			}
		}
		return data;
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	//TODO: move to main api in the future?
	public boolean checkFormat(Map<String, Object> stats, LinkedList<String> path){
		if(path.contains(STAT_SERVICE_NAME)){
			return true;
		}
		
		if(path.isEmpty()){
			return false;
		}
		String key = path.getFirst();
		if(path.size() == 1){
			if(key.equals("*"))
				return stats != null;
			else {
				return stats.get(key) != null; 
			}
		}
		
		boolean result = false;
		if (key.equals("*")) {
			path.removeFirst();
			for(String statKey: stats.keySet()){
				if(!(stats.get(statKey) instanceof Map)){
					result = path.size() == 1;
				} else {
					result = checkFormat((Map<String, Object>)stats.get(statKey), path);
				}
				if(result){
					break;
				}
			}
			path.addFirst(key);
			return result;
		} else if (stats.containsKey(key)) {
			path.removeFirst();
			result = checkFormat((Map<String, Object>)stats.get(key), path);
			path.addFirst(key);
			return result;
		}
		return false;
	}

	@Test
	public void testAttributes(){
		for(KeyDef key: collStats.getAttributes().getKeys()){
			LinkedList<String> path = key.getKey();
			assertTrue("the format is incorrect for path: " + path,checkFormat(allStats, path));
		}
	}
	
	@Test
	public void hasServiceName() {
		String sn = (String) collStats.getAttributes().getName();
		assertNotNull("no service name found in hasServiceName()", sn);
		assertTrue(sn.equals(name));
	}
	
	@Test
	public void hasTimeStamp() {
		Long time = ((Long) allStats.get(STAT_TIME_STAMP));
		assertTrue("runtime test exception in hasTimeStamp(): time = " + time,
				time != null);
	}
	
	@Test
	public void timeGTZero() {
		Long time = ((Long) allStats.get(STAT_TIME_STAMP));
		assertTrue("runtime test exception in timeGTZero(): time = " + time,
				time > 0);
	}
	
	@Test
	public void typeTest() {
		Map<String, Object> obj = (Map<String, Object>) allStats.get(collName);
		String type = (String) obj.get(STAT_TYPE);
		assertNotNull("in " + collName + " (type = null)", type);
		assertTrue(
				"in " + collName + " (type = " + type + ")",
				type.compareTo(STAT_TYPE_COLL) == 0
						|| type.compareTo(STAT_TYPE_DB) == 0
						|| type.compareTo(STAT_TYPE_RUN) == 0);
	}

	@Test
	public void collExistsTest() {
		assertTrue(collName + " does not exist", allStats.containsKey(collName));
	}

	@Test
	public void namespaceExistsTest() {
		Map<String, Object> obj = (Map<String, Object>) allStats.get(collName);
		assertNotNull("in " + collName + " (obj = null)", obj);
		assertTrue(
				"in " + collName + "does not contain field(" + STAT_NS + ")",
				obj.containsKey(STAT_NS));
	}

	@Test
	public void keyCountMatches() {
		int numKeys = ((Map<String, Object>) allStats.get(collName)).keySet().size();
		assertTrue("numKeys doesn't match: numKeys = " + numKeys, numKeys == 11);
	}
	
	// GT means 'greater than'
	@Test
	public void numObjsGTZeroTest() {
		Map<String, Object> obj = (Map<String, Object>) allStats.get(collName);
		assertNotNull("in " + collName + " (numObjs = null)",
				(Integer) obj.get(STAT_NUM_OBJS));
		int numObjs = ((Integer) obj.get(STAT_NUM_OBJS)).intValue();
		assertTrue("in " + collName + " (numObjs = " + numObjs + ") ",
				numObjs > 0);
	}

	@Test
	public void avgObjsGTZeroTest() {
		Map<String, Object> obj = (Map<String, Object>) allStats.get(collName);
		assertNotNull("in " + collName + " (avgObjs = null)",
				((Double) obj.get(STAT_AVG_OBJ_SIZE)));
		double avgObjs = ((Double) obj.get(STAT_AVG_OBJ_SIZE)).doubleValue();
		assertTrue("in " + collName + " (avgObjs = " + avgObjs + ") ",
				avgObjs > 0);
	}

	@Test
	public void dataGTZeroTest() {
		Map<String, Object> obj = (Map<String, Object>) allStats.get(collName);
		assertNotNull("in " + collName + " (data = null)",
				((Integer) obj.get(STAT_DATA_SIZE)));
		int data = ((Integer) obj.get(STAT_DATA_SIZE)).intValue();
		assertTrue("in " + collName + " (data = " + data + ") ", data > 0);
	}

	@Test
	public void storageGTZeroTest() {
		Map<String, Object> obj = (Map<String, Object>) allStats.get(collName);
		assertNotNull("in " + collName + " (storage = null)",
				((Integer) obj.get(STAT_TOTAL_SIZE)));
		int storage = ((Integer) obj.get(STAT_TOTAL_SIZE)).intValue();
		assertTrue("in " + collName + " (storage = " + storage + ") ",
				storage > 0);
	}

	@Test
	public void extentsGTZeroTest() {
		Map<String, Object> obj = (Map<String, Object>) allStats.get(collName);
		assertNotNull("in " + collName + " (numExtents = null)",
				(Integer) obj.get(STAT_NUM_EXTENT));
		int numExtents = ((Integer) obj.get(STAT_NUM_EXTENT)).intValue();
		assertTrue(collName, numExtents > 0);
	}

	@Test
	public void lastExtentSizeGTZeroTest() {
		Map<String, Object> obj = (Map<String, Object>) allStats.get(collName);
		assertNotNull("in " + collName + " (lastExtentsSize = null)",
				((Integer) obj.get(STAT_LAST_EXTENT_SIZE)));
		int lastExtentSize = ((Integer) obj.get(STAT_LAST_EXTENT_SIZE))
				.intValue();
		assertTrue(collName, lastExtentSize > 0);
	}

	@Test
	public void numIndexesGTEZeroTest() {
		Map<String, Object> obj = (Map<String, Object>) allStats.get(collName);
		assertNotNull("in " + collName + " (numIndexes = null)",
				((Integer) obj.get(STAT_NUM_INDEX)));
		int numIndexes = ((Integer) obj.get(STAT_NUM_INDEX)).intValue();
		assertTrue("in " + collName + "(numIndexes = " + numIndexes + ")",
				numIndexes >= 0);
	}

	@Test
	public void indexSizeGTEZeroTest() {
		Map<String, Object> obj = (Map<String, Object>) allStats.get(collName);
		assertNotNull("in " + collName + " (totalIndexSize = null)",
				(Integer) obj.get(STAT_TOTAL_INDEX_SIZE));
		Integer integer = (Integer) obj.get(STAT_TOTAL_INDEX_SIZE);
		int indexSize = integer.intValue();
		assertTrue("in " + collName + "(indexSize = " + indexSize + ")",
				indexSize >= 0);
	}
}