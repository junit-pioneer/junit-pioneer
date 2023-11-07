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

import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;

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
	 * @return the string that was written to {@code System.out} or {@code System.err}
	 */
	public String capturedString() {
		return writer.toString();
	}

	/**
	 * The {@link StdOutputStream#capturedString()}, divided on the line separator.
	 *
	 * <p>This includes leading, inner, and trailing empty lines but does not include the potential empty string
	 * that comes after a trailing line separator. (The exact algorithm is based on but behaves differently
	 * from {@link String#split(String)}.)</p>
	 *
	 * <p>Because the return value does not include a trailing empty line that comes from a trailing line separator,
	 * it can't be used to distinguish the cases where the last line was created by a {@code print} or a
	 * {@code println}. For more details and examples on this, see
	 * <a href="https://junit-pioneer.org/docs/standard-input-output/" target="_top">the documentation on standard input/output</a>.</p>
	 *
	 * @return the lines that were written to {@code System.out} or {@code System.err}
	 */
	public String[] capturedLines() {
		var lines = writer.toString().split(StdIoExtension.SEPARATOR, -1);
		return lines[lines.length - 1].isEmpty() ? Arrays.copyOf(lines, lines.length - 1) : lines;
	}

}
