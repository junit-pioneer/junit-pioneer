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

import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

/**
 * Robert Frost: The Road Not Taken is in the public domain
 */
@DisplayName("CartesianProductTest")
public class CartesianProductTestExtensionTests {

	@Nested
	@DisplayName("when configured correctly")
	class StandardBehaviouralTests {

		// This behaves the same way as an empty @ParameterizedTest
		@CartesianProductTest(value = { "0", "1" })
		@DisplayName("does nothing if there are no parameters")
		void empty() {
		}

		@CartesianProductTest(value = { "0", "1", "2" })
		@DisplayName("runs for each parameter once for single parameter")
		void singleParameter(String param) {
			int value = Integer.parseInt(param);
			Assertions.assertThat(value).isBetween(0, 2);
		}

		@CartesianProductTest({ "0", "1" })
		@DisplayName("creates a 3-fold cartesian product from a single value")
		void threeBits(String a, String b, String c) {
			int value = Integer.parseUnsignedInt(a + b + c, 2);
			Assertions.assertThat(value).isBetween(0b000, 0b111);
		}

		@CartesianProductTest
		@DisplayName("creates a 3-fold cartesian product from an implicit factory method")
		void nFold(String string, Class<?> type, TimeUnit unit, TestInfo info) {
			Assertions.assertThat(string).endsWith("a");
			Assertions.assertThat(type).isInterface();
			Assertions.assertThat(unit.name()).endsWith("S");
			Assertions.assertThat(info.getTags()).isEmpty();
		}

		@CartesianProductTest(factory = "supplyValues")
		@DisplayName("creates a 2-fold cartesian product from an explicit factory method")
		void explicitFactory(String string, TimeUnit unit) {
			Assertions.assertThat(string).isIn("War", "Peace");
			Assertions.assertThat(unit.name()).endsWith("S");
		}

		@Test
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianValueSource")
		void test() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "poeticValues",
						String.class, String.class);

			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(6)
					.hasNumberOfSucceededTests(4)
					.hasNumberOfFailedTests(2);
		}

		@Test
		@DisplayName("works with @CartesianValueSource and auto-injected test parameters")
		void autoInjectedParams() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "injected", String.class,
						TestReporter.class);

			//@formatter:off
			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(5)
					.hasNumberOfSucceededTests(5);
			//@formatter:on
		}

		@Nested
		@DisplayName("removes redundant parameters from input sets")
		class CartesianProductRedundancyTests {

			@Test
			@DisplayName("when given a value")
			void removesExtraFromValue() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethodWithParameterTypes(RedundantInputSetTestCases.class, "distinctInputs",
							String.class, String.class);
				//@formatter:off
				assertThat(results)
						.hasNumberOfDynamicallyRegisteredTests(4)
						.hasNumberOfSucceededTests(4);
				//@formatter:on
			}

			@Test
			@DisplayName("when test is annotated with @CartesianValueSource")
			void removesExtraFromAnnotation() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethodWithParameterTypes(RedundantInputSetTestCases.class,
							"distinctInputsAnnotations", int.class, String.class);
				//@formatter:off
				assertThat(results)
						.hasNumberOfDynamicallyRegisteredTests(6)
						.hasNumberOfSucceededTests(6);
				//@formatter:on
			}

			@Test
			@DisplayName("when test has a static factory method")
			void removesExtraFromFactory() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethodWithParameterTypes(RedundantInputSetTestCases.class, "distinctInputsFactory",
							TimeUnit.class, String.class);
				//@formatter:off
				assertThat(results)
						.hasNumberOfDynamicallyRegisteredTests(3)
						.hasNumberOfSucceededTests(3);
				//@formatter:on
			}

		}

	}

	static CartesianProductTest.Sets nFold() {
		return new CartesianProductTest.Sets()
				.add("Alpha", "Omega")
				.add(Runnable.class, Comparable.class, TestInfo.class)
				.add(TimeUnit.DAYS, TimeUnit.HOURS);
	}

	static CartesianProductTest.Sets supplyValues() {
		//@formatter:off
		return new CartesianProductTest.Sets()
				.add("War", "Peace")
				.add(TimeUnit.SECONDS, TimeUnit.DAYS);
		//@formatter:on
	}

	@Nested
	@DisplayName("fails when")
	class BadConfigurationTests {

		@Test
		@DisplayName("there is no factory method")
		void throwsForMissingFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "noFactory", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(AssertionError.class)
					.hasMessageContaining("not found");
		}

		@Test
		@DisplayName("there is an implicit factory method but explicit factory name was given - which does not exists")
		void throwsForMissingExplicitFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "hasImplicitFactory", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(AssertionError.class)
					.hasMessageContaining("not found");
		}

		@Test
		@DisplayName("the factory method is not static")
		void throwsForNonStaticFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "nonStaticFactory", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(AssertionError.class)
					.hasMessageContaining("must be static");
		}

		@Test
		@DisplayName("the factory method does not return Sets")
		void throwsForWrongReturnValueFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "wrongReturnFactory", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(AssertionError.class)
					.hasMessageContaining("must return");
		}

		@Test
		@DisplayName("the factory method does not produce enough parameters")
		void throwsForTooFewFactoryMethodParameters() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "incompleteFactory", int.class,
						String.class, TimeUnit.class);

			//@formatter:off
			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(3)
					.hasNumberOfFailedTests(3);
			//@formatter:on
		}

		@Test
		@DisplayName("the factory method produces too much parameters")
		void throwsForTooManyFactoryMethodParameters() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "bloatedFactory", int.class,
						String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessageContaining("must register values for each parameter");
		}

		@Test
		@DisplayName("the factory method produces parameters in the wrong order")
		void throwsForFactoryWithWrongParameterOrder() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "wrongOrder", String.class,
						int.class);

			//@formatter:off
			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(6)
					.hasNumberOfFailedTests(6);
			//@formatter:on
		}

		@Test
		@DisplayName("not all parameters have a corresponding @CartesianValueSource")
		void missingAnnotation() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "missing", int.class,
						int.class);

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessageContaining("No ParameterResolver registered");
		}

		@Test
		@DisplayName("the @CartesianValueSource has the wrong type")
		void wrongType() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "wrongType", float.class);

			//@formatter:off
			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(2)
					.hasNumberOfFailedTests(2);
			//@formatter:on
		}

		@Test
		@DisplayName("the @CartesianValueSource defines multiple input values")
		void badMultiple() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "badMultiple",
						String.class, int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessageContaining("Exactly one type of input must be provided");
		}

		@Test
		@DisplayName("the @CartesianValueSource annotations are not in order")
		void wrongOrder() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "wrongOrder",
						String.class, int.class);

			//@formatter:off
			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(10)
					.hasNumberOfFailedTests(10);
			//@formatter:off
		}
	}

	static class BadConfigurationTest {

		@CartesianProductTest
		void noFactory(int i) {
		}

		@CartesianProductTest(factory = "nonExistentFactory")
		void hasImplicitFactory(int i) {
		}

		static CartesianProductTest.Sets hasImplicitFactory() {
			return new CartesianProductTest.Sets().add(1, 2, 3);
		}

		@CartesianProductTest
		void nonStaticFactory(int i) {
		}

		CartesianProductTest.Sets nonStaticFactory() {
			return new CartesianProductTest.Sets().add(1, 2, 3);
		}

		@CartesianProductTest
		void wrongReturnFactory(int i) {
		}

		static int wrongReturnFactory() {
			return 0;
		}

		@CartesianProductTest
		void incompleteFactory(int number, String string, TimeUnit unit) {
		}

		static CartesianProductTest.Sets incompleteFactory() {
			return new CartesianProductTest.Sets().add(1, 4, 9);
		}

		@CartesianProductTest
		void bloatedFactory(int number, String string) {
		}

		static CartesianProductTest.Sets bloatedFactory() {
			return new CartesianProductTest.Sets()
					.add(1, 2, 3)
					.add("Ice is cold", "Fire is hot")
					.add(TimeUnit.DAYS);
		}

		@CartesianProductTest(factory = "getParams")
		void wrongOrder(String string, int i) {

		}

		static CartesianProductTest.Sets getParams() {
			//@formatter:off
			return new CartesianProductTest.Sets()
					.add(1, 2, 4)
					.add("Message #1", "Message #2");
			//@formatter:on
		}

	}

	static class CartesianValueSourceTestCases {

		@CartesianProductTest
		@CartesianValueSource(strings = { "Two roads diverged in a yellow wood,", "And sorry I could not travel both",
				"And be one traveler, long I stood" })
		@CartesianValueSource(strings = { "And looked down one as far as I could",
				"To where it bent in the undergrowth;" })
		void poeticValues(String line, String endLine) {
			Assertions.assertThat(line).startsWith("And");
		}

		@CartesianProductTest
		@CartesianValueSource(strings = { "Then took the other, as just as fair,",
				"And having perhaps the better claim", "Because it was grassy and wanted wear,",
				"Though as for that the passing there", "Had worn them really about the same," })
		void injected(String poemLine, TestReporter reporter) {
		}

		@CartesianProductTest
		@CartesianValueSource(ints = { 1 })
		void missing(int i, int j) {
		}

		@CartesianProductTest
		@CartesianValueSource(strings = { "And both that morning equally lay", "In leaves no step had trodden black." })
		void wrongType(float f) {
		}

		@CartesianProductTest
		@CartesianValueSource(strings = { "Oh, I marked the first for another day!",
				"Yet knowing how way leads on to way", "I doubted if I should ever come back." }, ints = { 1, 3, 5 })
		void badMultiple(String line, int number) {
		}

		@CartesianProductTest
		@CartesianValueSource(ints = { 1, 2 })
		@CartesianValueSource(strings = { "I shall be telling this with a sigh", "Somewhere ages and ages hence:",
				"Two roads diverged in a wood, and I,", "I took the one less traveled by,",
				"And that has made all the difference." })
		void wrongOrder(String line, int number) {
		}

	}

	static class RedundantInputSetTestCases {

		@CartesianProductTest(value = { "1", "1", "2" })
		@DisplayName("removes duplicates from input sets")
		void distinctInputs(String a, String b) {
		}

		@CartesianProductTest
		@CartesianValueSource(ints = { 1, 1, 4 })
		@CartesianValueSource(strings = { "A", "B", "C", "C" })
		void distinctInputsAnnotations(int i, String string) {
		}

		@CartesianProductTest(factory = "nonDistinctInputs")
		void distinctInputsFactory(TimeUnit unit, String string) {
		}

	}

	static CartesianProductTest.Sets nonDistinctInputs() {
		//@formatter:off
		return new CartesianProductTest.Sets()
				.add(TimeUnit.SECONDS, TimeUnit.SECONDS)
				.add("A", "B", "C");
		//@formatter:on
	}

}
