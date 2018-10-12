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

import java.util.Scanner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author smoyer1
 *
 */
public class StdInTests {

	StdIn stdIn;

	@BeforeEach
	void setUp() {
		stdIn = new StdIn();
	}

	@Test
	public void testSendWithChar() {
		char expected = 'T';
		stdIn.send(expected);
		Scanner scanner = new Scanner(System.in);
		char actual = scanner.findInLine(".").charAt(0);
		assertThat(actual).isEqualTo(expected);
		scanner.close();
	}

	@Test
	public void testSendWithCharArray() {
		String expected = "Test!";
		stdIn.send(expected.toCharArray());
		Scanner scanner = new Scanner(System.in);
		String actual = scanner.nextLine();
		assertThat(actual).isEqualTo(expected);
		scanner.close();
	}

	/**
	 * Test method for {@link pioneer.stdio.StdIn#send(java.lang.CharSequence)}.
	 */
	@Test
	public void testSendWithCharSequence() {
		String expected = "This is a test";
		stdIn.send(expected);
		Scanner scanner = new Scanner(System.in);
		String actual = scanner.nextLine();
		assertThat(actual).isEqualTo(expected);
		scanner.close();
	}

	/**
	 * Test method for {@link pioneer.stdio.StdIn#send(java.lang.CharSequence)}.
	 */
	@Test
	public void testBufferConcatenation() {
		String[] words = { "five ", "four ", "three ", "two ", "one" };
		for (String word : words) {
			stdIn.send(word);
		}
		String expected = String.join("", words);
		Scanner scanner = new Scanner(System.in);
		String actual = scanner.nextLine();
		assertThat(actual).isEqualTo(expected);
		scanner.close();
	}

}
