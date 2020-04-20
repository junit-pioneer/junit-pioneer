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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junitpioneer.jupiter.StdIOExtension.StdIn;
import org.junitpioneer.jupiter.StdIOExtension.StdOut;

@ExtendWith(StdIOExtension.class)
@DisplayName("StdIOExtension ")
public class StdIoExtensionTests extends AbstractJupiterTestEngineTests {

	final BasicCommandLineApp app = new BasicCommandLineApp();

	@Nested
	@DisplayName("with specific configuration ")
	class ConfigurationTests {

		@Test
		@DisplayName("fails if parameter is not annotated with @StdIntercept")
		void needsAnnotation() {
			try {
				executeTestsForMethod(StdIOExtensionConfigurationTests.class, "noAnnotation");
			}
			catch (ParameterResolutionException ex) {
				assertThat(ex.getMessage()).startsWith("No ParameterResolver registered");
			}
		}

		@Test
		@DisplayName("fails if the parameter type is not StdIn or StdOut")
		void needsType() {
			try {
				executeTestsForMethod(StdIOExtensionConfigurationTests.class, "badType");
			}
			catch (ParameterResolutionException ex) {
				assertThat(ex.getMessage()).contains("Can only resolve parameter", "but was");
			}
		}

		@Test
		@DisplayName("resolves parameter for correct type and annotation")
		void goodConfig() {
			Assertions
					.assertDoesNotThrow(
						() -> executeTestsForMethod(StdIOExtensionConfigurationTests.class, "goodConfig"));
		}

	}

	@Test
	@DisplayName("catches the output on the standard out")
	void catchesOut(@StdIntercept StdOut out) {
		app.write();

		assertThat(out.capturedLines()).containsExactly("Hello", "World!");
	}

	@Test
	@DisplayName("catches the input from the standard in")
	void catchesIn(@StdIntercept({ "Hello", "World!" }) StdIn in) throws IOException {
		app.read();

		assertThat(in.capturedLines()).containsExactly("Hello", "World!");
	}

	private static class StdIOExtensionConfigurationTests {

		@Test
		void noAnnotation(StdIn in) {
		}

		@Test
		void badType(@StdIntercept Boolean b) {
		}

		@Test
		void goodConfig(@StdIntercept StdIn in) {
		}

	}

	/**
	 * A sample class that I would write tests for.
	 */
	private static class BasicCommandLineApp {

		public void write() {
			System.out.println("Hello");
			System.out.println("World!");
		}

		public void read() throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String read = reader.readLine();
			System.out.println(read);
			read = reader.readLine();
			System.out.println(read);
		}

	}

}
