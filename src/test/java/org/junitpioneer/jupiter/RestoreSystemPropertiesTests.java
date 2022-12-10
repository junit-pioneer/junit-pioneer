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

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junitpioneer.testkit.ExecutionResults;

import java.util.ArrayDeque;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethod;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

/**
 * Verify proper behavior when annotated on a top level class
 */
@DisplayName("RestoreSystemProperties Annotation")
@ExtendWith(RestoreSystemPropertiesTests.VerifySysPropsExtension.class)
@RestoreSystemProperties
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Execution(SAME_THREAD)	// Single thread.  See VerifySysPropsExtension inner class
class RestoreSystemPropertiesTests {

	@BeforeAll
	static void globalSetUp() {
		System.setProperty("A", "all sys A");
		System.setProperty("B", "all sys B");

		// Create a new Sys Props using defaults for all values!
		// This crazy structure will need to be restored for each test and replaced
		// with the original props when the test is complete
		Properties orgProps = getAllCurrentEntries();	// Will include A & B from above
		Properties newProps = new Properties(orgProps);

		newProps.setProperty("C", "all sys C");	//	one prop actually set (not a default)

		System.setProperties(newProps);
	}

	@BeforeEach
	void methodSetUp() {
		System.setProperty("M", "each sys M");
		System.setProperty("N", "each sys N");
		System.setProperty("O", "each sys O");
	}

	@Test @Order(1)
	@DisplayName("verify initial state from BeforeAll & BeforeEach and set prop")
	void verifyInitialState() {
		assertThat(System.getProperty("A")).isEqualTo("all sys A");
		assertThat(System.getProperty("B")).isEqualTo("all sys B");
		assertThat(System.getProperty("C")).isEqualTo("all sys C");

		assertThat(System.getProperty("M")).isEqualTo("each sys M");
		assertThat(System.getProperty("N")).isEqualTo("each sys N");
		assertThat(System.getProperty("O")).isEqualTo("each sys O");

		System.setProperty("X", "method X");	// SHOULDN'T BE VISIBLE IN NEXT TEST
	}

	@Test @Order(2)
	@DisplayName("Property X from the previous test should have been reset")
	void shouldNotSeeChangesFromPreviousTest() {
		assertThat(System.getProperty("X")).isNull();
	}

	@Nested @Order(1)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	@DisplayName("Nested tests should inherit restore behavior and be able to override")
	class NestedTestsA {

		@BeforeEach
		void methodSetUp() {
			System.setProperty("M", "each sys M Nest");
		}

		@Test @Order(1)
		@DisplayName("initial state from nested BeforeAll & BeforeEach and set prop")
		void verifyInitialState() {
			assertThat(System.getProperty("A")).isEqualTo("all sys A");
			assertThat(System.getProperty("B")).isEqualTo("all sys B");
			assertThat(System.getProperty("C")).isEqualTo("all sys C");

			assertThat(System.getProperty("M")).isEqualTo("each sys M Nest");
			assertThat(System.getProperty("N")).isEqualTo("each sys N");
			assertThat(System.getProperty("O")).isEqualTo("each sys O");

			System.setProperty("X", "method X");
		}

		@Test @Order(2)
		@DisplayName("Property X from the previous test should have been reset")
		void shouldNotSeeChangesFromPreviousTest() {
			assertThat(System.getProperty("X")).isNull();
		}
	}


	static public Properties getAllCurrentEntries() {

		final Properties current = System.getProperties();
		final Properties clone = new Properties();

		// This will get default values, but doesn't handle non-strings...
		current.propertyNames().asIterator().forEachRemaining(k -> {
			clone.put(k, current.getProperty(k.toString()));
		});

		return clone;
	}
	/**
	 * Extension that checks the before and after state of SysProps.
	 *
	 * Must be programatically registered with low 'Order' to precede extensions under test.
	 * To avoid replicating the system being tested w/ the test itself, this doesn't
	 * use the extension store, it just uses static state.  As a result, this test
	 * class is marked as single threaded.
	 */
	protected static class VerifySysPropsExtension implements BeforeEachCallback, AfterEachCallback, BeforeAllCallback, AfterAllCallback {

		/* Nested tests will push additional copies */
		private static ArrayDeque<Properties> beforeAllState = new ArrayDeque<>();

		/* Only one test method happens at a time */
		private static Properties beforeEachState;

		@Override
		public void beforeAll(final ExtensionContext context) throws Exception {
			beforeAllState.push(getAllCurrentEntries());
		}

		@Override
		public void afterAll(final ExtensionContext context) throws Exception {
			Properties preTest = beforeAllState.pop();
			Properties actual = System.getProperties();

			assertThat(preTest).isNotNull();
			assertThat(actual).containsExactlyInAnyOrderEntriesOf(preTest);
		}

		@Override
		public void beforeEach(final ExtensionContext context) throws Exception {
			beforeEachState = getAllCurrentEntries();
		}

		@Override
		public void afterEach(final ExtensionContext context) throws Exception {
			Properties preTest = beforeEachState;
			Properties actual = System.getProperties();

			assertThat(preTest).isNotNull();
			assertThat(actual).containsExactlyInAnyOrderEntriesOf(preTest);
			beforeEachState = null;
		}

	}
}
