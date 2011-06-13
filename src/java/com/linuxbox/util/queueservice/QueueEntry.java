package com.linuxbox.util.queueservice;

import java.util.Date;

public interface QueueEntry {
	Date getEnqueuedAt();

	String getIdentifier();

	Object getNote();

	int getShardKey();
}
