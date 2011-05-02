package com.linuxbox.enkive.archiver.mongodb;

import java.util.ArrayList;
import java.util.List;

import static com.linuxbox.enkive.archiver.MesssageAttributeConstants.*;

import com.linuxbox.enkive.archiver.AbstractMessageArchivingService;
import com.linuxbox.enkive.archiver.exceptions.CannotArchiveException;
import com.linuxbox.enkive.docstore.Document;
import com.linuxbox.enkive.docstore.StoreRequestResult;
import com.linuxbox.enkive.docstore.exception.DocStoreException;
import com.linuxbox.enkive.message.ContentHeader;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MimeTransferEncoding;
import com.linuxbox.enkive.message.MultiPartHeader;
import com.linuxbox.enkive.message.SinglePartHeader;
import com.linuxbox.enkive.message.docstore.ContentDataDocument;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

public class MongoArchivingService extends AbstractMessageArchivingService {

	public static String ATTACHMENT_ID = "attachment_id";
	public static String ATTACHMENT_ID_LIST = "attachment_ids";
	protected Mongo m = null;
	protected DB messageDb;
	protected DBCollection messageColl;
	protected List<String> attachment_ids;
	
	public MongoArchivingService(Mongo m, String dbName, String collName) {
		this.m = m;
		try {
			messageDb = m.getDB(dbName);
			messageColl = messageDb.getCollection(collName);
		} catch (MongoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String storeMessage(Message message) throws CannotArchiveException {
		String messageUUID = null;
		attachment_ids = new ArrayList<String>();
		try {
			BasicDBObject messageObject = new BasicDBObject();
			messageObject.put("_id", calculateMessageId(message));
			messageObject.put(ORIGINAL_HEADERS, message.getOriginalHeaders());
			messageObject.put(MAIL_FROM, message.getMailFrom());
			messageObject.put(RCPT_TO, message.getRcptTo());
			messageObject.put(FROM, message.getFrom());
			messageObject.put(TO, message.getTo());
			messageObject.put(CC, message.getCc());
			messageObject.put(DATE, message.getDate());
			messageObject.put(SUBJECT, message.getSubject());
			messageObject.put(MESSAGE_ID, message.getMessageId());
			messageObject.put(MIME_VERSION, message.getMimeVersion());
			
			ContentHeader contentHeader = message.getContentHeader();
			
			messageObject.put("ContentHeader", archiveContentHeader(contentHeader));
			messageObject.put(ATTACHMENT_ID_LIST, attachment_ids);
			//TODO store list of all attached file UUIDs
			messageColl.insert(messageObject);
			messageUUID = messageObject.getString("_id");
		} catch (MongoException e) {
			throw new CannotArchiveException(e);
		} catch (DocStoreException e) {
			throw new CannotArchiveException(e);
		}
		return messageUUID;
	}

	@Override
	public String findMessage(Message message) {
		//TODO How do we want to determine duplicate messages?
		return null;
	}

	private BasicDBObject archiveContentHeader(ContentHeader contentHeader) throws DocStoreException{
		BasicDBObject headerObject = new BasicDBObject();
		// If we've got a multipartheader, add all of the single content_headers
		// If there's a nested multipartheader we can call this recursively and
		// still save the order in which things have been archived
		if (contentHeader.isMultipart()) {
			MultiPartHeader multiPartHeader = (MultiPartHeader)contentHeader;
			headerObject.put("type", "MultiPartHeader");
			headerObject.put(BOUNDARY_ID,
					multiPartHeader.getBoundary());
			headerObject.put(PREAMBLE,
					multiPartHeader.getPreamble());
			headerObject.put(EPILOGUE,
					multiPartHeader.getEpilogue());
			headerObject.put(ORIGINAL_HEADERS, multiPartHeader.getOriginalHeaders());
			List<ContentHeader> partHeaders = ((MultiPartHeader) contentHeader)
					.getPartHeaders();
			ArrayList<BasicDBObject> partHeadersList= new ArrayList<BasicDBObject>(); 
			for (ContentHeader partHeader : partHeaders) {
				partHeadersList.add(archiveContentHeader(partHeader));
			}
			headerObject.put("partHeaders", partHeadersList);

		} else {
			SinglePartHeader singlePartHeader = (SinglePartHeader)contentHeader;
			headerObject.put("type", "SinglePartHeader");
			headerObject.put(CONTENT_ID,
					singlePartHeader.getContentID());
			headerObject.put(CONTENT_TYPE,
					singlePartHeader.getContentType());
			headerObject.put(CONTENT_DISPOSITION,
					singlePartHeader.getContentDisposition());
			if (singlePartHeader.getFilename() != null)
				headerObject.put(FILENAME,
						singlePartHeader.getFilename());

			MimeTransferEncoding mtf = singlePartHeader.getContentTransferEncoding();
			if (mtf != null) {
				headerObject.put(CONTENT_TRANSFER_ENCODING,
						mtf.toString());
			}
			
			headerObject.put(ORIGINAL_HEADERS,
					singlePartHeader.getOriginalHeaders());
			
			//Store the attachment
			Document document = new ContentDataDocument(singlePartHeader.getEncodedContentData(), singlePartHeader.getContentType());
			StoreRequestResult docResult = docStoreService.store(document);
			headerObject.put(ATTACHMENT_ID, docResult.getIdentifier());
			attachment_ids.add(docResult.getIdentifier());
		}

		return headerObject;
	}
	
	private String calculateMessageId(Message message){
		return message.getCleanMessageId();
	}
}
