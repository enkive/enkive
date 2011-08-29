package com.linuxbox.util.queueservice;

import java.util.Date;

public class AbstractQueueEntry implements QueueEntry {
	private Date enqueuedAt;
	private String identifier;
	private Object note;
	private int shardKey;

	public AbstractQueueEntry(Date enqueuedAt, String identifier, Object note,
			int shardKey) {
		super();
		this.enqueuedAt = enqueuedAt;
		this.identifier = identifier;
		this.note = note;
		this.shardKey = shardKey;
	}

	@Override
	public Date getEnqueuedAt() {
		return enqueuedAt;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public Object getNote() {
		return note;
	}

	@Override
	public int getShardKey() {
		return shardKey;
	}
}
