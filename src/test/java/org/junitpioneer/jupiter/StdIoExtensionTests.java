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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
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
			ExecutionEventRecorder recorder = executeTestsForMethodWithParameters(StdIOExtensionConfigurations.class,
				"noAnnotation", StdIn.class);

			assertThat(getFirstFailuresThrowable(recorder))
					.isInstanceOf(ParameterResolutionException.class)
					.hasMessageContaining("No ParameterResolver registered for");
		}

		@Test
		@DisplayName("fails if the parameter type is not StdIn or StdOut")
		void needsType() {
			ExecutionEventRecorder recorder = executeTestsForMethodWithParameters(StdIOExtensionConfigurations.class,
				"badType", Boolean.class);

			assertThat(getFirstFailuresThrowable(recorder))
					.isInstanceOf(ParameterResolutionException.class)
					.hasMessageContaining("Can only resolve parameter of type %s or %s but was:",
						StdOut.class.getName(), StdIn.class.getName());
		}

		@Test
		@DisplayName("resolves parameter for type StdIn and annotation")
		void goodConfig_stdIn() {
			try {
				ExecutionEventRecorder recorder = executeTestsForMethodWithParameters(
					StdIOExtensionConfigurations.class, "resolveStdIn", StdIn.class);
				assertThat(recorder.getTestStartedCount()).isGreaterThan(0);
			}
			catch (Throwable ignored) {
				fail();
			}
		}

		@Test
		@DisplayName("resolves parameter for type StdOut and annotation")
		void goodConfig_stdOut() {
			try {
				ExecutionEventRecorder recorder = executeTestsForMethodWithParameters(
					StdIOExtensionConfigurations.class, "resolveStdOut", StdOut.class);
				assertThat(recorder.getTestStartedCount()).isGreaterThan(0);
			}
			catch (Throwable ignored) {
				fail();
			}
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

	@ExtendWith(StdIOExtension.class)
	static class StdIOExtensionConfigurations {

		@Test
		void noAnnotation(StdIn in) {
		}

		@Test
		void badType(@StdIntercept Boolean b) {
		}

		@Test
		void resolveStdIn(@StdIntercept StdIn in) {
		}

		@Test
		void resolveStdOut(@StdIntercept StdOut out) {
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
