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
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junitpioneer.testkit.ExecutionResults;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethod;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

/**
 * Verify proper behavior when annotated on a top level class
 */
@DisplayName("RestoreSystemProperties Annotation")
@RestoreSystemProperties
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestoreSystemPropertiesTests {

	@BeforeAll
	static void globalSetUp() {
		System.setProperty("A", "all sys A");
		System.setProperty("B", "all sys B");
		System.setProperty("C", "all sys C");
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

		System.setProperty("X", "method X");
	}

	@Test @Order(2)
	@DisplayName("Property X from the previous test should have been reset")
	void shouldNotSeeChangesFromPreviousTest() {
		assertThat(System.getProperty("X")).isNull();
	}

	@Nested @Order(1)
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



}
