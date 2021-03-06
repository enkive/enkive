/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
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
package com.linuxbox.enkive.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;

import com.linuxbox.enkive.exception.CannotTransferMessageContentException;
import com.linuxbox.enkive.exception.EnkiveRuntimeException;

/*
 * TODO
 *  
 * This class no longer needs to do any hash calculation. The back-end is now responsible for figuring out
 * the unique name for the document. Also, this class reads everything into a byte array, likely only to
 * calculate the hash. If possible, if this can keep track of an InputStream for the document and possibly
 * the MIME type, file extension, and encoding type (base64, quoted printable, 7bit, 8bit, binary) that
 * would be sufficient.   
 */

public abstract class AbstractBaseContentData implements BaseContentData {
	private static final String HASH_ALGORITHM = "SHA-1";

	protected byte[] data;
	protected byte[] hashBytes;
	protected String hashString;

	public AbstractBaseContentData() {
		super();
		data = null;
		clearHash();
	}

	@Override
	public InputStream getBinaryContent() throws ContentException {
		return new ByteArrayInputStream(data);
	}

	public byte[] getSha1() {
		// only calculate hash when requested
		if (hashBytes == null) {
			calculateHash();
		}
		return hashBytes;
	}

	@Override
	public String getSha1String() {
		// only calculate hash when requested
		if (hashString == null) {
			calculateHash();
		}
		return hashString;
	}

	@Override
	public void setBinaryContent(InputStream contentStream)
			throws CannotTransferMessageContentException {
		clearHash();
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			IOUtils.copy(contentStream, output);
			IOUtils.closeQuietly(contentStream);
			IOUtils.closeQuietly(output);
			data = output.toByteArray();
		} catch (IOException e) {
			throw new CannotTransferMessageContentException(e);
		}
	}

	@Override
	public void setBinaryContent(String content, Charset encoding) {
		clearHash();
		data = content.getBytes(encoding);
	}

	@Override
	public void setByteContent(byte[] content) {
		clearHash();
		data = content;
	}

	protected void clearHash() {
		hashBytes = null;
		hashString = null;
	}

	protected void calculateHash() {
		// if there's no data then there's no hash
		if (data == null) {
			clearHash();
			return;
		}

		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new EnkiveRuntimeException(e.getMessage());
		}

		md.update(data);

		hashBytes = md.digest();
		hashString = new String((new Hex()).encode(hashBytes));
	}
}
