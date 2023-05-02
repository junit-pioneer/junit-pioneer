/*
 * Copyright 2016-2022 the original author or authors.
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

abstract class StdOutputStream extends OutputStream {

	private final StringWriter writer = new StringWriter();

	public StdOutputStream() {
		// recreate default constructor to prevent compiler warning
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
	 * @return the lines that were written to {@code System.out} or {@code System.err}
	 */
	public String[] capturedLines() {
		return writer.toString().split(StdIoExtension.SEPARATOR);
	}

}
