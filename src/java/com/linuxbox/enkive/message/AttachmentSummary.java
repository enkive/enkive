package com.linuxbox.enkive.message;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

public class AttachmentSummary {
	protected String uuid;
	protected String fileName;
	protected String mimeType;

	/**
	 * Since attachments can be hierarchical, positions is a list of integers
	 * indicating where in the hierarchy a given attachment sits. For example,
	 * if it were the list {2, 1, 3} then it would be the third sub-attachment,
	 * of the first sub-attachment, of the second attachment. Since this is made
	 * for human consumption, the indices are 1-based rather than 0-based.
	 */
	protected List<Integer> position;

	/** A string version of the position, lazily created. */
	protected String positionString;

	public AttachmentSummary(String uuid, String fileName, String mimeType,
			Deque<Integer> positionAbove) {
		this.uuid = uuid;
		this.fileName = fileName;
		this.mimeType = mimeType;

		this.position = new ArrayList<Integer>(positionAbove.size());
		this.position.addAll(positionAbove);
	}

	public String getUuid() {
		return uuid;
	}

	public String getFileName() {
		return fileName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public List<Integer> getPosition() {
		return position;
	}

	public String getPositionString() {
		if (positionString == null) {
			positionString = generatePositionString();
		}
		return positionString;
	}

	protected String generatePositionString() {
		StringBuilder sb = new StringBuilder();
		Iterator<Integer> i = position.iterator();
		while (i.hasNext()) {
			Integer v = i.next();
			sb.append(v.toString());
			if (i.hasNext()) {
				sb.append('-');
			}
		}
		return sb.toString();
	}
}
