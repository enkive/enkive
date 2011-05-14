package com.linuxbox.util.queueservice;

import java.util.Date;

public class AbstractQueueEntry implements QueueEntry {
	private String identifier;
	private Object note;
	private Date enqueuedAt;

	public AbstractQueueEntry(String identifier, Object note, Date enqueuedAt) {
		super();
		this.identifier = identifier;
		this.note = note;
		this.enqueuedAt = enqueuedAt;
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
	public Date getEnqueuedAt() {
		return enqueuedAt;
	}

}
