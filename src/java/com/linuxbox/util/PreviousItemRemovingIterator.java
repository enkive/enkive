/*******************************************************************************
 * Copyright 2015 Enkive, LLC.
 * 
 * This file is part of Enkive CE (Community Edition).
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
 ******************************************************************************/
package com.linuxbox.util;

import java.util.Iterator;

/**
 * The need for this class is uncertain. By its name, it's apparently supposed
 * to remove the item prior to the current item that's retrieved, perhaps to
 * reduce the number of references to items in the collection. As originally
 * implemented, remove could be called twice on an item, once by this code and
 * once by the caller of this code. So some logic has been added to prevent
 * this.
 * 
 * Uncertain if this could be replaced with an iterator that removes an object
 * from underlying collection as soon as it's retrieved here rather than waiting
 * for the NEXT item to be retrieved.
 */
public class PreviousItemRemovingIterator<E> implements Iterator<E> {
	Iterator<E> iterator;
	boolean currentRemoved;

	public PreviousItemRemovingIterator(Iterator<E> iterator) {
		this.iterator = iterator;
		// prevent removal of non-retrieved item
		currentRemoved = true;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public E next() {
		if (!currentRemoved) {
			iterator.remove();
		}
		currentRemoved = false;

		return iterator.next();
	}

	@Override
	public void remove() {
		if (!currentRemoved) {
			currentRemoved = true;
			iterator.remove();
		}
	}
}
