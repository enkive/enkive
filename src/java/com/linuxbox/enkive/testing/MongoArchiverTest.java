package com.linuxbox.enkive.testing;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.james.mime4j.MimeException;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.docstore.mongogrid.ConvenienceMongoGridDocStoreService;
import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.mongodb.Mongo;

public class MongoArchiverTest {
	private final static String DATABASE_NAME = "enkive-test";
	private final static String DOCUMENTS_COLLECTION_NAME = "documents-test";

	private static Mongo m;
	private static int count = 0;
	private static ThreadPoolExecutor threadPool;

	private static DocStoreService docStoreService;

	public static void sendAllFiles(File dir)
			throws CannotTransferMessageContentException, BadMessageException,
			IOException, MimeException, CannotArchiveException {
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					MongoArchiverTestThread archiver = new MongoArchiverTestThread(
							m, file, docStoreService);
					threadPool.execute(archiver);
					count++;
				} else {
					sendAllFiles(file);
				}
			}
		}
	}

	/**
	 * @param args
	 * @throws MimeException
	 * @throws IOException
	 * @throws BadMessageException
	 * @throws CannotTransferMessageContentException
	 * @throws CannotArchiveException
	 * @throws InterruptedException
	 */
	public static void main(String[] args)
			throws CannotTransferMessageContentException, BadMessageException,
			IOException, MimeException, CannotArchiveException,
			InterruptedException, Exception {
		m = new Mongo();

		docStoreService = new ConvenienceMongoGridDocStoreService(m,
				DATABASE_NAME, DOCUMENTS_COLLECTION_NAME);

		ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(
				500000);
		threadPool = new ThreadPoolExecutor(5, 5, 100, TimeUnit.SECONDS, queue);
		long startTime = System.currentTimeMillis();
		File rootDir = new File(
				"/home/lee/Storage/Work/Projects/enkive-2.0/workspace/enkive2/test/data/mime4jTestData");
		sendAllFiles(rootDir);
		threadPool.awaitTermination(100, TimeUnit.MINUTES);
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println(count + " " + elapsedTime / 1000);

		docStoreService.shutdown();
		
		m.close();
	}
}
