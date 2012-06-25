package com.linuxbox.enkive.statistics;

import java.util.Collection;
import java.util.Arrays;
import java.util.List;

public class KeyDef {
	private List<String> key;
	private Collection<String> methods;
	
	public KeyDef( String keyPath){
		parseAll(keyPath);
	}
	
	private void parseAll(String str){
		String[] temp = str.split(":");
		key = Arrays.asList(temp[0].split("\\."));
		if(temp.length == 2){
			methods = Arrays.asList(temp[1].split(","));
		}
		else{
			methods = null;
		}		
	}
	
	public List<String> getKey(){
		return this.key;
	}
	
	public Collection<String> getMethods(){
		return this.methods;
	}
}
