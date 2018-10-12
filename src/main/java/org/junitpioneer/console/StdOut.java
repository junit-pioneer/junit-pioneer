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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

/**
 * @author smoyer1
 *
 */
public class StdOut extends PrintStream { // CharArrayWriter {

	Charset charset;
	//	InputStream in;
	BufferedReader reader;

	private StdOut(FifoStream fifo, Charset charset) throws UnsupportedEncodingException {
		super(fifo.getOutputStream(), true, charset.displayName());
		reader = new BufferedReader(new InputStreamReader(fifo.getInputStream()));
		this.charset = charset;
	}

	public static StdOut create() throws UnsupportedEncodingException {
		return create(Charset.defaultCharset());
	}

	public static StdOut create(Charset charset) throws UnsupportedEncodingException {
		return new StdOut(new FifoStream(), charset);
	}

	public int read() throws IOException {
		return reader.read();
	}

	public int read(char[] cbuf) throws IOException {
		return reader.read(cbuf);
	}

	public int read(CharBuffer target) throws IOException {
		return reader.read(target);
	}

	public int read(char[] cbuf, int off, int len) throws IOException {
		return reader.read(cbuf, off, len);
	}

	public String readLine() throws IOException {
		return reader.readLine();
	}

}
