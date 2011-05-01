package com.linuxbox.util;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {
	public static <T> List<T> listFromArray(T[] array) {
		ArrayList<T> result = new ArrayList<T>(array.length);
		for (T element : array) {
			result.add(element);
		}
		return result;
	}
}
