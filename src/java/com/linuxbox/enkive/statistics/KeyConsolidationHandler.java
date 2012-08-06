package com.linuxbox.enkive.statistics;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class KeyConsolidationHandler {
	private String humanKey = null;
	private String units = null;
	private LinkedList<String> key;
	private Collection<String> methods;
	
    
	/**
	 * @param keyPath is a string formatted in the following way:
	 * 
	 * "*.*.variableName:consolidationMethodName,consolidationMethodName"
	 * 
	 * the asterisks indicate that the variableName is embedded a level down
	 * the consolidation method names indicate which consolidations to keep for
	 * a given statistic.
	 */
	public KeyConsolidationHandler(String keyPath) {
		parseAll(keyPath);
	}

	public LinkedList<String> getKey() {
		return this.key;
	}

	public Collection<String> getMethods() {
		return this.methods;
	}

	public String getHumanKey(){
		return humanKey;
	}
	
	public String getUnits(){
		return units;
	}
	
	/**
	 * @param str is parsed using the semantics outlined in the constructor's comments 
	 */
	private void parseAll(String str) {
		String[] temp = str.split(":");
		key = new LinkedList<String>(Arrays.asList(temp[0].split("\\.")));
		if (temp[1] != null && !temp[1].equals("")) {
			methods = Arrays.asList(temp[1].split(","));
		} else {
			methods = null;
		}
		if(temp[2] != null && !temp[2].equals("")){
			humanKey=temp[2];
		}
		if(temp.length >= 4 && !temp[3].equals("")){
			units = temp[3];
		}
	}
}
