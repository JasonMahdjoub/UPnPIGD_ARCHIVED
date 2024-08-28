package com.distrimind.upnp_igd.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Christian Bauer
 */
public class Iterators {

	/**
	 * A default implementation with no elements.
	 */
	static public class Empty<E> implements Iterator<E> {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public E next() {
			throw new NoSuchElementException();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * A fixed single element.
	 */
	static public class Singular<E> implements Iterator<E> {

		final protected E element;
		protected int current;

		public Singular(E element) {
			this.element = element;
		}

		@Override
		public boolean hasNext() {
			return current == 0;
		}

		@Override
		public E next() {
			current++;
			return element;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Wraps a collection and provides stable iteration with thread-safe removal.
	 * <p>
	 * Internally uses the iterator of a <code>CopyOnWriteArrayList</code>, when
	 * <code>remove()</code> is called, delegates to {@link #synchronizedRemove(int)}.
	 * </p>
	 */
	static public abstract class Synchronized<E> implements Iterator<E> {

		final Iterator<E> wrapped;

		int nextIndex = 0;
		boolean removedCurrent = false;

		public Synchronized(Collection<E> collection) {
			this.wrapped = new CopyOnWriteArrayList<>(collection).iterator();
		}

		@Override
		public boolean hasNext() {
			return wrapped.hasNext();
		}

		@Override
		public E next() {
			removedCurrent = false;
			nextIndex++;
			return wrapped.next();
		}

		@Override
		public void remove() {
			if (nextIndex == 0)
				throw new IllegalStateException("Call next() first");
			if (removedCurrent)
				throw new IllegalStateException("Already removed current, call next()");
			synchronizedRemove(nextIndex-1);
			removedCurrent = true;
		}

		/**
		 * Must remove the element at the given index from the original collection in a
		 * thread-safe fashion.
		 */
		abstract protected void synchronizedRemove(int index);
	}

}
