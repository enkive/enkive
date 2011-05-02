package com.linuxbox.enkive.archiver.mongodb;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.james.mime4j.MimeException;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreService;
import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.mongodb.Mongo;

public class MongoArchiverTest {

	public static Mongo m;
	public static int count = 0;
	public static ThreadPoolExecutor threadPool;
	
	
	public static void sendAllFiles(File dir) throws CannotTransferMessageContentException, BadMessageException, IOException, MimeException, CannotArchiveException{
		if (dir.isDirectory() && count < 5) {
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					MongoArchiverThreadTest archiver = new MongoArchiverThreadTest(m, file);
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
	public static void main(String[] args) throws CannotTransferMessageContentException, BadMessageException, IOException, MimeException, CannotArchiveException, InterruptedException {
		
		m = new Mongo();
		ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue(500000);
		threadPool = new ThreadPoolExecutor(5, 5, 100, TimeUnit.SECONDS, queue);
		long startTime = System.currentTimeMillis();
		File rootDir = new File("/home/lee/Storage/Work/Projects/Enkive/TestData/enron/maildir");
		sendAllFiles(rootDir);
		threadPool.awaitTermination(100, TimeUnit.MINUTES);
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println(count + " " + elapsedTime/1000);
		m.close();

	}

}
