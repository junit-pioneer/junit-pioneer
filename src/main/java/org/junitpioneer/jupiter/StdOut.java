/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * <p>For details and examples, see
 * <a href="https://junit-pioneer.org/docs/standard-input-output/" target="_top">the documentation on <code>Standard input/output</code></a>
 * </p>
 *
 * @see StdIo
 */
public class StdOut extends OutputStream {

	private final StringWriter writer = new StringWriter();

	// recreate default constructor to prevent compiler warning
	public StdOut() {
	}

	@Override
	public void write(int i) {
		writer.write(i);
	}

	@Override
	public final void write(byte[] b, int off, int len) {
		writer.write(new String(b, Charset.defaultCharset()), off, len);
	}

	/**
	 * @return the lines that were written to {@code System.out}
	 */
	public String[] capturedLines() {
		return writer.toString().split(StdIoExtension.SEPARATOR);
	}

}
