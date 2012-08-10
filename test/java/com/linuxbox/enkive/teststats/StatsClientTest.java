package com.linuxbox.enkive.teststats;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.statistics.gathering.GathererException;
import com.linuxbox.enkive.statistics.services.StatsClient;

public class StatsClientTest {
	private static StatsClient client;
	private static final String dbPropName = "dbGatherer";
	private static final String collPropName = "collGatherer";
	private static final String runPropName = "rtGatherer";
	private static final String msgSearchPropName = "msgSearchGatherer";
	private static final String attPropName = "attGatherer";
	private static final String msgStatPropName = "msgGatherer";

	@BeforeClass
	public static void setUp() throws ParseException, GathererException {
		client = TestHelper.BuildClient();
	}

	@Test
	public void correctNumGatherers() {
		int size = client.gathererNames().size();
		assertTrue("the number of gatherers is incorrect: " + size, size == 6);
	}

	@Test
	public void correctNamedGatherers() {
		for (String name : client.gathererNames()) {
			assertTrue("name is not valid: " + name, name == dbPropName
					|| name == collPropName || name == runPropName
					|| name == msgSearchPropName || name == attPropName
					|| name == msgStatPropName);
		}
	}
}
