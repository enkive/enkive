package com.linuxbox.enkive.archiver.mongodb;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.archiver.exceptions.FailedToEmergencySaveException;
import com.linuxbox.enkive.archiver.ArchiverUtils;
import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.audit.AuditServiceException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.mongogrid.ConvenienceMongoGridDocStoreService;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;
import com.mongodb.Mongo;

@RunWith(Parameterized.class)
public class MongoArchivingServiceFailureTest {

	static Mongo m;
	static MongoArchivingService archiver;
	static DocStoreService docStoreService;

	private File file;
	private Message message;

	public MongoArchivingServiceFailureTest(File file) {
		this.file = file;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return ArchiverUtils.getAllTestFiles(new File(
				TestingConstants.TEST_MESSAGE_DIRECTORY));
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		m = new Mongo();
		docStoreService = new ConvenienceMongoGridDocStoreService(m,
				"enkive-test", "documents-test");
		docStoreService.startup();

		archiver = new MongoArchivingService(m, "enkive-test", "messages-test");
		archiver.setDocStoreService(docStoreService);
		archiver.setEmergencySaveRoot("test/emergencySaveFiles/");
		m.close();
	}

	@Before
	public void setUp() throws Exception {
		InputStream filestream = new FileInputStream(file);
		this.message = new MessageImpl(filestream);
		filestream.close();
	}

	@After
	public void tearDown() throws Exception {

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		docStoreService.shutdown();
		m.close();
	}

	@Test
	public void testMessageStore() throws CannotArchiveException,
			FailedToEmergencySaveException, AuditServiceException, IOException {
		String messageUUID = archiver.storeOrFindMessage(message);
		System.out.println(messageUUID);
		assertEquals("Identifiers should be the same",
				archiver.findMessage(message), messageUUID);
	}
}
