/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.retriever.mongodb;

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
import com.linuxbox.enkive.archiver.ArchiverUtils;
import com.linuxbox.enkive.archiver.mongodb.MongoArchivingService;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.mongogrid.ConvenienceMongoGridDocStoreService;
import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;
import com.mongodb.MongoClient;

@RunWith(Parameterized.class)
public class MongoRetrieverServiceTest {

	static MongoClient m;
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
		m = new MongoClient();
		docStoreService = new ConvenienceMongoGridDocStoreService(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_DOCUMENTS_COLLECTION);
		docStoreService.startup();

		retriever = new MongoRetrieverService(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
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
		this.messageString = ArchiverUtils.readMessage(filestream);
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
		return ArchiverUtils.getAllTestFiles(dir);
	}

}
