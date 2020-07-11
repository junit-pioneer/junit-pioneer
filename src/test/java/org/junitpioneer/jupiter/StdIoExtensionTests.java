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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junitpioneer.jupiter.StdIOExtension.StdIn;
import org.junitpioneer.jupiter.StdIOExtension.StdOut;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

/**
 * Shakespeare's Sonnet VII is in the public domain.
 */
@DisplayName("StdIOExtension ")
public class StdIoExtensionTests {

	final BasicCommandLineApp app = new BasicCommandLineApp();

	@Nested
	@DisplayName("with specific configuration ")
	class ConfigurationTests {

		@Test
		@DisplayName("fails if the parameter type is not StdIn or StdOut")
		void needsType() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(StdIOExtensionConfigurations.class, "badType",
						Boolean.class.getName());

			assertThat(results.firstFailuresThrowable())
					.isInstanceOf(ParameterResolutionException.class)
					.hasMessageContaining("No ParameterResolver registered");
		}

		@Test
		@DisplayName("fails if the parameter is StdIn but test method is not annotated with @StdInSource")
		void needsAnnotation() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(StdIOExtensionConfigurations.class, "noAnnotation",
						StdIn.class.getName());

			assertThat(results.firstFailuresThrowable())
					.isInstanceOf(ParameterResolutionException.class)
					.hasMessageContainingAll("Can not resolve test method parameter", "Method has to be annotated");
		}

		@Test
		@DisplayName("resolves parameter for type StdIn and annotation")
		void goodConfig_stdIn() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(StdIOExtensionConfigurations.class, "resolveStdIn",
						StdIn.class.getName());
			assertThat(results.numberOfStartedTests()).isGreaterThan(0);
		}

		@Test
		@DisplayName("resolves parameter for type StdOut")
		void goodConfig_stdOut() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(StdIOExtensionConfigurations.class, "resolveStdOut",
						StdOut.class.getName());
			assertThat(results.numberOfStartedTests()).isGreaterThan(0);
		}

	}

	@Nested
	@ExtendWith(StdIOExtension.class)
	class ExtendWithTests {

		@Test
		@DisplayName("catches the output on the standard out as lines")
		void catchesOut(StdOut out) {
			app.write();

			assertThat(out.capturedLines())
					.containsExactly("Lo! in the orient when the gracious light",
						"Lifts up his burning head, each under eye");
		}

		@Test
		@DisplayName("catches the input from the standard in")
		@StdInSource({ "Doth homage to his new-appearing sight", "Serving with looks his sacred majesty;" })
		void catchesIn(StdIn in) throws IOException {
			app.read();

			assertThat(in.capturedLines())
					.containsExactly("Doth homage to his new-appearing sight",
						"Serving with looks his sacred majesty;");
		}

		@Test
		@DisplayName("catches the input from the standard in and the output on the standard out")
		@StdInSource({ "And having climbed the steep-up heavenly hill,", "Resembling strong youth in his middle age," })
		void catchesBoth(StdIn in, StdOut out) throws IOException {
			app.readAndWrite();

			assertThat(in.capturedLines())
					.containsExactly("And having climbed the steep-up heavenly hill,",
						"Resembling strong youth in his middle age,");
			assertThat(out.capturedLines())
					.containsExactly("Yet mortal looks adore his beauty still,", "Attending on his golden pilgrimage:");
		}

	}

	@ExtendWith(StdIOExtension.class)
	static class StdIOExtensionConfigurations {

		@Test
		void badType(Boolean b) {
		}

		@Test
		void noAnnotation(StdIn in) {
		}

		@Test
		@StdInSource("value")
		void resolveStdIn(StdIn in) {
		}

		@Test
		void resolveStdOut(StdOut out) {
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
			System.out.println(read);
			read = reader.readLine();
			System.out.println(read);
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
