/*******************************************************************************
 * Copyright 2013 The Linux Box Corporation.
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
package com.linuxbox.enkive.web;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class WebPageInfo {

	final static public String PAGE_POSITION_PARAMETER = "pos";
	final static public String PAGE_SIZE_PARAMETER = "size";
	final static public String PAGE_SORT_BY_PARAMETER = "sortBy";
	final static public String PAGE_SORT_DIR_PARAMETER = "sortDir";

	final static public String FIRST_PAGE = "firstPage";
	final static public String PREVIOUS_PAGE = "previousPage";
	final static public String NEXT_PAGE = "nextPage";
	final static public String LAST_PAGE = "lastPage";
	final static public String PAGE_POSITION = "pagePos";
	final static public String PAGE_SIZE = "pageSize";
	final static public String TOTAL = "total";
	final static public String PAGING_LABEL = "paging";

	final static public int PAGE_SIZE_DEFAULT = 20;

	protected String firstPage;
	protected String previousPage;
	protected String nextPage;
	protected String lastPage;
	protected int pagePos = 1;
	protected int pageSize = PAGE_SIZE_DEFAULT;
	protected int total;
	protected long itemTotal;

	public long getItemTotal() {
		return itemTotal;
	}

	public WebPageInfo() {
		// Just use the defaults
	}

	public WebPageInfo(String pagePos, String pageSize) {
		if (pagePos != null)
			this.pagePos = Integer.parseInt(pagePos);
		if (pageSize != null)
			this.pageSize = Integer.parseInt(pageSize);
	}

	public JSONObject getPageJSON() throws JSONException {

		JSONObject pageInfo = new JSONObject();
		pageInfo.put(FIRST_PAGE, getFirstPage());
		pageInfo.put(PREVIOUS_PAGE, getPreviousPage());
		pageInfo.put(NEXT_PAGE, getNextPage());
		pageInfo.put(LAST_PAGE, getLastPage());
		pageInfo.put(PAGE_POSITION, getPagePos());
		pageInfo.put(PAGE_SIZE, getPageSize());
		pageInfo.put(TOTAL, getTotal());

		return pageInfo;
	}

	public String getFirstPage() {
		if (this.pagePos > 1)
			return PAGE_POSITION_PARAMETER + "=1&" + PAGE_SIZE_PARAMETER + "="
					+ pageSize;
		else
			return null;
	}

	public String getPreviousPage() {
		if (this.pagePos > 1)
			return PAGE_POSITION_PARAMETER + "=" + (this.pagePos - 1) + "&"
					+ PAGE_SIZE_PARAMETER + "=" + this.pageSize;
		else
			return null;
	}

	public String getNextPage() {
		if (pagePos == getTotal())
			return null;
		else
			return PAGE_POSITION_PARAMETER + "=" + (pagePos + 1) + "&"
					+ PAGE_SIZE_PARAMETER + "=" + pageSize;
	}

	public String getLastPage() {
		return PAGE_POSITION_PARAMETER + "=" + getTotal() + "&"
				+ PAGE_SIZE_PARAMETER + "=" + pageSize;
	}

	public int getPagePos() {
		return pagePos;
	}

	public void setPagePos(int pagePos) {
		this.pagePos = pagePos;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotal() {
		if (itemTotal % pageSize == 0)
			total = (int) (itemTotal / pageSize);
		else
			total = (int) ((itemTotal / pageSize) + 1);
		return total;
	}

	public void setItemTotal(long itemTotal) {
		this.itemTotal = itemTotal;
	}

	/*
	 * Helper function to get a sub list of a set of results, used for
	 * pagination
	 */
	public List<?> getSubList(List<?> list) {
		List<?> subList;
		if (((getPagePos() - 1) * getPageSize()) + getPageSize() < list.size())
			subList = list.subList(((getPagePos() - 1) * getPageSize()),
					((getPagePos()) * getPageSize()));
		else
			subList = list.subList(((getPagePos() - 1) * getPageSize()),
					list.size());

		return subList;
	}

}
