package com.linuxbox.enkive.statistics;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/*
 * NOAH: is this class well-named? It seems to store more than keys; it also stores the consolidation methods. I'm not sure about this, but as I read it I get the sense that it's defining consolidations. Is this also used if there are no consolidations?.
 * Also, there's no documentation about the String that the constructor takes.
 */

public class KeyDef {
	private LinkedList<String> key;
	private Collection<String> methods;

	public KeyDef(String keyPath) {
		parseAll(keyPath);
	}

	public LinkedList<String> getKey() {
		return this.key;
	}

	public Collection<String> getMethods() {
		return this.methods;
	}

	private void parseAll(String str) {
		String[] temp = str.split(":");
		key = new LinkedList<String>(Arrays.asList(temp[0].split("\\.")));
		if (temp.length == 2) {
			methods = Arrays.asList(temp[1].split(","));
		} else {
			methods = null;
		}
	}
}
