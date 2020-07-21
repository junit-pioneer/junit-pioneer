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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestClass;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethodWithParameterTypes;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junitpioneer.testkit.ExecutionResults;

/**
 * Shakespeare's Sonnet VII is in the public domain.
 */
@DisplayName("StdIoExtension ")
public class StdIoExtensionTests {

	private final BasicCommandLineApp app = new BasicCommandLineApp();

	private final static PrintStream STDOUT = System.out;
	private final static InputStream STDIN = System.in;

	@Nested
	@DisplayName("with standard configuration ")
	class StdIoTests {

		@Test
		@DisplayName("catches the output on the standard out as lines")
		@StdIo
		void catchesOut(StdOut out) {
			app.write();

			assertThat(out.capturedLines())
					.containsExactly("Lo! in the orient when the gracious light",
						"Lifts up his burning head, each under eye");
		}

		@Test
		@DisplayName("catches the input from the standard in")
		@StdIo({ "Doth homage to his new-appearing sight", "Serving with looks his sacred majesty;" })
		void catchesIn(StdIn in) throws IOException {
			app.read();

			assertThat(in.capturedLines())
					.containsExactly("Doth homage to his new-appearing sight",
						"Serving with looks his sacred majesty;");
		}

		@Test
		@DisplayName("catches empty input and reads nothing")
		@StdIo("")
		void catchesNothing(StdIn in) {
			assertThatCode(app::read).doesNotThrowAnyException();
			assertThat(in.capturedLines()).containsExactly("");
		}

		@Test
		@DisplayName("catches the input from the standard in and the output on the standard out")
		@StdIo({ "And having climbed the steep-up heavenly hill,", "Resembling strong youth in his middle age," })
		void catchesBoth(StdIn in, StdOut out) throws IOException {
			app.readAndWrite();

			assertThat(in.capturedLines())
					.containsExactly("And having climbed the steep-up heavenly hill,",
						"Resembling strong youth in his middle age,");
			assertThat(out.capturedLines())
					.containsExactly("Yet mortal looks adore his beauty still,", "Attending on his golden pilgrimage:");
		}

	}

	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	@DisplayName("resets the standard in and out ")
	class ResettingTests {

		@Test
		@DisplayName("1: System.in and System.out is untouched")
		@Order(1)
		void untouched() {
			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
		}

		@Test
		@DisplayName("2: System.in and System.out is redirected")
		@Order(2)
		@StdIo({ "line1", "line2", "line3" })
		void redirected(StdIn in, StdOut out) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			String line = reader.readLine();
			System.out.println(line);

			// even though `BufferedReader::readLine` was called just once,
			// all three lines were read because the reader buffers
			assertThat(in.capturedLines()).containsExactlyInAnyOrder("line1", "line2", "line3");
			assertThat(out.capturedLines()).containsExactlyInAnyOrder("line1");

			assertThat(System.in).isNotEqualTo(STDIN);
			assertThat(System.out).isNotEqualTo(STDOUT);
		}

		@Test
		@DisplayName("3: System.in and System.out is reset to their original value")
		@Order(3)
		void reset() {
			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
		}

		@Test
		@DisplayName("4: Only System.in is redirected.")
		@Order(4)
		@StdIo({ "line1", "line2" })
		void redirected_single_in(StdIn in) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			reader.readLine();

			assertThat(in.capturedLines()).containsExactlyInAnyOrder("line1", "line2");

			assertThat(System.in).isNotEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
		}

		@Test
		@DisplayName("5: System.in is reset, System.out is unaffected.")
		@Order(5)
		void reset_single_in() {
			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
		}

		@Test
		@DisplayName("6: Only System.out is redirected.")
		@Order(6)
		@StdIo
		void redirected_single_out(StdOut out) {
			System.out.println("line1");
			System.out.println("line2");

			assertThat(out.capturedLines()).containsExactlyInAnyOrder("line1", "line2");

			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.out).isNotEqualTo(STDOUT);
		}

		@Test
		@DisplayName("7: System.out is reset, System.in is unaffected.")
		@Order(7)
		void reset_single_out() {
			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
		}

	}

	@Nested
	@DisplayName("when configured ")
	class ConfigurationTests {

		@Test
		@DisplayName("correctly, no exception is thrown")
		void correctConfigurations() {
			ExecutionResults results = executeTestClass(CorrectConfigurations.class);

			assertThat(results).hasNumberOfStartedTests(3).hasNumberOfSucceededTests(3);
		}

		@Test
		@DisplayName("without input but StdIn parameter, an exception is thrown")
		void withoutInputWithStdInParameter() {
			ExecutionResults results = executeTestMethodWithParameterTypes(IllegalConfigurations.class,
				"noInputButStdIn", "org.junitpioneer.jupiter.StdIn");

			assertThat(results).hasSingleFailedTest();
		}

	}

	static class CorrectConfigurations {

		@Test
		@StdIo
		void noParameter() {
		}

		@Test
		@StdIo
		void noStdIn(StdOut out) {
		}

		@Test
		@StdIo("Hello, World")
		void noStdOut(StdIn in) {
		}

	}

	static class IllegalConfigurations {

		@Test
		@StdIo
		void noInputButStdIn(StdIn in) {
		}

	}

	/**
	 * A sample class that I would write tests for.
	 */
	private static class BasicCommandLineApp {

		public void write() {
			System.out.print("Lo! in the orient ");
			System.out.println("when the gracious light");
			System.out.println("Lifts up his burning head, each under eye");
		}

		public void read() throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String read = reader.readLine();
			read = reader.readLine();
		}

		public void readAndWrite() throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String read = reader.readLine();
			System.out.println("Yet mortal looks adore his beauty still,");
			read = reader.readLine();
			System.out.println("Attending on his golden pilgrimage:");
		}

	}

}
