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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.CharBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author smoyer1
 *
 */
public class SharedBufferTests {

	PrintStream out = System.out;

	StdOut stdOut;

	@BeforeEach
	void setUp() throws UnsupportedEncodingException {
		stdOut = StdOut.create();
		System.setOut(stdOut);
	}

	@Test
	void testThatWritesAndReadsCanBeInterwoven() throws IOException {
		String expected = "This is a test";
		System.out.println(expected);
		assertThat(stdOut.readLine()).isEqualTo(expected);

		expected = "This is a different test";
		System.out.println(expected);
		assertThat(stdOut.readLine()).isEqualTo(expected);
	}

	@Test
	void testThatMultipleLinesAreBuffered() throws IOException {
		String expected1 = "Now is the time for all";
		String expected2 = "good men to come to the";
		String expected3 = "aid of their country";

		System.out.println(expected1);
		System.out.println(expected2);
		System.out.println(expected3);

		assertThat(stdOut.readLine()).isEqualTo(expected1);
		assertThat(stdOut.readLine()).isEqualTo(expected2);
		assertThat(stdOut.readLine()).isEqualTo(expected3);
	}

	@Test
	void testThatReadWithAnEmptyFifoReturnsMinusOne() throws IOException {
		assertThat(stdOut.read()).isEqualTo(-1);
	}

	@Test
	void testThatReadIntoCharBufferWithAnEmptyFifoReturnsMinusOne() throws IOException {
		CharBuffer target = CharBuffer.allocate(5);
		assertThat(stdOut.read(target)).isEqualTo(-1);
	}

	@Test
	void testThatReadIntoCharArrayWithAnEmptyFifoReturnsMinusOne() throws IOException {
		char[] cbuf = new char[5];
		assertThat(stdOut.read(cbuf)).isEqualTo(-1);
	}

	@Test
	void testThatReadIntoPortionOfCharArrayWithAnEmptyFifoReturnsMinusOne() throws IOException {
		char[] cbuf = new char[5];
		assertThat(stdOut.read(cbuf, 2, 2)).isEqualTo(-1);
	}

	@Test
	void testThatReadLineWithAnEmptyFifoReturnsNull() throws IOException {
		assertThat(stdOut.readLine()).isNull();
	}

}
