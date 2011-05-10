package com.linuxbox.enkive.archiver.mongodb;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreService;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;
import com.mongodb.Mongo;

@RunWith(Parameterized.class)
public class MongoArchivingServiceTest {

	static Mongo m;
	static MongoArchivingService archiver;

	private File file;
	private Message message;

	public MongoArchivingServiceTest(File file) {
		this.file = file;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return getAllTestFiles(new File("test/data/mime4jTestData"));
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		m = new Mongo();
		archiver = new MongoArchivingService(m, "enkive-test", "messages-test");
		MongoGridDocStoreService docStoreService = new MongoGridDocStoreService(
				m, "enkive-test", "documents-test");
		archiver.setDocStoreService(docStoreService);
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
		m.close();
	}

	@Test
	public void testMessageStore() throws CannotArchiveException {
		String messageUUID = archiver.storeOrFindMessage(message);
		assertEquals("Identifiers should be the same",
				archiver.findMessage(message), messageUUID);
	}

	private static Collection<Object[]> getAllTestFiles(File dir) {
		Collection<Object[]> files = new ArrayList<Object[]>();
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					files.add(new File[] { file });
				} else {
					getAllTestFiles(file);
				}
			}
		}
		return files;
	}
}
