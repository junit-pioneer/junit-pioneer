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

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

/**
 * @author smoyer1
 *
 */
public class StdIn {

	static final Charset CHAR_SET = Charset.forName("ISO-8859-1");

	ByteArrayInputStream buffer = new ByteArrayInputStream(new byte[0]);

	public StdIn() {
		send(new byte[0]);
	}

	public int available() {
		return buffer.available();
	}

	public void send(char character) {
		char[] characters = new char[1];
		characters[0] = character;
		send(characters);
	}

	public void send(char[] characters) {
		send(new String(characters));
	}

	public void send(CharSequence characters) {
		send(characters.toString().getBytes(CHAR_SET));
	}

	public void send(byte[] characters) {
		byte[] combined = new byte[buffer.available() + characters.length];
		System.arraycopy(characters, 0, combined, buffer.available(), characters.length);
		buffer.read(combined, 0, buffer.available());
		buffer = new ByteArrayInputStream(combined);
		System.setIn(buffer);
	}

}
