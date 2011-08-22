package com.linuxbox.enkive.web;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.linuxbox.enkive.message.EncodedContentData;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.retriever.MessageRetrieverService;

public class AttachmentRetrieveServlet extends EnkiveServlet {
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
					
				
			for(String attachmentUUID :message.getContentHeader().getAttachmentUUIDs()){
				
				EncodedContentData attachment = retriever.retrieveAttachment(attachmentUUID);
				
				String filename = attachment.getFilename();
				if (attachment.getFilename() == null || attachment.getFilename().isEmpty()){
					filename = "Message Body";
				}else{
					try {
						filename = "filename=" + attachment.getFilename();
						resp.setContentType(attachment.getMimeType());
						resp.setCharacterEncoding("utf-8");
						resp.setHeader("Content-disposition", "attachment;  " + filename);
						attachment.transferBinaryContent(resp.getOutputStream());
					} catch (CannotTransferMessageContentException e) {
						LOGGER.error("error transferring attachment message " + messageId, e);
						resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
								"error transferring attachment message " + messageId
										+ "; see server logs");
					}
				}
			}
		
		}catch(CannotRetrieveException e){
			LOGGER.error("error retrieving message " + messageId, e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"error retrieving message " + messageId
							+ "; see server logs");
			
		}
    }
}