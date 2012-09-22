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
