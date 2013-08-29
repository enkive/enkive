/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * 
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
package com.linuxbox.enkive;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.linuxbox.enkive.archiver.mongodb.MongoArchivingServiceTest;
import com.linuxbox.enkive.audit.mongodb.MongoAuditServiceTest;
import com.linuxbox.enkive.docstore.AbstractDocStoreServiceTest;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreServiceTest;
import com.linuxbox.enkive.filter.EnkiveFilterTest;
import com.linuxbox.enkive.message.retention.MongoMessageRetentionPolicyEnforcementTest;
import com.linuxbox.enkive.retriever.mongodb.MongoRetrieverServiceTest;
import com.linuxbox.enkive.teststats.StatsMongoAttachTest;
import com.linuxbox.enkive.teststats.StatsMongoCollTest;
import com.linuxbox.enkive.teststats.StatsMongoDbTest;
import com.linuxbox.enkive.teststats.StatsMsgTest;
import com.linuxbox.enkive.teststats.StatsRuntimeTest;
import com.linuxbox.util.HashingInputStreamTest;
import com.linuxbox.util.mongodb.JavaLockingServiceTest;
import com.linuxbox.util.mongodb.MongoLockingServiceTest;
import com.linuxbox.util.queueservice.mongodb.JavaQueueServiceTest;
import com.linuxbox.util.queueservice.mongodb.MongoQueueServiceTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ HashingInputStreamTest.class,
		AbstractDocStoreServiceTest.class, MongoAuditServiceTest.class,
		MongoLockingServiceTest.class, MongoQueueServiceTest.class,
		MongoGridDocStoreServiceTest.class, MongoArchivingServiceTest.class,
		StatsMongoDbTest.class, StatsMongoCollTest.class, StatsMsgTest.class,
		StatsRuntimeTest.class, StatsMongoAttachTest.class,
		MongoRetrieverServiceTest.class,
		MongoMessageRetentionPolicyEnforcementTest.class, // has problems
															// (infinite loop
															// problems)
		// Unimplemented - IndriQueryComposerTest.class,
		EnkiveFilterTest.class, JavaLockingServiceTest.class,
		JavaQueueServiceTest.class })
public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		// $JUnit-END$
		return suite;
	}
}