package com.linuxbox.enkive.archiver.mongodb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.MimeException;

import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.exception.BadMessageException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageImpl;
import com.mongodb.Mongo;

public class MongoArchiverThreadTest implements Runnable {

	protected MongoArchivingService archiver;
	protected File file;
	
	public MongoArchiverThreadTest(Mongo m, File file){
		archiver = new MongoArchivingService(m, "enkive", "messages");
		this.file = file;
	}
	
	@Override
	public void run() {
		InputStream fileStream;
		try {
			fileStream = new FileInputStream(file);
			Message message = new MessageImpl(fileStream);
			archiver.storeOrFindMessage(message);
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
		}
		

	}

}
