/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.cartesian;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junitpioneer.jupiter.ReportEntry;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

public class CartesianTestFactoryTests {

	@Nested
	@DisplayName("when configured correctly")
	class CorrectConfigurationTests {

		@Test
		@DisplayName("ignores parentheses in factory name")
		void ignoreParentheses() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectFactoryTestCases.class, "parenthesesDoNotCount",
						String.class, String.class);

			assertThat(results).hasNumberOfSucceededTests(4);
			assertThat(results).hasNumberOfReportEntries(4).withValues("AC", "AD", "BC", "BD");
		}

		@Test
		@DisplayName("finds very explicitly specified class and method")
		void findsExact() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectFactoryTestCases.class, "findsExact", String.class,
						String.class);

			assertThat(results).hasNumberOfSucceededTests(4);
			assertThat(results).hasNumberOfReportEntries(4).withValues("AC", "AD", "BC", "BD");
		}

		@Test
		@DisplayName("finds kind of explicitly specified class and method")
		void findsExactAgain() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectFactoryTestCases.class, "findsExactAgain", String.class,
						String.class);

			assertThat(results).hasNumberOfSucceededTests(4);
			assertThat(results).hasNumberOfReportEntries(4).withValues("AC", "AD", "BC", "BD");
		}

		@Test
		@DisplayName("finds vaguely specified class and method")
		void findsExactAgainAgain() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectFactoryTestCases.class, "findsExactAgainAgain",
						String.class, String.class);

			assertThat(results).hasNumberOfSucceededTests(4);
			assertThat(results).hasNumberOfReportEntries(4).withValues("AC", "AD", "BC", "BD");
		}

		@Test
		@DisplayName("works with null values for non-primitives")
		void worksWithNull() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CorrectFactoryTestCases.class, "worksWithNull", String.class,
						String.class);

			assertThat(results).hasNumberOfSucceededTests(2);
			assertThat(results).hasNumberOfReportEntries(2).withValues("A,null", "B,null");
		}

	}

	@Nested
	@DisplayName("when configured badly, fails")
	class WrongConfigurationTests {

		@Test
		@DisplayName("when factory is non-static")
		void nonStatic() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(WrongFactoryTestCases.class, "nonStatic", String.class,
						String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.andThenCheckException(exception -> assertThat(exception)
							.extracting(Throwable::getCause)
							.isExactlyInstanceOf(ExtensionConfigurationException.class)
							.extracting(Throwable::getMessage)
							.matches(message -> message.matches("^Method .* must be static.$")));
		}

		@Test
		@DisplayName("when factory doesn't return with Sets object")
		void nonSets() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(WrongFactoryTestCases.class, "nonSetsReturn", String.class,
						String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.andThenCheckException(exception -> assertThat(exception)
							.extracting(Throwable::getCause)
							.isExactlyInstanceOf(ExtensionConfigurationException.class)
							.extracting(Throwable::getMessage)
							.matches(message -> message.matches("^Method .* must return a .* object$")));
		}

		@Test
		@DisplayName("when factory explicitly should be inside a class that can't be found")
		void notThere() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(WrongFactoryTestCases.class, "notThere", String.class,
						String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.andThenCheckException(exception -> assertThat(exception)
							.extracting(Throwable::getCause)
							.isExactlyInstanceOf(ExtensionConfigurationException.class)
							.extracting(Throwable::getMessage)
							.matches(message -> message.matches("^Class .* not found, referenced in method .*$")));
		}

		@Test
		@DisplayName("when factory explicitly should be inside a class that can't be found")
		void tooManyArguments() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(WrongFactoryTestCases.class, "tooManyArguments", String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.andThenCheckException(exception -> assertThat(exception)
							.extracting(Throwable::getCause)
							.isExactlyInstanceOf(ParameterResolutionException.class)
							.extracting(Throwable::getMessage)
							.matches(message -> message
									.matches(
										"^Method .* must register values for each parameter exactly once. Expected \\[[0-9]] parameter sets, but got \\[[0-9]].$")));
		}

		@Test
		@DisplayName("when supplying null value to primitive parameter")
		void noNullToPrimitive() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(WrongFactoryTestCases.class, "noNullToPrimitive", int.class,
						int.class);

			assertThat(results)
					.hasNumberOfFailedTests(2)
					.andThenCheckExceptions(exceptions -> assertThat(exceptions)
							.hasOnlyElementsOfType(ParameterResolutionException.class)
							.extracting(Throwable::getMessage)
							.allMatch(
								message -> message.matches("^No ParameterResolver registered for parameter .*$")));
		}

	}

	static class WrongFactoryTestCases {

		@CartesianTest
		@CartesianTest.MethodFactory("nonStatic")
		void nonStatic(String s1, String s2) {
		}

		@CartesianTest
		@CartesianTest.MethodFactory("nonSetsReturn")
		void nonSetsReturn(String s1, String s2) {
		}

		@CartesianTest
		@CartesianTest.MethodFactory("NoClass#notThere")
		void notThere(String s1, String s2) {
		}

		@CartesianTest
		@CartesianTest.MethodFactory("tooMany")
		void tooManyArguments(String line) {
		}

		@CartesianTest
		@CartesianTest.MethodFactory("withNull")
		void noNullToPrimitive(int num1, int num2) {
		}

		ArgumentSets nonStatic() {
			return ArgumentSets.argumentsForFirstParameter("A", "B").argumentsForNextParameter("C", "D");
		}

		static List<?> nonSetsReturn() {
			return Arrays.asList("A", "B");
		}

		static ArgumentSets notThere() {
			return ArgumentSets.argumentsForFirstParameter("A", "B").argumentsForNextParameter("C", "D");
		}

		static ArgumentSets tooMany() {
			// use `Stream.ofList` to call those method overloads during tests as well
			return ArgumentSets
					.argumentsForFirstParameter(Stream.of("A", "B"))
					.argumentsForNextParameter(Stream.of("C", "D"));
		}

		static ArgumentSets withNull() {
			return ArgumentSets.argumentsForFirstParameter(1, 2).argumentsForNextParameter((Object) null);
		}

	}

	static class CorrectFactoryTestCases {

		@CartesianTest
		@CartesianTest.MethodFactory("parentheses()")
		@ReportEntry("{0}{1}")
		void parenthesesDoNotCount(String s1, String s2) {
		}

		@CartesianTest
		@CartesianTest.MethodFactory("org.junitpioneer.jupiter.cartesian.CartesianTestFactoryTests$CorrectFactoryTestCases$Inner#exact")
		@ReportEntry("{0}{1}")
		void findsExact(String s1, String s2) {
		}

		@CartesianTest
		@CartesianTest.MethodFactory("CorrectFactoryTestCases$Inner#exact")
		@ReportEntry("{0}{1}")
		void findsExactAgain(String s1, String s2) {
		}

		@CartesianTest
		@CartesianTest.MethodFactory("Inner#exact")
		@ReportEntry("{0}{1}")
		void findsExactAgainAgain(String s1, String s2) {
		}

		@CartesianTest
		@CartesianTest.MethodFactory("withNull")
		@ReportEntry("{0},{1}")
		void worksWithNull(String s1, String s2) {
		}

		static ArgumentSets parentheses() {
			return ArgumentSets.argumentsForFirstParameter("A", "B").argumentsForNextParameter("C", "D");
		}

		static ArgumentSets exact() {
			throw new ParameterResolutionException("Shouldn't call this, ever.");
		}

		static ArgumentSets withNull() {
			return ArgumentSets.argumentsForFirstParameter("A", "B").argumentsForNextParameter((Object) null);
		}

		static class Inner {

			static ArgumentSets exact() {
				return ArgumentSets.argumentsForFirstParameter("A", "B").argumentsForNextParameter("C", "D");
			}

		}

	}

}
