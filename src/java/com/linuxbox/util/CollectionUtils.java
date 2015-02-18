/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
 *
 * This file is part of Enkive CE (Community Edition).
 *
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
 *******************************************************************************/
package com.linuxbox.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.linuxbox.enkive.exception.UnimplementedMethodException;

public class CollectionUtils {
	/**
	 * A simple interface to filter items. The filter can change, reject, or
	 * accept as-is items. If it returns null, the item is rejected. Or it can
	 * returned the item passed in or another item to take its place.
	 * 
	 * @author ivancich
	 * 
	 * @param <T>
	 */
	public static interface ItemFilter<T> {
		T doFilter(T input);
	}

	/**
	 * A class that implements ItemFilter that accepts everything as is. Can be
	 * used as a default filter.
	 * 
	 * @author ivancich
	 * 
	 * @param <T>
	 */
	public static class AcceptAllFilter<T> implements ItemFilter<T> {
		public T doFilter(T input) {
			return input;
		}
	}

	/**
	 * Returns a list of elements from an array as processed by the filter.
	 * 
	 * @param <T>
	 * @param array
	 * @param filter
	 * @return
	 */
	public static <T> List<T> listFromArray(T[] array, ItemFilter<T> filter) {
		ArrayList<T> result = new ArrayList<T>(array.length);
		for (T element : array) {
			final T filteredElement = filter.doFilter(element);
			if (filteredElement != null) {
				result.add(filteredElement);
			}
		}
		return result;
	}

	/**
	 * Returns a list of leements from an array. No filter is used (or actually
	 * an "accept everything as-is" filter is used).
	 * 
	 * @param <T>
	 * @param array
	 * @return
	 */
	public static <T> List<T> listFromArray(T[] array) {
		return listFromArray(array, new AcceptAllFilter<T>());
	}

	public static <F, T> List<T> listFromConvertedArray(F[] array,
			TypeConverter<F, T> converter) throws Exception {
		ArrayList<T> result = new ArrayList<T>(array.length);
		for (F element : array) {
			result.add(converter.convert(element));
		}
		return result;
	}
	
	public static <F,T> void addAllMapped(Collection<? super T> destination, Collection<? extends F> source, Mapper<? super F, ? extends T> mapper) {
		for (T newItem : mapAndIterate(source, mapper)) {
			destination.add(newItem);
		}
	}

	public static <F, T> Iterable<T> mapAndIterate(Collection<? extends F> from, final Mapper<? super F,? extends T> mapper) {
		final Collection<F> KeptCollection = Collections.unmodifiableCollection(from);
		
		return new Iterable<T>(){
			@Override
			public Iterator<T> iterator() {
				final Iterator<F> keptIterator = KeptCollection.iterator();

				return new Iterator<T>() {
					@Override
					public boolean hasNext() {
						return keptIterator.hasNext();
					}

					@Override
					public T next() {
						return mapper.map(keptIterator.next());
					}

					@Override
					public void remove() {
						throw new UnimplementedMethodException();
					}
				};
			}
		};
	}

	public static interface Mapper<F, T> {
		T map(F input);
	}
	
}
