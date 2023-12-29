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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestClass;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethod;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethodWithParameterTypes;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;
import org.junitpioneer.testkit.ExecutionResults;

/**
 * Shakespeare's Sonnet VII is in the public domain.
 */
@DisplayName("StdIoExtension ")
public class StdIoExtensionTests {

	private final BasicCommandLineApp app = new BasicCommandLineApp();

	private final static PrintStream STDOUT = System.out;
	private final static PrintStream STDERR = System.err;
	private final static InputStream STDIN = System.in;

	@Nested
	@DisplayName("with standard configuration ")
	class StdIoTests {

		@Test
		@StdIo
		@DisplayName("catches the output on the standard out")
		void catchesOut(StdOut out) {
			app.write();

			assertThat(out.capturedString())
					.isEqualTo(linesAsString("Lo! in the orient when the gracious light",
						"Lifts up his burning head, each under eye"));
			assertThat(out.capturedLines())
					.containsExactly("Lo! in the orient when the gracious light",
						"Lifts up his burning head, each under eye");
		}

		@Test
		@StdIo
		@DisplayName("catches no output if none was written")
		void catchesEmptyOut(StdOut out) {
			// don't write anything

			assertThat(out.capturedString()).isEqualTo("");
			assertThat(out.capturedLines()).isEmpty();
		}

		@Test
		@StdIo
		@DisplayName("catches an empty line on the standard out")
		void catchesEmptyLine(StdOut out) {
			app.writeEmptyLine();

			assertThat(out.capturedString()).isEqualTo(StdIoExtension.SEPARATOR);
			assertThat(out.capturedLines()).containsExactly("");
		}

		@Test
		@StdIo
		@DisplayName("catches the output on the standard out, including trailing/preceding empty lines")
		void catchesOutWithEmptyLines(StdOut out) {
			app.writeWithEmptyLines();

			assertThat(out.capturedString())
					.isEqualTo(linesAsString("", "", "Lo! in the orient when the gracious light",
						"Lifts up his burning head, each under eye", "", ""));
			assertThat(out.capturedLines())
					.containsExactly("", "", "Lo! in the orient when the gracious light",
						"Lifts up his burning head, each under eye", "", "");
		}

		@Test
		@StdIo
		@DisplayName("catches the output on the standard out, including a line that doesn't end with a newline")
		void catchesOutWithoutTrailingNewline(StdOut out) {
			app.writeWithoutNewline();

			assertThat(out.capturedString())
					.isEqualTo(linesAsStringWithoutTrailingNewline("Lo! in the orient when the gracious light",
						"Lifts up his burning head, each under eye"));
			assertThat(out.capturedLines())
					.containsExactly("Lo! in the orient when the gracious light",
						"Lifts up his burning head, each under eye");
		}

		@Test
		@ComposedIo
		@DisplayName("works if StdIo is a meta-annotation")
		void catchesOutFromMeta(StdOut out) {
			app.write();

			assertThat(out.capturedLines())
					.containsExactly("Lo! in the orient when the gracious light",
						"Lifts up his burning head, each under eye");
		}

		@Test
		@StdIo
		@DisplayName("catches the output on the standard err")
		void catchesErr(StdErr err) {
			app.writeErr();

			assertThat(err.capturedString())
					.isEqualTo(linesAsString("Lo! in the orient when the gracious light",
						"Lifts up his burning head, each under eye"));
			assertThat(err.capturedLines())
					.containsExactly("Lo! in the orient when the gracious light",
						"Lifts up his burning head, each under eye");
		}

		@Test
		@StdIo({ "Doth homage to his new-appearing sight", "Serving with looks his sacred majesty;" })
		@DisplayName("catches the input from the standard in")
		void catchesIn(StdIn in) throws IOException {
			app.read();

			assertThat(in.capturedString())
					.isEqualTo(linesAsString("Doth homage to his new-appearing sight",
						"Serving with looks his sacred majesty;"));
			assertThat(in.capturedLines())
					.containsExactly("Doth homage to his new-appearing sight",
						"Serving with looks his sacred majesty;");
		}

		@Test
		@StdIo("")
		@DisplayName("catches empty input and reads nothing")
		void catchesEmptyIn(StdIn in) {
			assertThatCode(app::read).doesNotThrowAnyException();
			assertThat(in.capturedString()).isEqualTo(StdIoExtension.SEPARATOR);
			assertThat(in.capturedLines()).containsExactly("");
		}

		@Test
		@StdIo({ "", "", "Doth homage to his new-appearing sight", "Serving with looks his sacred majesty;", "", "" })
		@DisplayName("catches the input from the standard in, including trailing/preceding empty lines")
		void catchesInWithEmptyLines(StdIn in) throws IOException {
			app.read();

			assertThat(in.capturedString())
					.isEqualTo(linesAsString("", "", "Doth homage to his new-appearing sight",
						"Serving with looks his sacred majesty;", "", ""));
			assertThat(in.capturedLines())
					.containsExactly("", "", "Doth homage to his new-appearing sight",
						"Serving with looks his sacred majesty;", "", "");
		}

		@Test
		@StdIo({ "Doth homage to his new-appearing sight", "Serving with looks his sacred majesty;" })
		@DisplayName("for non-empty input, available() reports input's number of bytes")
		void everythingAvailableBeforeRead(StdIn in) throws IOException {
			int inputLength = linesAsString("Doth homage to his new-appearing sight",
				"Serving with looks his sacred majesty;").getBytes().length;

			assertThat(in.available()).isEqualTo(inputLength);
		}

		@Test
		@StdIo({ "Doth homage to his new-appearing sight" })
		@DisplayName("for non-empty input after partial read, available() reports correctly reduced number of bytes")
		void somethingAvailableAfterRead(StdIn in) throws IOException {
			int bytesToRead = 16;
			app.read(bytesToRead);
			int remainingLength = linesAsString("Doth homage to his new-appearing sight").getBytes().length
					- bytesToRead;

			assertThat(in.available()).isEqualTo(remainingLength);
		}

		@Test
		@StdIo("")
		@DisplayName("for empty input, available() returns 0 available bytes")
		void nothingAvailableWhenEmpty(StdIn in) throws IOException {
			assertThat(in.available()).isEqualTo(linesAsString("").length());
		}

		@Test
		@StdIo({ "And having climbed the steep-up heavenly hill,", "Resembling strong youth in his middle age," })
		@DisplayName("catches the input from the standard in and the output on the standard out")
		void catchesBoth(StdIn in, StdOut out) throws IOException {
			app.readAndWrite();

			assertThat(in.capturedLines())
					.containsExactly("And having climbed the steep-up heavenly hill,",
						"Resembling strong youth in his middle age,");
			assertThat(out.capturedLines())
					.containsExactly("Yet mortal looks adore his beauty still,", "Attending on his golden pilgrimage:");
		}

		@Test
		@StdIo({ "But when from highmost pitch, with weary car,", "Like feeble age, he reeleth from the day,",
				"The eyes, 'fore duteous, now converted are" })
		@DisplayName("catches the input from the standard in, even without StdIn parameter")
		void catchesInWithoutParameter() throws IOException {
			app.read();

			assertThat(app.lines)
					.containsExactly("But when from highmost pitch, with weary car,",
						"Like feeble age, he reeleth from the day,", "The eyes, 'fore duteous, now converted are");
		}

		@Test
		@StdIo("")
		@DisplayName("does not catch unrelated parameters")
		void catchesNothing(StdIn in, TestReporter reporter) {
			// if the extension supports `TestReporter`, Jupiter will throw:
			// ParameterResolutionException: Discovered multiple competing ParameterResolvers
		}

	}

	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	@DisplayName("resets the standard in and out ")
	class ResettingTests {

		@Test
		@ReadsStdIo
		@Order(1)
		@DisplayName("1: System.in, System.out and System.err is untouched")
		void untouched() {
			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
			assertThat(System.err).isEqualTo(STDERR);
		}

		@Test
		@StdIo({ "From his low tract, and look another way:", "So thou, thyself outgoing in thy noon" })
		@Order(2)
		@DisplayName("2: System.in, System.out and System.err is redirected")
		void redirected(StdIn in, StdOut out, StdErr err) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			String line = reader.readLine();
			System.out.println(line);
			System.err.println(line);

			// even though `BufferedReader::readLine` was called just once,
			// both lines were read because the reader buffers
			assertThat(in.capturedLines())
					.containsExactlyInAnyOrder("From his low tract, and look another way:",
						"So thou, thyself outgoing in thy noon");
			assertThat(out.capturedLines()).containsExactlyInAnyOrder("From his low tract, and look another way:");
			assertThat(err.capturedLines()).containsExactlyInAnyOrder("From his low tract, and look another way:");

			assertThat(System.in).isNotEqualTo(STDIN);
			assertThat(System.out).isNotEqualTo(STDOUT);
			assertThat(System.err).isNotEqualTo(STDERR);
		}

		@Test
		@ReadsStdIo
		@Order(3)
		@DisplayName("3: System.in, System.out and System.err is reset to their original value")
		void reset() {
			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
			assertThat(System.err).isEqualTo(STDERR);
		}

		@Test
		@StdIo({ "Unlooked on diest unless thou get a son." })
		@Order(4)
		@DisplayName("4: Only System.in is redirected.")
		void redirected_single_in(StdIn in) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			reader.readLine();

			assertThat(in.capturedLines()).containsExactlyInAnyOrder("Unlooked on diest unless thou get a son.");

			assertThat(System.in).isNotEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
		}

		@Test
		@ReadsStdIo
		@Order(5)
		@DisplayName("5: System.in is reset, System.out and System.err is unaffected.")
		void reset_single_in() {
			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
			assertThat(System.err).isEqualTo(STDERR);
		}

		@Test
		@StdIo
		@Order(6)
		@DisplayName("6: Only System.out is redirected.")
		void redirected_single_out(StdOut out) {
			System.out.println("Shakespeare");
			System.out.println("Sonnet VII");

			assertThat(out.capturedLines()).containsExactlyInAnyOrder("Shakespeare", "Sonnet VII");

			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.err).isEqualTo(STDERR);
			assertThat(System.out).isNotEqualTo(STDOUT);
		}

		@Test
		@ReadsStdIo
		@Order(7)
		@DisplayName("7: System.out is reset, System.in and System.err is unaffected.")
		void reset_single_out() {
			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
			assertThat(System.err).isEqualTo(STDERR);
		}

		@Test
		@StdIo
		@Order(8)
		@DisplayName("8: Only System.err is redirected.")
		void redirected_single_err(StdErr err) {
			System.err.println("Mortal beauty");
			System.err.println("Gracious light");

			assertThat(err.capturedLines()).containsExactlyInAnyOrder("Mortal beauty", "Gracious light");

			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
			assertThat(System.err).isNotEqualTo(STDERR);
		}

		@Test
		@ReadsStdIo
		@Order(9)
		@DisplayName("9: System.err is reset, System.in and System.out is unaffected.")
		void reset_single_err() {
			assertThat(System.in).isEqualTo(STDIN);
			assertThat(System.out).isEqualTo(STDOUT);
			assertThat(System.err).isEqualTo(STDERR);
		}

	}

	@Nested
	@DisplayName("when configured ")
	class ConfigurationTests {

		@Test
		@WritesStdIo
		@DisplayName("correctly, no exception is thrown")
		void correctConfigurations() {
			ExecutionResults results = executeTestClass(CorrectConfigurationTestCases.class);

			assertThat(results).hasNumberOfStartedTests(3).hasNumberOfSucceededTests(3);
		}

		@Test
		@DisplayName("without input but StdIn parameter, an exception is thrown")
		void withoutInputWithStdInParameter() {
			ExecutionResults results = executeTestMethodWithParameterTypes(IllegalConfigurationTestCases.class,
				"noInputButStdIn", StdIn.class);

			assertThat(results).hasSingleFailedTest();
		}

		@Test
		@DisplayName("without input and without parameters, an exception is thrown")
		void withoutInputAndWithoutParameters() {
			ExecutionResults results = executeTestMethod(IllegalConfigurationTestCases.class, "noParameterAndNoInput");

			assertThat(results).hasSingleFailedTest();
		}

	}

	static class CorrectConfigurationTestCases {

		@Test
		@StdIo("Redirected output")
		void noParameterButInput() {
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

	static class IllegalConfigurationTestCases {

		@Test
		@StdIo
		void noParameterAndNoInput() {
		}

		@Test
		@StdIo
		void noInputButStdIn(StdIn in) {
		}

	}

	/**
	 * A sample class that I would write tests for.
	 */
	private static class BasicCommandLineApp {

		private final List<String> lines = new ArrayList<>();

		public void write() {
			System.out.print("Lo! in the orient ");
			System.out.println("when the gracious light");
			System.out.println("Lifts up his burning head, each under eye");
		}

		public void writeEmptyLine() {
			System.out.println();
		}

		public void writeWithoutNewline() {
			System.out.print("Lo! in the orient ");
			System.out.println("when the gracious light");
			System.out.print("Lifts up his burning head, each under eye");
		}

		public void writeWithEmptyLines() {
			System.out.println();
			System.out.println();
			System.out.println("Lo! in the orient when the gracious light");
			System.out.println("Lifts up his burning head, each under eye");
			System.out.println();
			System.out.println();
		}

		public void writeErr() {
			System.err.print("Lo! in the orient ");
			System.err.println("when the gracious light");
			System.err.println("Lifts up his burning head, each under eye");
		}

		public void read() throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			reader.lines().forEach(lines::add);
		}

		public void read(int byteCount) throws IOException {
			for (int i = 0; i < byteCount; i++) {
				System.in.read();
			}
		}

		public void readAndWrite() throws IOException {
			var reader = new BufferedReader(new InputStreamReader(System.in));
			lines.add(reader.readLine());
			System.out.println("Yet mortal looks adore his beauty still,");
			lines.add(reader.readLine());
			System.out.println("Attending on his golden pilgrimage:");
		}

	}

	private static String linesAsString(String... lines) {
		return String.join(StdIoExtension.SEPARATOR, lines) + StdIoExtension.SEPARATOR;
	}

	private static String linesAsStringWithoutTrailingNewline(String... lines) {
		return String.join(StdIoExtension.SEPARATOR, lines);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@StdIo
	@interface ComposedIo {
	}

}
