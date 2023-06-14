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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.parallel.Execution;

/**
 * <p>Verify proper behavior when annotated on a top level class.</p>
 *
 * <p>{@link VerifyEnvVarsExtension} is registered as an extension <em>before</em> {@code RestoreSystemProperties}. It
 * stores the initial environment variables and verifies them at the end.
 */
@DisplayName("RestoreEnvironmentVariables Annotation")
@ExtendWith(RestoreEnvironmentVariablesTests.VerifyEnvVarsExtension.class) // 1st: Order is important here
@RestoreEnvironmentVariables
@TestMethodOrder(OrderAnnotation.class)
@Execution(SAME_THREAD) // Single thread. See VerifyEnvVarsExtension inner class
class RestoreEnvironmentVariablesTests {

	@BeforeAll
	static void globalSetUp() {
		EnvironmentVariableUtils.set("A", "all envar A");
		EnvironmentVariableUtils.set("B", "all envar B");
		EnvironmentVariableUtils.set("C", "all envar C");
	}

	@BeforeEach
	void methodSetUp() {
		EnvironmentVariableUtils.set("M", "each envar M");
		EnvironmentVariableUtils.set("N", "each envar N");
		EnvironmentVariableUtils.set("O", "each envar O");
	}

	@Test
	@Order(1)
	@DisplayName("verify initial state from BeforeAll & BeforeEach and setenv")
	void verifyInitialState() {
		assertThat(System.getenv("A")).isEqualTo("all envar A");
		assertThat(System.getenv("B")).isEqualTo("all envar B");
		assertThat(System.getenv("C")).isEqualTo("all envar C");

		assertThat(System.getenv("M")).isEqualTo("each envar M");
		assertThat(System.getenv("N")).isEqualTo("each envar N");
		assertThat(System.getenv("O")).isEqualTo("each envar O");

		EnvironmentVariableUtils.set("X", "method X");
	}

	@Test
	@Order(2)
	@DisplayName("Envar X from the previous test should have been reset")
	void shouldNotSeeChangesFromPreviousTest() {
		assertThat(System.getenv("X")).isNull();
	}

	@Nested
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@DisplayName("Nested tests should inherit restore behavior and be able to override")
	class NestedTests {

		@BeforeEach
		void methodSetUp() {
			EnvironmentVariableUtils.set("M", "each envar M Nest");
		}

		@Test
		@Order(1)
		@DisplayName("initial state from nested BeforeAll & BeforeEach and setenv")
		void verifyInitialState() {
			assertThat(System.getenv("A")).isEqualTo("all envar A");
			assertThat(System.getenv("B")).isEqualTo("all envar B");
			assertThat(System.getenv("C")).isEqualTo("all envar C");

			assertThat(System.getenv("M")).isEqualTo("each envar M Nest");
			assertThat(System.getenv("N")).isEqualTo("each envar N");
			assertThat(System.getenv("O")).isEqualTo("each envar O");

			EnvironmentVariableUtils.set("X", "method X");
		}

		@Test
		@Order(2)
		@DisplayName("Envar X from the previous test should have been reset")
		void shouldNotSeeChangesFromPreviousTest() {
			assertThat(System.getenv("X")).isNull();
		}

	}

	/**
	 * <p>Extension that checks the before and after state of environment variables.</p>
	 *
	 * <p>Must be registered before {@code RestoreEnvironmentVariables}.
	 * To avoid replicating the system being tested w/ the test itself, this class
	 * uses static state rather than the extension store. As a result, this test
	 * class is marked as single threaded.</p>
	 */
	static class VerifyEnvVarsExtension
			implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

		// Nested tests will push additional copies
		private static final Deque<Map<String, String>> beforeAllState = new ArrayDeque<>();

		// Only one test method happens at a time
		private static Map<String, String> beforeEachState;

		@Override
		public void beforeAll(final ExtensionContext context) throws Exception {
			HashMap<String, String> envVars = new HashMap<>();
			envVars.putAll(System.getenv()); //detached

			beforeAllState.push(envVars);
		}

		@Override
		public void afterAll(final ExtensionContext context) throws Exception {
			Map<String, String> preTest = beforeAllState.pop();
			Map<String, String> actual = System.getenv();

			assertThat(preTest).isNotNull();
			assertThat(actual).containsExactlyInAnyOrderEntriesOf(preTest);
		}

		@Override
		public void beforeEach(final ExtensionContext context) throws Exception {
			Map<String, String> envVars = new HashMap<>();
			envVars.putAll(System.getenv()); //detached

			beforeEachState = envVars;
		}

		@Override
		public void afterEach(final ExtensionContext context) throws Exception {
			Map<String, String> preTest = beforeEachState;
			Map<String, String> actual = System.getenv();

			assertThat(preTest).isNotNull();
			assertThat(actual).containsExactlyInAnyOrderEntriesOf(preTest);
			beforeEachState = null;
		}

	}

}
