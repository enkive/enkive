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
 *******************************************************************************/
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
