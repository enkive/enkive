package com.linuxbox.enkive.archiver.mongodb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.apache.james.mime4j.MimeException;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.docstore.mongogrid.MongoGridDocStoreService;
import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;
import com.linuxbox.enkive.retriever.mongodb.MongoRetriever;
import com.mongodb.Mongo;

public class MongoArchiverThreadTest implements Runnable {

	protected MongoArchivingService archiver;
	protected MongoRetriever retriever;
	protected File file;
	
	public MongoArchiverThreadTest(Mongo m, File file){
		archiver = new MongoArchivingService(m, "enkive", "messages");
		MongoGridDocStoreService docStoreService = new MongoGridDocStoreService(m, "enkive", "documents");
		archiver.setDocStoreService(docStoreService);
		
		retriever = new MongoRetriever(m, "enkive", "messages");
		retriever.setDocStoreService(docStoreService);
		this.file = file;
	}
	
	@Override
	public void run() {
		InputStream fileStream;
		try {
			fileStream = new FileInputStream(file);
			Message message = new MessageImpl(fileStream);
			String messageUUID = archiver.storeOrFindMessage(message);
			Thread.sleep(100);
			Message rebuiltMessage = retriever.retrieve(messageUUID);
			OutputStreamWriter out = new OutputStreamWriter(System.out);
			rebuiltMessage.pushReconstitutedEmail(out);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotTransferMessageContentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadMessageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotArchiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotRetrieveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
