package com.linuxbox.enkive;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.linuxbox.enkive.archiver.mongodb.MongoArchivingServiceTest;
import com.linuxbox.enkive.docstore.AbstractDocStoreServiceTest;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreServiceTest;
import com.linuxbox.enkive.retriever.mongodb.MongoRetrieverServiceTest;
import com.linuxbox.util.HashingInputStreamTest;
import com.linuxbox.util.mongodb.MongoLockingServiceTest;
import com.linuxbox.util.queueservice.mongodb.MongoQueueServiceTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  // Add a JUnit 3 suite
  // CalculatorSuite.class,
  // JUnit 4 style tests
  HashingInputStreamTest.class,
  AbstractDocStoreServiceTest.class,
  MongoLockingServiceTest.class,
  MongoQueueServiceTest.class,
  MongoGridDocStoreServiceTest.class,
  MongoArchivingServiceTest.class,
  MongoRetrieverServiceTest.class
})
public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		//$JUnit-END$
		return suite;
	}
}
