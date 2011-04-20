/*
 *  Copyright 2010 The Linux Box Corporation.
 *
 *  This file is part of Enkive CE (Community Edition).
 *
 *  Enkive CE is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Enkive CE is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public
 *  License along with Enkive CE. If not, see
 *  <http://www.gnu.org/licenses/>.
 */

package com.linuxbox.enkive.message;

/**
 * This depends on commons-codec-1.4.jar for the hex encoding.
 */

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractContentData extends AbstractBaseContentData
		implements ContentData {

	protected Map<String, String> metaData;

	public AbstractContentData() {
		super();
		metaData = new HashMap<String, String>();
	}

	@Override
	public String getMetaDataField(String name) {
		return metaData.get(name);
	}

	@Override
	public void setMetaDataField(String name, String value) {
		metaData.put(name, value);
	}

	@Override
	public Map<String, String> getMetaData() {
		return metaData;
	}

	@Override
	public void setMetaData(Map<String, String> metaData) {
		this.metaData = metaData;
	}

}
