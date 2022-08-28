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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class StdInOutExtensionDemo {

	// tag::stdio_stdin_replace_input[]
	@Test
	@StdIo({ "Hello", "World" })
	void stdinReplaceInput() {
		// `System.in` is replaced and the code under
		// test reads lines "Hello" and "World"
	}
	// end::stdio_stdin_replace_input[]

	// tag::stdio_stdin_replace__and_verify_input[]
	@Test
	@StdIo({ "Hello", "World" })
	void stdinReplaceAndVerify(StdIn in) {
		// `System.in` is replaced, the code under
		// test reads lines "Hello" and "World",
		// and `StdIn` can be used to verify that
	}
	// end::stdio_stdin_replace__and_verify_input[]

	// tag::stdio_stdin_not_replaced_but_stdout[]
	@Test
	@StdIo
	void stdinNotReplacedButStdout(StdOut out) {
		// `System.out` is replaced, so the written lines
		// are captured and can be verified with `StdOut`
	}
	// end::stdio_stdin_not_replaced_but_stdout[]

	// tag::stdio_both_replaced[]
	@Test
	@StdIo()
	void bothReplaced(StdOut out) {
		// `System.in` and `System.out` are replaced
		// and written lines can be verified with `StdOut`
	}
	// end::stdio_both_replaced[]

	// tag::stdio_both_replaced_and_verify[]
	@Test
	@StdIo({ "Hello", "World" })
	void bothReplaceAndVerify(StdIn in, StdOut out) {
		// `System.in` is replaced, the code under
		// test reads lines "Hello" and "World",
		// and `StdIn` can be used to verify that;
		// `System.out` is also replaced, so the
		// written lines can be verified with `StdOut`
	}
	// end::stdio_both_replaced_and_verify[]

	// tag::stdio_edge_cases_ExampleConsoleReader[]
	class ConsoleReader {

		private List<String> lines = new ArrayList<>();

		public void readLines() throws IOException {
			InputStreamReader is = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(is);
			for (int i = 0; i < 2; i++) {
				String line = reader.readLine();
				lines.add(line);
			}
		}

	}
	// end::stdio_edge_cases_ExampleConsoleReader[]

	// tag::stdio_edge_cases_ConsoleReaderTest[]
	class ConsoleReaderTest {

		@Test
		@StdIo({ "line1", "line2", "line3" })
		void testReadLines(StdIn in) throws IOException {
			ConsoleReader consoleReader = new ConsoleReader();

			consoleReader.readLines();

			String[] lines = in.capturedLines();

			// This is failing
			// assertEquals(lines, "line1", "line2");

			// This is passing
			// assertEquals(lines, "line1", "line2", "line3");
		}

	}
	// end::stdio_edge_cases_ConsoleReaderTest[]

}
