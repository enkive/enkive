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
 ******************************************************************************/


package com.linuxbox.enkive.message;

import java.io.IOException;
import java.util.List;

/**
 * This class represents a multi-part MIME header. It will include information
 * about the boundary and so forth. Each part of the this multipart header will
 * be stored as a SinglePartHeader.
 * 
 * @author eric
 * 
 *         Note: The implementation of ContentHeader's getContentData method
 *         should return a reference to the data before the first multi-part
 *         boundary. Often this will be content that contains,
 *         "This is a message with multiple parts in MIME format.", and this
 *         content will likely be shared by many messages (given the planned
 *         de-duping functionality of the archiver).
 */
public interface MultiPartHeader extends ContentHeader {
	/**
	 * 
	 * @return the boundary string. So if there were a header such as,
	 *         'Content-Type: multipart/mixed; boundary="frontier"', "frontier"
	 *         would be returned.
	 */
	public String getBoundary();

	/**
	 * Sets the boundary String.
	 * 
	 * @param boundary
	 *            The boundary String.
	 */
	public void setBoundary(String boundary);

	/**
	 * Sets the preamble
	 * 
	 * @param preamble
	 */
	public void setPreamble(String preamble);

	/**
	 * @return the preamble
	 * 
	 */
	public String getPreamble();

	/**
	 * Sets the epilogue
	 * 
	 * @param epilogue
	 */
	public void setEpilogue(String epilogue);

	/**
	 * @return the epilogue
	 * 
	 */
	public String getEpilogue();

	/**
	 * 
	 * @return list of SinglePartHeader. Must be a list since order is important
	 *         and must be maintained when re-constituting the original email
	 *         message.
	 */
	public List<ContentHeader> getPartHeaders();

	/**
	 * 
	 * Sets the partHeaders attribute
	 * 
	 * @param partHeaders
	 */
	public void setPartHeaders(List<ContentHeader> partHeaders);

	/**
	 * Adds a PartHeader to this MultipartHeader.
	 * 
	 * @param header
	 *            The ContentHeader to add.
	 */
	public void addPartHeader(ContentHeader header);

	/**
	 * @return a String that contains all parts of this MultipartHeader.
	 * @throws IOException
	 * 
	 */
	public String printMultiPartHeader() throws IOException;
}
