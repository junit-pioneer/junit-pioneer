/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.assertj.core.util.Lists.list;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
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
		@Test
		@DisplayName("does nothing if there are no parameters")
		void emptyRunsOnce() {
			ExecutionResults results = PioneerTestKit.executeTestMethod(BasicConfigurationTestCases.class, "empty");

			assertThat(results).hasSingleSucceededTest();
		}

		@Test
		@DisplayName("runs for each value once for single parameter")
		void singleParameterRunsForEach() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BasicConfigurationTestCases.class, "singleParameter",
						String.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(3).hasNumberOfSucceededTests(3);
			assertThat(results).hasNumberOfReportEntries(3).withValues("0", "1", "2");
		}

		@Test
		@DisplayName("creates a 3-fold cartesian product from a single value")
		void threeBitsIntoEightTests() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BasicConfigurationTestCases.class, "threeBits", String.class,
						String.class, String.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(8).hasNumberOfSucceededTests(8);
			assertThat(results)
					.hasNumberOfReportEntries(8)
					.withValues("000", "001", "010", "100", "011", "101", "110", "111");
		}

		@Test
		@DisplayName("creates a 3-fold cartesian product from an implicit factory method for three parameters")
		void nFoldIntoManyTests() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BasicConfigurationTestCases.class, "nFold", String.class,
						Class.class, TimeUnit.class, TestInfo.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(12).hasNumberOfSucceededTests(12);
			assertThat(results)
					.hasNumberOfReportEntries(12)
					.withValues("Omega, interface org.junit.jupiter.api.TestInfo, DAYS",
						"Alpha, interface java.lang.Comparable, HOURS",
						"Alpha, interface org.junit.jupiter.api.TestInfo, DAYS",
						"Omega, interface org.junit.jupiter.api.TestInfo, HOURS",
						"Alpha, interface org.junit.jupiter.api.TestInfo, HOURS",
						"Alpha, interface java.lang.Runnable, HOURS", "Alpha, interface java.lang.Runnable, DAYS",
						"Omega, interface java.lang.Runnable, HOURS", "Omega, interface java.lang.Runnable, DAYS",
						"Alpha, interface java.lang.Comparable, DAYS", "Omega, interface java.lang.Comparable, DAYS",
						"Omega, interface java.lang.Comparable, HOURS");
		}

		@Test
		@DisplayName("creates a 2-fold cartesian product from an explicit factory method for two parameters")
		void explicitFactorySuppliesValues() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BasicConfigurationTestCases.class, "explicitFactory",
						String.class, TimeUnit.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(4).hasNumberOfSucceededTests(4);
			assertThat(results)
					.hasNumberOfReportEntries(4)
					.withValues("War,SECONDS", "War,DAYS", "Peace,SECONDS", "Peace,DAYS");
		}

		@Test
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianValueSource")
		void cartesianValueSources() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "poeticValues",
						String.class, String.class);

			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(6)
					.hasNumberOfSucceededTests(4)
					.hasNumberOfFailedTests(2);
			assertThat(results)
					.hasNumberOfReportEntries(6)
					.withValues("Two roads diverged in a yellow wood, - And looked down one as far as I could",
						"Two roads diverged in a yellow wood, - To where it bent in the undergrowth;",
						"And sorry I could not travel both - And looked down one as far as I could",
						"And sorry I could not travel both - To where it bent in the undergrowth;",
						"And be one traveler, long I stood - And looked down one as far as I could",
						"And be one traveler, long I stood - To where it bent in the undergrowth;");
		}

		@Test
		@DisplayName("works with @CartesianValueSource and auto-injected test parameters")
		void autoInjectedParams() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "injected", String.class,
						TestReporter.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(5).hasNumberOfSucceededTests(5);
			assertThat(results)
					.hasNumberOfReportEntries(5)
					.withValues("Then took the other, as just as fair,", "And having perhaps the better claim",
						"Because it was grassy and wanted wear,", "Though as for that the passing there",
						"Had worn them really about the same,");
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

				assertThat(results).hasNumberOfDynamicallyRegisteredTests(4).hasNumberOfSucceededTests(4);
				assertThat(results).hasNumberOfReportEntries(4).withValues("11", "12", "21", "22");

			}

			@Test
			@DisplayName("when test is annotated with @CartesianValueSource")
			void removesExtraFromAnnotation() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethodWithParameterTypes(RedundantInputSetTestCases.class,
							"distinctInputsAnnotations", int.class, String.class);

				assertThat(results).hasNumberOfDynamicallyRegisteredTests(6).hasNumberOfSucceededTests(6);
				assertThat(results).hasNumberOfReportEntries(6).withValues("1A", "1B", "1C", "4A", "4B", "4C");

			}

			@Test
			@DisplayName("when test has a static factory method")
			void removesExtraFromFactory() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethodWithParameterTypes(RedundantInputSetTestCases.class, "distinctInputsFactory",
							TimeUnit.class, String.class);

				assertThat(results).hasNumberOfDynamicallyRegisteredTests(3).hasNumberOfSucceededTests(3);
				assertThat(results).hasNumberOfReportEntries(3).withValues("A:SECONDS", "B:SECONDS", "C:SECONDS");

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
		return new CartesianProductTest.Sets().add("War", "Peace").add(TimeUnit.SECONDS, TimeUnit.DAYS);
	}

	@Nested
	@DisplayName("fails when")
	class BadConfigurationTests {

		@Test
		@DisplayName("the name is overwritten with empty string")
		void throwsForEmptyName() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "noName", String.class,
						String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("CartesianProductTest can not have a non-empty display name");
		}

		@Test
		@DisplayName("there is no factory method")
		void throwsForMissingFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "noFactory", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("not found");
		}

		@Test
		@DisplayName("there is an implicit factory method but explicit factory name was given - which does not exist")
		void throwsForMissingExplicitFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "hasImplicitFactory",
						int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("not found");
		}

		@Test
		@DisplayName("the factory method is not static")
		void throwsForNonStaticFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "nonStaticFactory",
						int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("must be static");
		}

		@Test
		@DisplayName("the factory method does not return `Sets`")
		void throwsForWrongReturnValueFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "wrongReturnFactory",
						int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("must return");
		}

		@Test
		@DisplayName("the factory method does not produce enough parameters")
		void throwsForTooFewFactoryMethodParameters() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "incompleteFactory",
						int.class, String.class, TimeUnit.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(3).hasNumberOfFailedTests(3);

		}

		@Test
		@DisplayName("the factory method produces too many parameters")
		void throwsForTooManyFactoryMethodParameters() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "bloatedFactory", int.class,
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
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "wrongOrder", String.class,
						int.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(6).hasNumberOfFailedTests(6);

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

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(2).hasNumberOfFailedTests(2);

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

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(10).hasNumberOfFailedTests(10);

		}

		@Test
		@DisplayName("has both a value and a factory method specified")
		void conflict1() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "conflictValueAndFactory",
						String.class, String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessage("CartesianProductTest can only take exactly one type of arguments source");
		}

		@Test
		@DisplayName("has both a value and @CartesianValueSource annotations")
		void conflict2() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "conflictValueAndValueSource",
						String.class, String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessage("CartesianProductTest can only take exactly one type of arguments source");
		}

		@Test
		@DisplayName("has both a factory method and @CartesianValueSource annotations")
		void conflict3() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class,
						"conflictValueSourceAndFactory", int.class, String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessage("CartesianProductTest can only take exactly one type of arguments source");
		}

	}

	@Nested
	@DisplayName("sets")
	class SetsTests {

		CartesianProductTest.Sets sets = new CartesianProductTest.Sets();

		@Test
		@DisplayName("should add distinct elements")
		void shouldAddDistinct() {
			List<Integer> list = list(4, 5, 6);
			Stream<Integer> stream = Stream.of(7, 8, 9);
			Iterable<Integer> iterable = list(10, 11, 12);

			sets.add(1, 2, 3).addAll(list).addAll(stream).addAll(iterable);

			Assertions.assertThat(sets.getSets()).containsExactly(list(1, 2, 3), list, list(7, 8, 9), list(10, 11, 12));
		}

		@Test
		@DisplayName("should remove non-distinct elements")
		void shouldRemoveNonDistinct() {
			List<Integer> list = list(4, 5, 4);
			Stream<Integer> stream = Stream.of(7, 8, 7);

			sets.add(1, 2, 1).addAll(list).addAll(stream);

			Assertions.assertThat(sets.getSets()).containsExactly(list(1, 2), list(4, 5), list(7, 8));
		}

	}

	static class BasicConfigurationTestCases {

		@CartesianProductTest({ "0", "1" })
		void empty() {
		}

		@CartesianProductTest({ "0", "1", "2" })
		@ReportEntry("{0}")
		void singleParameter(String param) {
			int value = Integer.parseInt(param);
			Assertions.assertThat(value).isBetween(0, 2);
		}

		@CartesianProductTest({ "0", "1" })
		@ReportEntry("{0}{1}{2}")
		void threeBits(String a, String b, String c) {
			int value = Integer.parseUnsignedInt(a + b + c, 2);
			Assertions.assertThat(value).isBetween(0b000, 0b111);
		}

		@CartesianProductTest
		@ReportEntry("{0}, {1}, {2}")
		void nFold(String string, Class<?> type, TimeUnit unit, TestInfo info) {
			Assertions.assertThat(string).endsWith("a");
			Assertions.assertThat(type).isInterface();
			Assertions.assertThat(unit.name()).endsWith("S");
			Assertions.assertThat(info.getTags()).isEmpty();
		}

		@CartesianProductTest(factory = "supplyValues")
		@ReportEntry("{0},{1}")
		void explicitFactory(String string, TimeUnit unit) {
			Assertions.assertThat(string).isIn("War", "Peace");
			Assertions.assertThat(unit.name()).endsWith("S");
		}

	}

	static class BadConfigurationTestCases {

		@CartesianProductTest(value = { "1", "2" }, name = "")
		void noName(String a, String b) {
		}

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
			return new CartesianProductTest.Sets().add(1, 2, 3).add("Ice is cold", "Fire is hot").add(TimeUnit.DAYS);
		}

		@CartesianProductTest(factory = "getParams")
		void wrongOrder(String string, int i) {

		}

		static CartesianProductTest.Sets getParams() {

			return new CartesianProductTest.Sets().add(1, 2, 4).add("Message #1", "Message #2");

		}

		@CartesianProductTest(value = { "0", "1" }, factory = "getParams")
		void conflictValueAndFactory(String a, String b) {

		}

		@CartesianProductTest(value = { "0", "1" })
		@CartesianValueSource(strings = { "0", "1" })
		@CartesianValueSource(strings = { "0", "1" })
		void conflictValueAndValueSource(String a, String b) {

		}

		@CartesianProductTest(factory = "getParams")
		@CartesianValueSource(ints = { 0, 1 })
		@CartesianValueSource(strings = { "0", "1" })
		void conflictValueSourceAndFactory(int a, String b) {

		}

	}

	static class CartesianValueSourceTestCases {

		@CartesianProductTest
		@CartesianValueSource(strings = { "Two roads diverged in a yellow wood,", "And sorry I could not travel both",
				"And be one traveler, long I stood" })
		@CartesianValueSource(strings = { "And looked down one as far as I could",
				"To where it bent in the undergrowth;" })
		@ReportEntry("{0} - {1}")
		void poeticValues(String line, String endLine) {
			Assertions.assertThat(line).startsWith("And");
		}

		@CartesianProductTest
		@CartesianValueSource(strings = { "Then took the other, as just as fair,",
				"And having perhaps the better claim", "Because it was grassy and wanted wear,",
				"Though as for that the passing there", "Had worn them really about the same," })
		@ReportEntry("{0}")
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
		@ReportEntry("{0}{1}")
		void distinctInputs(String a, String b) {
		}

		@CartesianProductTest
		@CartesianValueSource(ints = { 1, 1, 4 })
		@CartesianValueSource(strings = { "A", "B", "C", "C" })
		@ReportEntry("{0}{1}")
		void distinctInputsAnnotations(int i, String string) {
		}

		@CartesianProductTest(factory = "nonDistinctInputs")
		@ReportEntry("{1}:{0}")
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
