package com.linuxbox.enkive.statistics;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class ConsolidationKeyHandler {
	private String humanKey = null;
	private String units = null;
	private LinkedList<String> key;
	private Collection<String> methods;
	private boolean isPoint;

	/**
	 * @param keyPath
	 *            is a string containing the following information:
	 * 
	 *            The path to the data (ie. *.*.totalMemory) The consolidation
	 *            methods !separated by commas! (ie. max,min,avg) NOTE: methods
	 *            can be left blank, but that should only be used for variables
	 *            that DO NOT need consolidation (ie. collection name) The Human
	 *            Readable Name (ie. Total RAM Memory Used) The Units
	 *            (Optional): (ie. bytes) The statistic type (ie. point)
	 * 
	 *            separated by colons ':' and formatted like so:
	 * 
	 *            <path to the data>:<consolidation methods>:<human readable
	 *            name>:<units>:<statistic type>
	 * 
	 *            Example:
	 *            "*.*.TotalMemory:max,min,avg:Total RAM Memory Used:Bytes:point"
	 * 
	 *            The asterisks in the path to the data indicate that the
	 *            variableName is embedded a level farther down in the Map. The
	 *            consolidation method names indicate which consolidations to
	 *            keep for this statistic.
	 */
	public ConsolidationKeyHandler(String keyPath) {
		parseAll(keyPath);
	}

	/**
	 * @return a linked list that defines a path to the data
	 */
	public LinkedList<String> getKey() {
		return this.key;
	}

	/**
	 * @return a colletion containing strings that define which consolidation(s)
	 *         to do for this statistic
	 */
	public Collection<String> getMethods() {
		return this.methods;
	}

	/**
	 * 
	 * @return the human readable name of this data
	 */
	public String getHumanKey() {
		return humanKey;
	}

	/**
	 * 
	 * @return a string for the units of this statistic
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * 
	 * @return true if this statistic is a point statistic
	 */
	public boolean isPoint() {
		return isPoint;
	}

	/**
	 * @param str
	 *            is parsed to define the following information:
	 * 
	 *            The path to the data (ie. *.*.totalMemory) The consolidation
	 *            methods !separated by commas! (ie. max,min,avg) The Human
	 *            Readable Name (ie. Total RAM Memory Used) The Units
	 *            (Optional): (ie. bytes) The statistic type (ie. point)
	 * 
	 *            using the semantics outlined in the constructor's comments
	 */
	private void parseAll(String str) {
		String[] temp = str.split(":");
		key = new LinkedList<String>(Arrays.asList(temp[0].split("\\.")));
		if (temp[1] != null && !temp[1].equals("")) {
			methods = Arrays.asList(temp[1].split(","));
			if (temp[4] != null && !temp[4].equals("")) {// will only apply to
															// stats with
															// methods
				String type = temp[4];
				if (type.equals("point")) {
					isPoint = true;
				} else {
					isPoint = false;
				}
			}
		} else {
			methods = null;
		}
		if (temp[2] != null && !temp[2].equals("")) {
			humanKey = temp[2];
		}
		if (temp.length >= 4 && !temp[3].equals("")) {
			units = temp[3];
		}
	}
}
