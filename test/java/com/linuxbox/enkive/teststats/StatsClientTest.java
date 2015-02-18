/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
		assertTrue("the number of gatherers is incorrect: " + size, size == 5);
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
