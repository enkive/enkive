package com.linuxbox.enkive.statistics.gathering;

import java.util.Date;

import com.linuxbox.enkive.message.search.MessageSearchService;
import com.linuxbox.enkive.message.search.exception.MessageSearchException;

public interface GathererMessageSearchService extends MessageSearchService {

	public int getNumberOfMessages(Date startDate, Date endDate)
			throws MessageSearchException;

}
