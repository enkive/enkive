package com.linuxbox.enkive.teststats;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.statistics.gathering.StatsRuntimeGatherer;

import static com.linuxbox.enkive.statistics.StatsConstants.*;

public class StatsRuntimeTest {
	protected static StatsRuntimeGatherer rtStat = new StatsRuntimeGatherer("RuntimeGatherer", "* * * * * ?");
	protected static Map<String, Object> stats;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		stats = rtStat.getStatistics();
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

	@Test
	public void maxMem() {
		long memory = ((Long) stats.get(STAT_MAX_MEMORY)).longValue();
		assertTrue("runtime test exception in maxMem(): memory = " + memory,
				memory > 0);
	}

	@Test
	public void freeMem() {
		long memory = ((Long) stats.get(STAT_FREE_MEMORY)).longValue();
		assertTrue("runtime test exception in freeMem(): memory = " + memory,
				memory > 0);
	}

	@Test
	public void totalMem() {
		long memory = ((Long) stats.get(STAT_TOTAL_MEMORY)).longValue();
		assertTrue("runtime test exception in totalMem(): memory = " + memory,
				memory > 0);
	}

	@Test
	public void processors() {
		int numProcessors = ((Integer) stats.get(STAT_PROCESSORS)).intValue();
		assertTrue("runtime test exception in processors(): numProcessors = "
				+ numProcessors, numProcessors > 0);
	}
}
