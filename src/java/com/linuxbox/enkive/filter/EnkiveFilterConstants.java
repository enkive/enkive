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


package com.linuxbox.enkive.filter;

public class EnkiveFilterConstants {

	static class FilterDefinitionConstants {
		public static final String FILTER = "filter";
		public static final String DEFAULT_ACTION = "defaultAction";
		public static final String DENY = "deny";
		public static final String ENABLED = "enabled";
		public static final String FILTER_TRUE = "true";
		public static final String FILTER_FALSE = "false";
		public static final String ACTION = "action";
		public static final String ALLOW = "allow";
		public static final String HEADER = "header";
		public static final String VALUE = "value";
		public static final String TYPE = "type";
		public static final String INTEGER = "integer";
		public static final String TEXT = "text";
		public static final String ADDRESS = "address";
		public static final String FLOAT = "float";
		public static final String DATE = "date";
		public static final String COMPARISON = "comparison";
		public static final String IS_GREATER_THAN = "is_greater_than";
		public static final String IS_LESS_THAN = "is_less_than";
		public static final String CONTAINS = "contains";
		public static final String DOES_NOT_CONTAIN = "does_not_contain";
		public static final String MATCHES = "matches";
		public static final String DOES_NOT_MATCH = "does_not_match";
	}

	static class FilterAction {
		public static final int ALLOW = 1;
		public static final int DENY = 0;
	}

	static class FilterType {
		public static final int INTEGER = 1;
		public static final int FLOAT = 2;
		public static final int DATE = 3;
		public static final int STRING = 4;
		public static final int ADDRESS = 5;
	}

	static class FilterComparator {
		public static final int IS_GREATER_THAN = 1;
		public static final int IS_LESS_THAN = 2;
		public static final int MATCHES = 3;
		public static final int DOES_NOT_MATCH = 4;
		public static final int CONTAINS = 5;
		public static final int DOES_NOT_CONTAIN = 6;
	}
}
