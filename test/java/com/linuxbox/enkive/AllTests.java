package com.linuxbox.enkive;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreServiceTest;
import com.linuxbox.util.HashingInputStreamTest;
import com.linuxbox.util.mongodb.MongoLockingServiceTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  // Add a JUnit 3 suite
  // CalculatorSuite.class,
  // JUnit 4 style tests
  HashingInputStreamTest.class,
  MongoLockingServiceTest.class,
  MongoGridDocStoreServiceTest.class
})
public class AllTests {
	public static Test suite() {
		TestSuite suite = new TestSuite(AllTests.class.getName());
		//$JUnit-BEGIN$
		//$JUnit-END$
		return suite;
	}
}
