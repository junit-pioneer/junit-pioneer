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
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junitpioneer.testkit.assertion.PropertiesAssert;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
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
		Properties orgProps = getflatClone(System.getProperties());	// Will include A & B from above
		Properties newProps = new Properties(orgProps);

		newProps.setProperty("C", "all sys C");	//	one prop actually set (not a default)

		System.setProperties(newProps);
	}

	@BeforeEach
	void beforeEachMethod() {
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

	/**
	 * 'Dumb' clone that flattens the nested default structure into a flat set of values.
	 *
	 * @param original
	 * @return
	 */
	static public Properties getflatClone(Properties original) {

		final Properties clone = new Properties();

		// This will get default values, but doesn't handle non-strings...
		original.propertyNames().asIterator().forEachRemaining(k -> {
			clone.put(k, original.getProperty(k.toString()));
		});

		return clone;
	}

	static public Properties deepClone(Properties original) throws Exception {

		Properties clonedDefaults = null;
		Properties defaults = getDefaultPropertiesInstance(original);

		if (defaults != null) {
			clonedDefaults = deepClone(defaults);
		}

		final Properties clone = new Properties(clonedDefaults);

		// Copy just the values directly in Map backing the Properties
		original.keySet().forEach(k -> {
			clone.put(k, original.get(k));
		});

		return clone;
	}

	static public Properties getDefaultPropertiesInstance(Properties parent) throws Exception {
		Field field = ReflectionSupport
				.findFields(Properties.class, f -> f.getName().equals("defaults"), HierarchyTraversalMode.BOTTOM_UP)
				.stream().findFirst().get();

		field.setAccessible(true);
		Properties theDefault = (Properties) ReflectionSupport.tryToReadFieldValue(field, parent).get();

		return theDefault;
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
			System.out.println("Verify BeforeAll");

			beforeAllState.push(deepClone(System.getProperties()));
		}

		@Override
		public void afterAll(final ExtensionContext context) throws Exception {

			System.out.println("Verify in after All");

			Properties preTest = beforeAllState.pop();
			Properties actual = System.getProperties();

			assertThat(preTest).isNotNull();
			PropertiesAssert.assertThat(actual).isEffectivelyTheSameAs(preTest);
			PropertiesAssert.assertThat(actual).isStrictlyTheSameAs(preTest);
		}

		@Override
		public void beforeEach(final ExtensionContext context) throws Exception {
			System.out.println("Verify BeforeEach");

			beforeEachState = deepClone(System.getProperties());
		}

		@Override
		public void afterEach(final ExtensionContext context) throws Exception {
			System.out.println("Verify in after Each");

			Properties preTest = beforeEachState;
			Properties actual = System.getProperties();

			assertThat(preTest).isNotNull();
			PropertiesAssert.assertThat(actual).isEffectivelyTheSameAs(preTest);
			PropertiesAssert.assertThat(actual).isStrictlyTheSameAs(preTest);
			beforeEachState = null;
		}

	}

}
