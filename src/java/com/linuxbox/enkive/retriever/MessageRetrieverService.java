/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
 * 
 * This file is part of Enkive CE (Community Edition).
 * 
 * Enkive CE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Enkive CE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public
 * License along with Enkive CE. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.linuxbox.enkive.retriever;

import java.util.Collection;
import java.util.List;

import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.EncodedContentData;
import com.linuxbox.enkive.message.Message;
import com.linuxbox.enkive.message.MessageSummary;

public interface MessageRetrieverService {
	/**
	 * 
	 * @param messageId
	 * @return
	 * @throws CannotRetrieveException
	 */
	public MessageSummary retrieveSummary(String messageId)
			throws CannotRetrieveException;

	/**
	 * 
	 * @param messageIds
	 * @return
	 * @throws CannotRetrieveException
	 */
	public List<MessageSummary> retrieveSummary(Collection<String> messageIds)
			throws CannotRetrieveException;

	/**
	 * Returns a re-constituted message given the message UUID.
	 * 
	 * 
	 * @param messageUUID
	 * @return a re-constituted message
	 */
	public Message retrieve(String messageUUID) throws CannotRetrieveException;

	EncodedContentData retrieveAttachment(String attachmentUUID)
			throws CannotRetrieveException;

}
