package com.linuxbox.enkive.statistics;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

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
