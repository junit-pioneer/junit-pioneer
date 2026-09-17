/*
 * Copyright 2026 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberNameUtilsTests {

	@ParameterizedTest(name = "[{index}] {arguments}")
	@DisplayName("Should correctly extract method names from various String formats")
	@CsvSource(useHeadersInDisplayName = true, delimiter = '|', value = { "input| expected", "myMethod| myMethod",
			"MyClass#myMethod| myMethod", "MyClass#myMethod(int)| myMethod",
			"MyClass#myMethod(int\\, String)| myMethod" })
	void shouldExtractMethodName(String input, String expected) {
		assertThat(MemberNameUtils.extractMethodName(input)).isEqualTo(expected);
	}

	@ParameterizedTest
	@DisplayName("Should throw IllegalArgumentException when method reference name is null or empty.")
	@NullAndEmptySource
	@ValueSource(strings = { " ", "", "#", "MyClass#" })
	void shouldThrowExceptionForInvalidMethodNameInput(String input) {
		assertThatThrownBy(() -> MemberNameUtils.extractMethodName(input)).isInstanceOf(IllegalArgumentException.class);
	}

	@ParameterizedTest
	@DisplayName("Should correctly extract class names when '#' separator exists")
	@CsvSource(value = { "MyClass#myMethod, MyClass", "com.example.MyClass#myMethod, com.example.MyClass",
			"com.example.MyClass#myMethod(int), com.example.MyClass" })
	void shouldExtractClassName(String input, String expected) {
		assertThat(MemberNameUtils.extractClassName(input)).contains(expected);
	}

	@ParameterizedTest
	@DisplayName("Should return empty optional when no class name is provided")
	@NullAndEmptySource
	@ValueSource(strings = { "myMethod", "myMethod(int)", "#myMethod" })
	void shouldReturnEmptyOptionalWhenThereIsNoClasName(String input) {
		assertThat(MemberNameUtils.extractClassName(input)).isEmpty();
	}

}
