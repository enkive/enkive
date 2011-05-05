package com.linuxbox.enkive.docstore.mongogrid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.apache.james.mime4j.util.MimeUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.linuxbox.enkive.docstore.AbstractDocStoreService;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.StringDocument;
import com.linuxbox.util.HashingInputStream;
import com.mongodb.gridfs.GridFSFile;

public class MongoGridDocStoreServiceTest {
	static final String testString = "to be or not to be; that is the \u20AC 1 question";
	static final byte[] testData = testString
			.getBytes(Constants.PREFERRED_CHARSET);
	static Document testDocument;
	static String testDocumentName;

	static MongoGridDocStoreService service;

	@BeforeClass
	public static void setUpClass() throws Exception {
		service = new MongoGridDocStoreService("enkive-test", "fs-test");
		testDocument = new StringDocument(testString, "text/plain", "txt",
				MimeUtil.ENC_7BIT);

		MessageDigest messageDigest = getPrimedMessageDigest(testDocument);
		messageDigest.update(testData);
		final byte[] hashBytes = messageDigest.digest();
		testDocumentName = new String((new Hex()).encode(hashBytes));
	}

	@AfterClass
	public static void tearDownClass() {
		service.getDb().dropDatabase();
		service.shutdown();
	}

	/*
	 * @Before public void setUp() throws Exception { }
	 * 
	 * @After public void tearDown() throws Exception { }
	 */

	@Test
	public void testFileControlBasic() throws Exception {
		String id = UUID.randomUUID().toString();
		assertTrue(service.controlFile(id));
		service.releaseControlOfFile(id);
	}

	@Test
	public void testFileControlDoubleControl() throws Exception {
		String id = UUID.randomUUID().toString();
		assertTrue(service.controlFile(id));
		assertFalse(service.controlFile(id));
		service.releaseControlOfFile(id);
	}

	@Test(expected = ControlReleaseException.class)
	public void testFileControlNoControl() throws Exception {
		String id = UUID.randomUUID().toString();
		service.releaseControlOfFile(id);
	}

	@Test
	public void testStoreKnownNameFullLength() throws Exception {
		String id = UUID.randomUUID().toString();

		boolean alreadyStored = service.storeKnownName(testDocument, id,
				testData, testData.length);
		assertFalse(alreadyStored);

		Document outDoc = service.retrieve(id);

		BufferedReader r = new BufferedReader(new InputStreamReader(
				outDoc.getEncodedContentStream(), Constants.PREFERRED_CHARSET));

		assertEquals(testString, r.readLine());
		assertNull(r.readLine());

		r.close();

		service.gridFS.remove(id);
	}

	@Test
	public void testStoreKnownNamePartialLength() throws Exception {
		final int length = testString.length() / 2;
		String id = UUID.randomUUID().toString();

		boolean alreadyStored = service.storeKnownName(testDocument, id,
				testData, length);
		assertFalse(alreadyStored);

		Document outDoc = service.retrieve(id);

		InputStream is = outDoc.getEncodedContentStream();

		byte[] out = new byte[length];
		int totalRead = 0;
		do {
			int read = is.read(out);
			totalRead += read;
		} while (totalRead < length);

		for (int i = 0; i < out.length; i++) {
			Assert.assertEquals(testData[i], out[i]);
		}

		Assert.assertTrue(is.read() < 0);

		is.close();

		service.gridFS.remove(id);
	}

	@Test
	public void testIndexing() throws Exception {
		String id = UUID.randomUUID().toString();

		service.storeKnownName(testDocument, id, testData, testData.length);

		GridFSFile gFile1 = service.gridFS.findOne(id);
		assertEquals("a new file is initially UNINDEXED",
				(Integer) MongoGridDocStoreService.STATUS_UNINDEXED,
				(Integer) gFile1.getMetaData().get(Constants.INDEX_STATUS_KEY));

		final String nextUnindexedId1 = service.nextUnindexed();
		assertEquals(id, nextUnindexedId1);

		GridFSFile gFile2 = service.gridFS.findOne(id);
		assertEquals(
				"once a file has been retrieved as unindexed, it should not be in INDEXING state",
				(Integer) MongoGridDocStoreService.STATUS_INDEXING,
				(Integer) gFile2.getMetaData().get(Constants.INDEX_STATUS_KEY));

		final String nextUnindexedId2 = service.nextUnindexed();
		assertNull(nextUnindexedId2);

		service.markAsIndexed(id);
		GridFSFile gFile3 = service.gridFS.findOne(id);
		assertEquals(
				"now that a file is marked as indexed, it should appear as INDEXED",
				(Integer) MongoGridDocStoreService.STATUS_INDEXED,
				(Integer) gFile3.getMetaData().get(Constants.INDEX_STATUS_KEY));

		service.gridFS.remove(id);
	}

	@Test
	public void testStoreAndDetermineName() throws Exception {
		HashingInputStream in = new HashingInputStream(
				getPrimedMessageDigest(testDocument), new ByteArrayInputStream(
						testData));

		StoreRequestResult result = service.storeAndDetermineName(testDocument,
				in);

		assertFalse(result.getAlreadyStored());

		Document outDoc = service.retrieve(result.getIdentifier());

		BufferedReader r = new BufferedReader(new InputStreamReader(
				outDoc.getEncodedContentStream(), Constants.PREFERRED_CHARSET));

		assertEquals(testString, r.readLine());
		assertNull(r.readLine());

		r.close();

		service.gridFS.remove(result.getIdentifier());
	}

	@Test
	public void testStoreKnownNameWithExistingDocument() throws Exception {
		boolean already1 = service.storeKnownName(testDocument,
				testDocumentName, testData, testData.length);

		assertFalse(already1);

		HashingInputStream doc1Input = new HashingInputStream(
				getPrimedMessageDigest(testDocument), new ByteArrayInputStream(
						testData));

		StoreRequestResult result2 = service.storeAndDetermineName(
				testDocument, doc1Input);

		assertTrue(result2.getAlreadyStored());

		assertEquals("names should be the same", testDocumentName,
				result2.getIdentifier());

		service.gridFS.remove(testDocumentName);
	}

	@Test
	public void testStoreAndDetermineNameWithExistingDocument()
			throws Exception {
		HashingInputStream doc1Input = new HashingInputStream(
				getPrimedMessageDigest(testDocument), new ByteArrayInputStream(
						testData));

		StoreRequestResult result1 = service.storeAndDetermineName(
				testDocument, doc1Input);

		assertFalse(result1.getAlreadyStored());

		boolean already2 = service.storeKnownName(testDocument,
				testDocumentName, testData, testData.length);

		assertTrue(already2);

		assertEquals("names should be the same", testDocumentName,
				result1.getIdentifier());

		service.gridFS.remove(result1.getIdentifier());
	}

	/**
	 * Returns a MessageDigest that is primed with the data for the type of
	 * document.
	 * 
	 * @param doc
	 * @return
	 * @throws Exception
	 */
	private static MessageDigest getPrimedMessageDigest(Document doc)
			throws Exception {
		MessageDigest messageDigest = MessageDigest
				.getInstance(AbstractDocStoreService.HASH_ALGORITHM);
		messageDigest.update(AbstractDocStoreService
				.getFileTypeEncodingDigestPrime(doc));
		return messageDigest;
	}
}
