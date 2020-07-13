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

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.*;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethod;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethodWithParameterTypes;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junitpioneer.jupiter.StdIoExtension.StdIn;
import org.junitpioneer.jupiter.StdIoExtension.StdOut;
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
	@DisplayName("with specific configuration ")
	class ConfigurationTests {

		@Test
		@DisplayName("fails if the method is annotated but no parameter is passed to the test")
		void needsParameter() {
			ExecutionResults results = executeTestMethod(StdIoExtensionConfigurations.class, "noParameter");

			assertThat(results.firstFailuresThrowable())
					.isInstanceOf(ExtensionConfigurationException.class)
					.hasMessage(format("Method is annotated with %s but no %s or %s parameters were found.",
						StdIo.class.getName(), StdIn.class.getName(), StdOut.class.getName()));
		}

		@Test
		@DisplayName("fails if the parameter type is not StdIn or StdOut")
		void needsType() {
			ExecutionResults results = executeTestMethodWithParameterTypes(StdIoExtensionConfigurations.class,
				"badType", Boolean.class.getName());

			assertThat(results.firstFailuresThrowable())
					.isInstanceOf(ExtensionConfigurationException.class)
					.hasMessage(format("Method is annotated with %s but no %s or %s parameters were found.",
						StdIo.class.getName(), StdIn.class.getName(), StdOut.class.getName()));
		}

		@Test
		@DisplayName("fails if the parameter is StdIn or StdOut but StdIoExtension is not active")
		void needsExtension() {
			ExecutionResults results = executeTestMethodWithParameterTypes(
				NotAnnotatedStdIoExtensionConfiguration.class, "noAnnotation", StdOut.class.getName());

			assertThat(results.firstFailuresThrowable())
					.isInstanceOf(ParameterResolutionException.class)
					.hasMessageStartingWith("No ParameterResolver registered");
		}

		@Test
		@DisplayName("fails if the parameter is StdIn or StdOut but test method is not annotated with @StdIo")
		void needsAnnotation() {
			ExecutionResults results = executeTestMethodWithParameterTypes(StdIoExtensionConfigurations.class,
				"noAnnotation", StdIn.class.getName());

			assertThat(results.firstFailuresThrowable())
					// This is because the class is annotated with @ExtendWith
					.isInstanceOf(ExtensionConfigurationException.class)
					.hasMessage(
						format("StdIoExtension is active but no %s annotation was found.", StdIo.class.getName()));
		}

		@Test
		@DisplayName("fails if the method is annotated and has input sources but no StdIn parameter")
		void needsStdIn() {
			ExecutionResults results = executeTestMethodWithParameterTypes(StdIoExtensionConfigurations.class,
				"noStdIn", StdOut.class.getName());

			assertThat(results.firstFailuresThrowable())
					.isInstanceOf(ExtensionConfigurationException.class)
					.hasMessage(format(
						"Method has no %s parameter but input sources were provided in the %s annotation (Did you forget to add a test parameter?).",
						StdIn.class.getName(), StdIo.class.getName()));
		}

	}

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
		@StdIo
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
		@StdIo({"line1", "line2"})
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

	// We use the @ExtendWith to test that the @StdIo annotation is required in every scenario,
	// essentially ensuring this is considered a wrong configuration.
	@ExtendWith(StdIoExtension.class)
	static class StdIoExtensionConfigurations {

		@Test
		@StdIo
		void noParameter() {
		}

		@Test
		@StdIo
		void badType(Boolean b) {
		}

		@Test
		void noAnnotation(StdIn in) {
		}

		@Test
		@StdIo("value")
		void noStdIn(StdOut out) {
		}

	}

	static class NotAnnotatedStdIoExtensionConfiguration {

		@Test
		void noAnnotation(StdOut out) {
			fail("This should never execute");
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
