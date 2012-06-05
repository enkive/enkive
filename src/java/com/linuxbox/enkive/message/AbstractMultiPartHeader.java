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
package com.linuxbox.enkive.message;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMultiPartHeader extends AbstractContentHeader
		implements MultiPartHeader {
	protected List<ContentHeader> partHeaders;
	protected String boundary;
	protected String preamble;
	protected String epilogue;

	AbstractMultiPartHeader() {
		partHeaders = new ArrayList<ContentHeader>();
	}

	@Override
	public void addPartHeader(ContentHeader header) {
		partHeaders.add(header);
	}

	@Override
	public String getBoundary() {
		return boundary;
	}

	@Override
	public List<ContentHeader> getPartHeaders() {
		return partHeaders;
	}

	@Override
	public void setPartHeaders(List<ContentHeader> partHeaders) {
		this.partHeaders = partHeaders;
	}

	@Override
	public void setBoundary(String boundary) {
		this.boundary = boundary;
	}

	@Override
	public void setPreamble(String preamble) {
		this.preamble = preamble;
	}

	@Override
	public String getPreamble() {
		return preamble;
	}

	@Override
	public String getEpilogue() {
		return epilogue;
	}

	@Override
	public void setEpilogue(String epilogue) {
		this.epilogue = epilogue;
	}

	@Override
	public boolean isMultipart() {
		return true;
	}
}
