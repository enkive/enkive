package com.linuxbox.enkive.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.EncodedContentData;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.retriever.MessageRetrieverService;


import static com.linuxbox.enkive.web.WebConstants.DATA_TAG;


import org.json.*;


public class MessageAttachmentDetailServlet extends EnkiveServlet {
	private static final long serialVersionUID = 7489338160172966335L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		
		
		final String messageId = req.getParameter("message_id");
		final MessageRetrieverService retriever = getMessageRetrieverService();
		
	
		try{
			final Message message = retriever.retrieve(messageId);
			// Sample final Message message = retriever.retrieve("11925f0f19fe215f308d683131eeb7288d7c054d");
			
			JSONArray attachments = new JSONArray();
			
		
			for(String attachmentUUID :message.getContentHeader().getAttachmentUUIDs()){
				
				EncodedContentData attachment = retriever.retrieveAttachment(attachmentUUID);
				JSONObject attachmentObject = new JSONObject();
				
				String filename = attachment.getFilename();
				if (attachment.getFilename() == null || attachment.getFilename().isEmpty()){
					filename = "Message Body";
				}
				
				try {
					attachmentObject.put("filename", filename);
					attachmentObject.put("UUID", attachmentUUID);
					attachments.put(attachmentObject);
						
				JSONObject jObject = new JSONObject();
				jObject.put(WebConstants.DATA_TAG, attachments);
				String jsonString = jObject.toString();
				resp.getWriter().write(jsonString);
				} catch (JSONException e) {
					throw new CannotRetrieveException(
							"could not create JSON for message attachment", e);
				}
			}
			
		}catch(CannotRetrieveException e){
			LOGGER.error("Could not retrieve attachment");	
		
		}
	}

}