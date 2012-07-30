package com.linuxbox.enkive.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * this class holds methods that are useful in the 
 * creation of many common variable types being 
 * used in the statistics package.
 * It exists in order to allow someone to change
 * the datatypes of many variables if another one 
 * is found more suitable.
 */
public abstract class VarsMaker {
	public static Map<String, Object> createMap() {
		return new HashMap<String, Object>();
	}

	public static Map<String, Object> createMapofMap() {
		return new HashMap<String, Object>();
	}	
	
	public static Set<Map<String, Object>> createSet() {
		return new HashSet<Map<String, Object>>();
	}
	
	public static List<Map<String, Object>> createList() {
		return new LinkedList<Map<String, Object>>();
	}
}
