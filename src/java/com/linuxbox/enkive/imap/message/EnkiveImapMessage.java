package com.linuxbox.enkive.imap.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.Flags;

import org.apache.james.mailbox.store.mail.model.AbstractMessage;
import org.apache.james.mailbox.store.mail.model.Property;
import org.springframework.util.StringUtils;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.retriever.MessageRetrieverService;

public class EnkiveImapMessage extends AbstractMessage<String> {

	Message message;
	long uid;
	String mailboxId = "";
	String messageId;
	MessageRetrieverService retrieverService;

	public EnkiveImapMessage(String messageId,
			MessageRetrieverService retrieverService) {
		this.messageId = messageId;
		this.retrieverService = retrieverService;
	}

	@Override
	public Date getInternalDate() {
		loadMessage();
		return message.getDate();
	}

	@Override
	public String getMailboxId() {
		return mailboxId;
	}

	@Override
	public long getUid() {
		return uid;
	}

	@Override
	public void setUid(long uid) {
		this.uid = uid;

	}

	@Override
	public void setModSeq(long modSeq) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getModSeq() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isAnswered() {
		return false;
	}

	@Override
	public boolean isDeleted() {
		return false;
	}

	@Override
	public boolean isDraft() {
		return false;
	}

	@Override
	public boolean isFlagged() {
		return false;
	}

	@Override
	public boolean isRecent() {
		return false;
	}

	@Override
	public boolean isSeen() {
		return true;
	}

	@Override
	public void setFlags(Flags flags) {

	}

	@Override
	public InputStream getBodyContent() throws IOException {
		// TODO Actually return body?
		//Leverage parse message function from maildir implementation?
		return new ByteArrayInputStream("".getBytes());

	}

	@Override
	public InputStream getFullContent() throws IOException {
		loadMessage();
		return new ByteArrayInputStream(message.getReconstitutedEmail()
				.getBytes());
	}

	@Override
	public String getMediaType() {
		return "";
	}

	@Override
	public String getSubType() {
		return "";
	}

	@Override
	public long getFullContentOctets() {
		loadMessage();
		try {
			return message.getReconstitutedEmail().length();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public Long getTextualLineCount() {
		loadMessage();
		long lineCount = 0;
		try {
			lineCount = StringUtils.countOccurrencesOf(
					message.getReconstitutedEmail(), "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lineCount;
	}

	@Override
	public InputStream getHeaderContent() throws IOException {
		loadMessage();
		return new ByteArrayInputStream(message.getOriginalHeaders().getBytes());
	}

	@Override
	public List<Property> getProperties() {
		// TODO Auto-generated method stub
		ArrayList<Property> properties = new ArrayList<Property>();

		return properties;
	}

	@Override
	protected int getBodyStartOctet() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	private void loadMessage() {
		if (message == null)
			try {
				message = retrieverService.retrieve(messageId);
			} catch (CannotRetrieveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
