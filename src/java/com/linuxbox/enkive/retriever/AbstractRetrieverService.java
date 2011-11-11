/*
 *  Copyright 2010 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.retriever;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.linuxbox.enkive.docstore.DocStoreService;
import com.linuxbox.enkive.exception.CannotRetrieveException;
import com.linuxbox.enkive.message.MessageSummary;

public abstract class AbstractRetrieverService implements
		MessageRetrieverService {

	protected DocStoreService docStoreService;

	@Override
	public List<MessageSummary> retrieveSummary(Collection<String> messageIds)
			throws CannotRetrieveException {
		List<MessageSummary> result = new ArrayList<MessageSummary>(
				messageIds.size());
		for (String messageId : messageIds) {
			MessageSummary summary = retrieveSummary(messageId);
			if (summary != null)
				result.add(summary);
		}
		return result;
	}

	public DocStoreService getDocStoreService() {
		return docStoreService;
	}

	public void setDocStoreService(DocStoreService docStoreService) {
		this.docStoreService = docStoreService;
	}
}
