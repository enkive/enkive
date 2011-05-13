package com.linuxbox.util.queueservice;

import java.util.Date;

public interface QueueEntry {
	String getIdentifier();

	int getNote();

	Date getEnqueuedAt();
}
