/*
 * Copyright 2015-2020 the original author or authors.
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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * <p>For details and examples, see
 * <a href="https://junit-pioneer.org/docs/standard-input-output/" target="_top">the documentation on <code>Standard input/output</code></a>
 * </p>
 *
 * @see StdIo
 */
public class StdIn extends InputStream {

	private final Reader reader;
	private final Writer writer = new StringWriter();

	public StdIn(String[] values) {
		reader = new StringReader(String.join(StdIoExtension.SEPARATOR, values));
	}

	@Override
	public int read() throws IOException {
		int reading = reader.read();
		if (reading != -1) {
			writer.write(reading);
		}
		return reading;
	}

	/**
	 * @return the lines that were read from {@code System.in}; note that buffering readers may read all lines eagerly
	 */
	public String[] capturedLines() {
		return writer.toString().split(StdIoExtension.SEPARATOR);
	}

}
