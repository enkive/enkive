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
import com.linuxbox.util.mongodb.Dropper;
import com.mongodb.gridfs.GridFSFile;

public class MongoGridDocStoreServiceTest {
	private final static String DB_NAME = "enkive-test";
	private final static String GRID_FS_COLLECTION = "fs-test";

	static final String testString = "to be or not to be; that is the \u20AC 1 question";
	static final byte[] testData = testString
			.getBytes(Constants.PREFERRED_CHARSET);
	static Document testDocument;
	static byte[] testDocumentHash;

	static ConvenienceMongoGridDocStoreService docStoreService;

	@BeforeClass
	public static void setUpClass() throws Exception {
		docStoreService = new ConvenienceMongoGridDocStoreService(DB_NAME,
				GRID_FS_COLLECTION);
		docStoreService.startup();

		testDocument = new StringDocument(testString, "text/plain", "txt",
				MimeUtil.ENC_7BIT);

		MessageDigest messageDigest = getPrimedMessageDigest(testDocument);
		messageDigest.update(testData);
		testDocumentHash = messageDigest.digest();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		docStoreService.shutdown();
		Dropper.dropDatabase(DB_NAME);
	}

	/*
	 * @Before public void setUp() throws Exception { }
	 * 
	 * @After public void tearDown() throws Exception { }
	 */

	@Test
	public void testStoreKnownNameFullLength() throws Exception {
		byte[] hash = generateFakeRandomHash();

		StoreRequestResult result = docStoreService.storeKnownHash(
				testDocument, hash, testData, testData.length);
		assertFalse(result.getAlreadyStored());

		Document outDoc = docStoreService.retrieve(result.getIdentifier());

		BufferedReader r = new BufferedReader(new InputStreamReader(
				outDoc.getEncodedContentStream(), Constants.PREFERRED_CHARSET));

		assertEquals(testString, r.readLine());
		assertNull(r.readLine());

		r.close();

		docStoreService.gridFS.remove(result.getIdentifier());
	}

	@Test
	public void testStoreKnownNamePartialLength() throws Exception {
		final int length = testString.length() / 2;
		byte[] hash = generateFakeRandomHash();

		StoreRequestResult result = docStoreService.storeKnownHash(
				testDocument, hash, testData, length);
		assertFalse(result.getAlreadyStored());

		Document outDoc = docStoreService.retrieve(result.getIdentifier());

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

		docStoreService.gridFS.remove(result.getIdentifier());
	}

	@Test
	public void testIndexing() throws Exception {
		byte[] hash = generateFakeRandomHash();

		StoreRequestResult result = docStoreService.storeKnownHash(
				testDocument, hash, testData, testData.length);
		final String identifier = result.getIdentifier();

		GridFSFile gFile1 = docStoreService.gridFS.findOne(identifier);
		assertEquals("a new file is initially UNINDEXED",
				(Integer) MongoGridDocStoreService.STATUS_UNINDEXED,
				(Integer) gFile1.getMetaData().get(Constants.INDEX_STATUS_KEY));

		final String nextUnindexedId1 = docStoreService.nextUnindexed();
		assertEquals(identifier, nextUnindexedId1);

		GridFSFile gFile2 = docStoreService.gridFS.findOne(identifier);
		assertEquals(
				"once a file has been retrieved as unindexed, it should not be in INDEXING state",
				(Integer) MongoGridDocStoreService.STATUS_INDEXING,
				(Integer) gFile2.getMetaData().get(Constants.INDEX_STATUS_KEY));

		final String nextUnindexedId2 = docStoreService.nextUnindexed();
		assertNull(nextUnindexedId2);

		docStoreService.markAsIndexed(identifier);
		GridFSFile gFile3 = docStoreService.gridFS.findOne(identifier);
		assertEquals(
				"now that a file is marked as indexed, it should appear as INDEXED",
				(Integer) MongoGridDocStoreService.STATUS_INDEXED,
				(Integer) gFile3.getMetaData().get(Constants.INDEX_STATUS_KEY));

		docStoreService.gridFS.remove(identifier);
	}

	@Test
	public void testStoreAndDetermineName() throws Exception {
		HashingInputStream in = new HashingInputStream(
				getPrimedMessageDigest(testDocument), new ByteArrayInputStream(
						testData));

		StoreRequestResult result = docStoreService.storeAndDetermineHash(
				testDocument, in);

		assertFalse(result.getAlreadyStored());

		Document outDoc = docStoreService.retrieve(result.getIdentifier());

		BufferedReader r = new BufferedReader(new InputStreamReader(
				outDoc.getEncodedContentStream(), Constants.PREFERRED_CHARSET));

		assertEquals(testString, r.readLine());
		assertNull(r.readLine());

		r.close();

		docStoreService.gridFS.remove(result.getIdentifier());
	}

	@Test
	public void testStoreKnownNameWithExistingDocument() throws Exception {
		StoreRequestResult result1 = docStoreService.storeKnownHash(
				testDocument, testDocumentHash, testData, testData.length);

		assertFalse(result1.getAlreadyStored());

		HashingInputStream doc1Input = new HashingInputStream(
				getPrimedMessageDigest(testDocument), new ByteArrayInputStream(
						testData));

		StoreRequestResult result2 = docStoreService.storeAndDetermineHash(
				testDocument, doc1Input);

		assertTrue(result2.getAlreadyStored());

		assertEquals("identifiers should be the same", result1.getIdentifier(),
				result2.getIdentifier());

		docStoreService.gridFS.remove(result1.getIdentifier());
	}

	@Test
	public void testStoreAndDetermineNameWithExistingDocument()
			throws Exception {
		HashingInputStream doc1Input = new HashingInputStream(
				getPrimedMessageDigest(testDocument), new ByteArrayInputStream(
						testData));

		StoreRequestResult result1 = docStoreService.storeAndDetermineHash(
				testDocument, doc1Input);

		assertFalse(result1.getAlreadyStored());

		StoreRequestResult result2 = docStoreService.storeKnownHash(
				testDocument, testDocumentHash, testData, testData.length);

		assertTrue(result2.getAlreadyStored());

		assertEquals("identifiers should be the same", result1.getIdentifier(),
				result2.getIdentifier());

		docStoreService.gridFS.remove(result1.getIdentifier());
	}

	@Test
	public void testIndexingUsingShardKeys() throws Exception {
		Assert.fail("unimplemented");
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

	private static byte[] generateFakeRandomHash() {
		final byte[] result = new byte[20];
		final UUID u = UUID.randomUUID();

		// transfer bits into array

		long bits = u.getMostSignificantBits();
		for (int i = 0; i < 8; i++) {
			result[i] = (byte) (bits & 0xff);
		}

		bits = u.getLeastSignificantBits();
		for (int i = 8; i < 16; i++) {
			result[i] = (byte) (bits & 0xff);
		}

		// remaining bytes should be 0s

		return result;
	}
}
