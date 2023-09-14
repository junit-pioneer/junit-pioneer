/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * <p>For details and examples, see
 * <a href="https://junit-pioneer.org/docs/standard-input-output/" target="_top">the documentation on <code>Standard input/output</code></a>
 * </p>
 *
 * @see StdIo
 */
public class StdIn extends InputStream {

	private final StringReader reader;
	private final StringWriter writer = new StringWriter();

	/**
	 * Keeps track of number of bytes that are still available to {@link InputStream#read() read()}.
	 */
	private int availableBytes;

	public StdIn(String[] lines) {
		// console input is always newline-terminated, so append a line separator to every line
		var mockedInput = String.join(StdIoExtension.SEPARATOR, lines) + StdIoExtension.SEPARATOR;
		reader = new StringReader(mockedInput);
		availableBytes = mockedInput.getBytes().length;
	}

	@Override
	public int available() throws IOException {
		return availableBytes;
	}

	@Override
	public int read() throws IOException {
		int reading = reader.read();
		if (reading != -1) {
			availableBytes--;
			writer.write(reading);
		}
		return reading;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException { // NOSONAR - this is fine for a simple testing extension
		return super.read(b, off, len);
	}

	/**
	 * @return the string that was read from {@code System.in}; note that buffering readers may read all lines eagerly
	 */
	public String capturedString() {
		return writer.toString();
	}

	/**
	 * @return the lines that were read from {@code System.in}; note that buffering readers may read all lines eagerly
	 */
	public String[] capturedLines() {
		var lines = writer.toString().split(StdIoExtension.SEPARATOR, -1);
		return lines[lines.length - 1].isEmpty() ? Arrays.copyOf(lines, lines.length - 1) : lines;
	}

}
