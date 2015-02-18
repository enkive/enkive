/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
 * 
 * This file is part of Enkive CE (Community Edition).
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
package com.linuxbox.enkive.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class holds methods that are useful in the creation of many common
 * variable types being used in the statistics package. It exists in order to
 * allow someone to change the datatypes (ie. LinkedList to ArrayList) of many
 * variables if another one is found more suitable.
 */
public class VarsMaker {
	public static Map<String, Object> createMap() {
		return new HashMap<String, Object>();
	}

	public static Map<String, Object> createMap(Map<String, Object> map) {
		return new HashMap<String, Object>(map);
	}

	public static Map<String, Map<String, Object>> createMapofMap() {
		return new HashMap<String, Map<String, Object>>();
	}

	public static Set<Map<String, Object>> createSetOfMaps() {
		return new HashSet<Map<String, Object>>();
	}

	public static Set<Object> createSetOfObjs() {
		return new HashSet<Object>();
	}

	public static List<Map<String, Object>> createListOfMaps() {
		return new LinkedList<Map<String, Object>>();
	}

	public static List<List<Map<String, Object>>> createListOfLists() {
		return new LinkedList<List<Map<String, Object>>>();
	}

	public static LinkedList<String> createLinkedListOfStrs() {
		return new LinkedList<String>();
	}

	public static List<RawStats> createListOfRawStats() {
		return new LinkedList<RawStats>();
	}
}
