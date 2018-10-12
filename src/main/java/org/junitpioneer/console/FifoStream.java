/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Provides an OutputStream that's effectively piped to an InputStream through a
 * FIFO holding the I/O bytes. The OutputStream returned by this class is
 * non-blocking and will return an EOF when no bytes are available.
 */
public class FifoStream {

	private class FifoInputStream extends InputStream {

		Deque<Byte> fifo;

		public FifoInputStream(Deque<Byte> fifo) {
			this.fifo = fifo;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.InputStream#available()
		 */
		@Override
		public int available() throws IOException {
			return fifo.size();
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.InputStream#markSupported()
		 */

		@Override
		public boolean markSupported() {
			return false;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.InputStream#read()
		 */
		@Override
		public int read() throws IOException {
			int b;
			try {
				b = fifo.removeFirst();
			}
			catch (NoSuchElementException e) {
				b = -1;
			}
			return b;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.InputStream#reset()
		 */
		@Override
		public synchronized void reset() throws IOException {
			throw new IOException("Reset is not supported");
		}

	}

	private class FifoOutputStream extends OutputStream {

		Deque<Byte> fifo;

		public FifoOutputStream(Deque<Byte> fifo) {
			this.fifo = fifo;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.OutputStream#write(int)
		 */
		@Override
		public void write(int b) throws IOException {
			fifo.addLast((byte) b);
		}

	}

	Deque<Byte> fifo = new ArrayDeque<>();

	/**
	 * Gets an InputStream attached to the internal (byte) FIFO.
	 *
	 * @return The InputStream.
	 */
	public InputStream getInputStream() {
		return new FifoInputStream(fifo);
	}

	/**
	 * Gets an OutputStream attached to the internal (byte) FIFO.
	 *
	 * @return The OutputStream.
	 */
	public OutputStream getOutputStream() {
		return new FifoOutputStream(fifo);
	}

}
