package com.linuxbox.enkive.message.search.mongodb;

import static com.linuxbox.enkive.archiver.mongodb.MongoMessageStoreConstants.ATTACHMENT_ID_LIST;
import static com.linuxbox.enkive.search.Constants.CONTENT_PARAMETER;

import java.util.List;
import java.util.Map;

import com.linuxbox.enkive.docsearch.DocSearchQueryService;
import com.linuxbox.enkive.docsearch.exception.DocSearchException;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ContentQueryBuilder extends AbstractMongoMessageQueryBuilder {
	protected final DocSearchQueryService docSearchService;

	public ContentQueryBuilder(DocSearchQueryService docSearchService) {
		this.docSearchService = docSearchService;
	}

	@Override
	public DBObject buildQueryPortion(Map<String, String> fields)
			throws EmptySearchResultsException, MessageSearchException {
		String contentStr = fields.get(CONTENT_PARAMETER);
		if (null == contentStr) {
			return null;
		}

		contentStr = contentStr.trim();
		if (contentStr.isEmpty()) {
			return null;
		}

		try {
			List<String> attachmentIds = docSearchService.search(contentStr);
			if (attachmentIds.isEmpty()) {
				throw new EmptySearchResultsException(
						"The content search found no matching message bodies or attachments.");
			}

			BasicDBList attachmentQuery = new BasicDBList();
			for (String attachmentId : attachmentIds) {
				attachmentQuery.add(new BasicDBObject(ATTACHMENT_ID_LIST,
						attachmentId));
			}

			BasicDBObject result = new BasicDBObject();
			result.put("$or", attachmentQuery);

			return result;
		} catch (DocSearchException e) {
			throw new MessageSearchException(
					"Exception occurred searching message content for \""
							+ contentStr + "\".", e);
		}
	}
}
