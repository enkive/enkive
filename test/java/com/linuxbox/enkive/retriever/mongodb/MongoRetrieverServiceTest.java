package com.linuxbox.enkive.retriever.mongodb;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
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

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.archiver.mongodb.MongoArchivingService;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.mongogrid.ConvenienceMongoGridDocStoreService;
import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;
import com.mongodb.Mongo;

@RunWith(Parameterized.class)
public class MongoRetrieverServiceTest {

	static Mongo m;
	static MongoRetrieverService retriever;
	static DocStoreService docStoreService;

	private File file;
	private String messageUUID;
	private String messageString;

	public MongoRetrieverServiceTest(File file) {
		this.file = file;
	}

	@Parameters
	public static Collection<Object[]> data() {
		return getAllTestFiles(new File(TestingConstants.TEST_MESSAGE_DIRECTORY));
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		m = new Mongo();
		docStoreService = new ConvenienceMongoGridDocStoreService(m,
				"enkive-test", "documents-test");
		docStoreService.startup();

		retriever = new MongoRetrieverService(m, "enkive-test", "messages-test");
		retriever.setDocStoreService(docStoreService);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		docStoreService.shutdown();

		m.close();
	}

	@Before
	public void setUp() throws Exception {
		InputStream filestream = new FileInputStream(file);
		this.messageString = readMessage(filestream);
		filestream.close();
		filestream = new FileInputStream(file);
		Message message = new MessageImpl(filestream);
		messageUUID = MongoArchivingService.calculateMessageId(message);
		filestream.close();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMessageStore() throws CannotRetrieveException, IOException {
		Message retrievedMessage = retriever.retrieve(messageUUID);
		assertEquals("Rebuilt message text should be the same as original",
				retrievedMessage.getReconstitutedEmail().trim(),
				messageString.trim());
	}

	private static Collection<Object[]> getAllTestFiles(File dir) {
		Collection<Object[]> files = new ArrayList<Object[]>();
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					files.add(new File[] { file });
				} else {
					return getAllTestFiles(file);
				}
			}
		}
		return files;
	}

	private static String readMessage(InputStream inputStream)
			throws IOException {
		StringBuffer message = new StringBuffer();
		InputStreamReader reader = new InputStreamReader(inputStream);

		Reader in = new BufferedReader(reader);
		int ch;
		while ((ch = in.read()) > -1) {
			message.append((char) ch);
		}
		in.close();
		return message.toString();
	}

}
