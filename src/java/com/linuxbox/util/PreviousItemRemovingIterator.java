/*******************************************************************************
 * Copyright 2012 The Linux Box Corporation.
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

public class PreviousItemRemovingIterator<E> implements Iterator<E> {

	Iterator<E> iterator;
	boolean firstCall;

	public PreviousItemRemovingIterator(Iterator<E> iterator) {
		this.iterator = iterator;
		firstCall = true;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public E next() {
		E item = iterator.next();
		if (!firstCall)
			remove();
		else
			firstCall = false;
		return item;
	}

	@Override
	public void remove() {
		iterator.remove();
	}

}
