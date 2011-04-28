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

	public static <F, T> List<T> listFromConvertedArray(F[] array,
			TypeConverter<F, T> converter) throws Exception {
		ArrayList<T> result = new ArrayList<T>(array.length);
		for (F element : array) {
			result.add(converter.convert(element));
		}
		return result;
	}
}
