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
/*
 * 
 */
package com.linuxbox.enkive.message.retention;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.TestingConstants;
import com.linuxbox.enkive.archiver.ArchiverUtils;
import com.linuxbox.enkive.archiver.mongodb.MongoArchivingService;
import com.linuxbox.enkive.audit.AuditService;
import com.linuxbox.enkive.audit.mongodb.MongoAuditService;
import com.linuxbox.enkive.docsearch.DocSearchQueryService;
import com.linuxbox.enkive.docsearch.indri.IndriDocSearchQueryService;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.mongogrid.ConvenienceMongoGridDocStoreService;
import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.search.mongodb.MongoMessageSearchService;
import com.linuxbox.enkive.retriever.mongodb.MongoRetrieverService;
import com.linuxbox.enkive.workspace.searchQuery.mongo.MongoSearchQueryBuilder;
import com.linuxbox.enkive.workspace.searchResult.mongo.MongoSearchResultBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoMessageRetentionPolicyEnforcementTest {

	static MongoClient m;
	static MongoRetrieverService retriever;
	static DocStoreService docStoreService;
	static MessageRetentionPolicyEnforcer policyEnforcer;
	static MongoMessageSearchService searchService;
	static MongoArchivingService archiver;
	static DocSearchQueryService docSearchService;
	static AuditService auditService;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		m = new MongoClient();
		docStoreService = new ConvenienceMongoGridDocStoreService(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_DOCUMENTS_COLLECTION);
		docStoreService.startup();

		auditService = new MongoAuditService(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_AUDIT_COLLECTION);

		docSearchService = new IndriDocSearchQueryService();

		searchService = new MongoMessageSearchService(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		searchService.setDocSearchService(docSearchService);
		searchService.finishSetup();
		MongoSearchQueryBuilder queryBuilder = new MongoSearchQueryBuilder(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_QUERY_COLLECTION,
				TestingConstants.MONGODB_TEST_WORKSPACE_COLLECTION);
		queryBuilder.setSearchResultBuilder(new MongoSearchResultBuilder(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_WORKSPACE_COLLECTION));
		searchService.setSearchQueryBuilder(queryBuilder);

		archiver = new MongoArchivingService(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		archiver.setDocStoreService(docStoreService);
		archiver.setAuditService(auditService);

		retriever = new MongoRetrieverService(m,
				TestingConstants.MONGODB_TEST_DATABASE,
				TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		retriever.setDocStoreService(docStoreService);
		retriever.setAuditService(auditService);

		policyEnforcer = new MessageRetentionPolicyEnforcer();

		policyEnforcer.setMessageArchivingService(archiver);
		policyEnforcer.setSearchService(searchService);

		MessageRetentionPolicy retentionPolicy = new MessageRetentionPolicy();
		HashMap<String, String> retentionPolicyCriteria = new HashMap<String, String>();

		retentionPolicyCriteria.put("retentionPeriod", "30");
		retentionPolicyCriteria.put("limit", "10");

		retentionPolicy.setRetentionPolicyCriteria(retentionPolicyCriteria);
		policyEnforcer.setRetentionPolicy(retentionPolicy);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		docStoreService.shutdown();
		m.dropDatabase(TestingConstants.MONGODB_TEST_DATABASE);
		m.close();
	}

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRetentionTestingMessagesExist() {
		try {
			// Past Message
			retriever.retrieve("439112126b7305e009075d8b61c4fb4fac030597");
			// Future Message
			retriever.retrieve("388e97415b12ec608f7dd7b4dec4a9574fe485c3");
		} catch (CannotRetrieveException e) {
			fail("Could not retrieve retention period testing messages");
		}
	}

	@Test
	public void testRemoveMessages() {
		policyEnforcer.enforceMessageRetentionPolicies();
		assertTrue("Future Message has been removed", !getAllTestMessages()
				.contains("388e97415b12ec608f7dd7b4dec4a9574fe485c3"));
	}

	@Test
	public void testDidNotRemoveDoubleReferencedAttachment() {
		try {
			File testFile = new File(TestingConstants.TEST_MESSAGE_DIRECTORY
					+ "/retentionPolicyTestData/future-message.msg");
			InputStream filestream = new FileInputStream(testFile);
			String messageString = ArchiverUtils.readMessage(filestream);

			Message pastMessage = retriever
					.retrieve("388e97415b12ec608f7dd7b4dec4a9574fe485c3");
			assertEquals(
					"Rebuilt past message text should be the same as original",
					pastMessage.getReconstitutedEmail().trim(),
					messageString.trim());
		} catch (CannotRetrieveException e) {
			fail("Could not retrieve retention period testing messages");
		} catch (FileNotFoundException e) {
			fail("Could not read original retention period testing message");
		} catch (IOException e) {
			fail("Could not read original retention period testing message");
		}
	}

	private Collection<Object[]> getAllTestMessages() {
		Collection<Object[]> testMessages = new ArrayList<Object[]>();

		DB enkive = m.getDB(TestingConstants.MONGODB_TEST_DATABASE);
		DBCollection messageColl = enkive
				.getCollection(TestingConstants.MONGODB_TEST_MESSAGES_COLLECTION);
		DBCursor messages = messageColl.find();

		while (messages.hasNext()) {
			DBObject message = messages.next();
			String messageId = (String) message.get("_id");
			testMessages.add(new String[] { messageId });
		}

		return testMessages;
	}

}
