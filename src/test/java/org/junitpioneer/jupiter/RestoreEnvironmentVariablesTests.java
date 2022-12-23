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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verify proper behavior when annotated on a top level class
 */
@DisplayName("RestoreEnvironmentVariables Annotation")
@RestoreEnvironmentVariables
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@TestMethodOrder(OrderAnnotation.class)
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

	@Test @Order(1)
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

	@Test @Order(2)
	@DisplayName("Envar X from the previous test should have been reset")
	void shouldNotSeeChangesFromPreviousTest() {
		assertThat(System.getenv("X")).isNull();
	}

	@Nested @Order(1)
	@DisplayName("Nested tests should inherit restore behavior and be able to override")
	class NestedTestsA {

		@BeforeEach
		void methodSetUp() {
			EnvironmentVariableUtils.set("M", "each envar M Nest");
		}

		@Test @Order(1)
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

		@Test @Order(2)
		@DisplayName("Envar X from the previous test should have been reset")
		void shouldNotSeeChangesFromPreviousTest() {
			assertThat(System.getenv("X")).isNull();
		}
	}

}
