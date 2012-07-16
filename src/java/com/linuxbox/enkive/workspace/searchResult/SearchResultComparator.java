package com.linuxbox.enkive.workspace.searchResult;

import java.util.Comparator;

public class SearchResultComparator implements Comparator<SearchResult> {

	String sortField;
	int sortDir;
	
	public SearchResultComparator(String sortField, int sortDir){
		this.sortField = sortField;
		this.sortDir = sortDir;
	}
	
	@Override
	public int compare(SearchResult o1, SearchResult o2) {
		//If we're sorting by status, sort by that first, then date
		int result = 0;
		
		if (sortField.equals(SearchResult.SORTBYSTATUS)) {
			result = o1.getStatus().compareTo(o2.getStatus());
		}
		
		if (sortField.equals(SearchResult.SORTBYDATE) || result == 0) {
			if(o1.getTimestamp().after(o2.getTimestamp()))
				result = -1;
			else if(o1.getTimestamp().before(o2.getTimestamp()))
				result = 1;
		}
		
		return result * sortDir;
	}
	
}
