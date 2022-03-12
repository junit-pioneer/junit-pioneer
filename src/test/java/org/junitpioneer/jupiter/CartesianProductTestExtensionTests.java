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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.PreconditionViolationException;
import org.junitpioneer.jupiter.CartesianEnumSource.Mode;
import org.junitpioneer.jupiter.params.ByteRangeSource;
import org.junitpioneer.jupiter.params.DoubleRangeSource;
import org.junitpioneer.jupiter.params.FloatRangeSource;
import org.junitpioneer.jupiter.params.IntRangeSource;
import org.junitpioneer.jupiter.params.IntRangeSource.IntRangeSources;
import org.junitpioneer.jupiter.params.LongRangeSource;
import org.junitpioneer.jupiter.params.ShortRangeSource;
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
		@DisplayName("works correctly with abstract parameters")
		void abstractParameter() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BasicConfigurationTestCases.class, "abstractParam",
						Number.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(2).hasNumberOfSucceededTests(2);
			assertThat(results).hasNumberOfReportEntries(2).withValues("1", "2");
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
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianEnumSource")
		void cartesianEnumSources() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "allValues",
						TestEnum.class, AnotherTestEnum.class);

			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(9)
					.hasNumberOfSucceededTests(3)
					.hasNumberOfFailedTests(6);
			assertThat(results)
					.hasNumberOfReportEntries(9)
					.withValues("ONE - ALPHA", "ONE - BETA", "ONE - GAMMA", "TWO - ALPHA", "TWO - BETA", "TWO - GAMMA",
						"THREE - ALPHA", "THREE - BETA", "THREE - GAMMA");
		}

		@Test
		@DisplayName("works with @CartesianEnumSource with single omitted Enum type")
		void cartesianEnumSourceWithSingleOmittedType() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class,
						"allValuesWithSingleOmittedType", TestEnum.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(3).hasNumberOfSucceededTests(3);
			assertThat(results).hasNumberOfReportEntries(3).withValues("ONE", "TWO", "THREE");
		}

		@Test
		@DisplayName("works with @CartesianEnumSource with multiple omitted Enum types")
		void cartesianEnumSourceWithMultipleOmittedTypes() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class,
						"allValuesWithMultipleOmittedTypes", TestEnum.class, AnotherTestEnum.class);

			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(9)
					.hasNumberOfSucceededTests(3)
					.hasNumberOfFailedTests(6);
			assertThat(results)
					.hasNumberOfReportEntries(9)
					.withValues("ONE - ALPHA", "ONE - BETA", "ONE - GAMMA", "TWO - ALPHA", "TWO - BETA", "TWO - GAMMA",
						"THREE - ALPHA", "THREE - BETA", "THREE - GAMMA");
		}

		@Test
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianEnumSource with INCLUDE / EXCLUDE modes")
		void cartesianEnumSourcesWithIncludeAndExcludeModes() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "someValues",
						TestEnum.class, AnotherTestEnum.class);

			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(2)
					.hasNumberOfSucceededTests(1)
					.hasNumberOfFailedTests(1);
			assertThat(results).hasNumberOfReportEntries(2).withValues("ONE - ALPHA", "TWO - ALPHA");
		}

		@Test
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianEnumSource with INCLUDE / EXCLUDE modes and omitted types")
		void cartesianEnumSourcesWithIncludeAndExcludeModesAndOmittedTypes() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class,
						"someValuesWithOmittedTypes", TestEnum.class, AnotherTestEnum.class);

			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(2)
					.hasNumberOfSucceededTests(1)
					.hasNumberOfFailedTests(1);
			assertThat(results).hasNumberOfReportEntries(2).withValues("ONE - ALPHA", "TWO - ALPHA");
		}

		@Test
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianEnumSource with MATCH_ANY / MATCH_ALL modes")
		void cartesianEnumSourcesWithMatchAnyAndMatchAllModes() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "patternValues",
						TestEnum.class, AnotherTestEnum.class);

			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(2)
					.hasNumberOfSucceededTests(1)
					.hasNumberOfFailedTests(1);
			assertThat(results).hasNumberOfReportEntries(2).withValues("ONE - ALPHA", "TWO - ALPHA");
		}

		@Test
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianEnumSource with MATCH_ANY / MATCH_ALL modes and omitted types")
		void cartesianEnumSourcesWithMatchAnyAndMatchAllModesAndOmittedTypes() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class,
						"patternValuesWithOmittedTypes", TestEnum.class, AnotherTestEnum.class);

			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(2)
					.hasNumberOfSucceededTests(1)
					.hasNumberOfFailedTests(1);
			assertThat(results).hasNumberOfReportEntries(2).withValues("ONE - ALPHA", "TWO - ALPHA");
		}

		@Test
		@DisplayName("works with @CartesianEnumSource and auto-injected test parameters")
		void cartesianEnumSourceAutoInjectedParams() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "injected", TestEnum.class,
						TestReporter.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(3).hasNumberOfSucceededTests(3);
			assertThat(results).hasNumberOfReportEntries(3).withValues("ONE", "TWO", "THREE");
		}

		@Test
		@DisplayName("works with @CartesianValueSource and auto-injected test parameters")
		void valueSourceAutoInjectedParams() {
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

		@Test
		@DisplayName("works with @IntRangeSource")
		void intRangeSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ArgumentsSourceTestCases.class, "basicIntRangeSource",
						int.class, int.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(8).hasNumberOfSucceededTests(8);
			assertThat(results)
					.hasNumberOfReportEntries(8)
					.withValues("1,2", "1,4", "2,2", "2,4", "3,2", "3,4", "4,2", "4,4");
		}

		@Test
		@DisplayName("works with @IntRangeSource if it is in a container annotation")
		void intRangeSourceInContainer() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ArgumentsSourceTestCases.class, "containerIntSource",
						int.class, int.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(6).hasNumberOfSucceededTests(6);
			assertThat(results).hasNumberOfReportEntries(6).withValues("12", "13", "22", "23", "32", "33");
		}

		@Test
		@DisplayName("works with range source and @CartesianValueSource combined")
		void cartesianValueSourceWithRangeSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ArgumentsSourceTestCases.class, "cartesianValueSource",
						int.class, int.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(8).hasNumberOfSucceededTests(8);
			assertThat(results)
					.hasNumberOfReportEntries(8)
					.withValues("0,2", "0,4", "1,2", "1,4", "2,2", "2,4", "3,2", "3,4");
		}

		@Test
		@DisplayName("works with range source and @CartesianEnumSource combined")
		void cartesianEnumSourceWithRangeSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ArgumentsSourceTestCases.class, "cartesianEnumSource",
						int.class, TestEnum.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(12).hasNumberOfSucceededTests(12);
			assertThat(results)
					.hasNumberOfReportEntries(12)
					.withValues("0,ONE", "0,TWO", "0,THREE", "1,ONE", "1,TWO", "1,THREE", "2,ONE", "2,TWO", "2,THREE",
						"3,ONE", "3,TWO", "3,THREE");
		}

		@Test
		@DisplayName("works with @CartesianValueSource and @CartesianEnumSource combined")
		void cartesianValueSourceWithCartesianEnumSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ArgumentsSourceTestCases.class,
						"cartesianValueSourceWithCartesianEnumSource", int.class, TestEnum.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(12).hasNumberOfSucceededTests(12);
			assertThat(results)
					.hasNumberOfReportEntries(12)
					.withValues("0,ONE", "0,TWO", "0,THREE", "1,ONE", "1,TWO", "1,THREE", "2,ONE", "2,TWO", "2,THREE",
						"3,ONE", "3,TWO", "3,THREE");
		}

		@Test
		@DisplayName("works with mixed argument sources and @CartesianEnumSource having omitted types")
		void mixedArgumentSourcesWithCartesianEnumSourceHavingOmittedTypes() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ArgumentsSourceTestCases.class,
						"mixedArgumentSourcesWithCartesianEnumSourceHavingOmittedTypes", int.class, TestEnum.class,
						AnotherTestEnum.class, long.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(36).hasNumberOfSucceededTests(6);
			assertThat(results)
					.hasNumberOfReportEntries(36)
					.withValues("0,ONE,ALPHA,2", "0,ONE,ALPHA,3", "0,ONE,BETA,2", "0,ONE,BETA,3", "0,ONE,GAMMA,2",
						"0,ONE,GAMMA,3", "0,TWO,ALPHA,2", "0,TWO,ALPHA,3", "0,TWO,BETA,2", "0,TWO,BETA,3",
						"0,TWO,GAMMA,2", "0,TWO,GAMMA,3", "0,THREE,ALPHA,2", "0,THREE,ALPHA,3", "0,THREE,BETA,2",
						"0,THREE,BETA,3", "0,THREE,GAMMA,2", "0,THREE,GAMMA,3", "1,ONE,ALPHA,2", "1,ONE,ALPHA,3",
						"1,ONE,BETA,2", "1,ONE,BETA,3", "1,ONE,GAMMA,2", "1,ONE,GAMMA,3", "1,TWO,ALPHA,2",
						"1,TWO,ALPHA,3", "1,TWO,BETA,2", "1,TWO,BETA,3", "1,TWO,GAMMA,2", "1,TWO,GAMMA,3",
						"1,THREE,ALPHA,2", "1,THREE,ALPHA,3", "1,THREE,BETA,2", "1,THREE,BETA,3", "1,THREE,GAMMA,2",
						"1,THREE,GAMMA,3");
		}

		@Test
		@DisplayName("works with @FloatRangeSource and @ByteRangeSource")
		void floatByteSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ArgumentsSourceTestCases.class, "floatByteSource", float.class,
						byte.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(8).hasNumberOfSucceededTests(8);
			assertThat(results)
					.hasNumberOfReportEntries(8)
					.withValues("f:1.2,b:1", "f:1.7,b:1", "f:1.2,b:2", "f:1.7,b:2", "f:1.2,b:3", "f:1.7,b:3",
						"f:1.2,b:4", "f:1.7,b:4");
		}

		@Test
		@DisplayName("works with @DoubleRangeSource, @LongRangeSource and @ShortRangeSource")
		void doubleLongShortSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ArgumentsSourceTestCases.class, "doubleLongShortSource",
						double.class, long.class, short.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(8).hasNumberOfSucceededTests(8);
			assertThat(results)
					.hasNumberOfReportEntries(8)
					.withValues("d:1.2,l:1,s:4", "d:1.7,l:1,s:4", "d:1.2,l:2,s:4", "d:1.7,l:2,s:4", "d:1.2,l:1,s:5",
						"d:1.7,l:1,s:5", "d:1.2,l:2,s:5", "d:1.7,l:2,s:5");
		}

		@Test
		@DisplayName("works with `null` values on non-primitive parameters")
		void nullWithNonPrimitive() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BasicConfigurationTestCases.class, "withNulls", TimeUnit.class,
						int.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(2).hasNumberOfSucceededTests(2);
			assertThat(results).hasNumberOfReportEntries(2).withValues("null,1", "null,2");
		}

		@Test
		@DisplayName("works with fully-qualified factory")
		void fullyQualifiedFactory() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BasicConfigurationTestCases.class,
						"testWithFullyQualifiedFactory", int.class, String.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(6).hasNumberOfSucceededTests(6);

			assertThat(results).hasNumberOfReportEntries(6).withValues("A-1", "A-2", "A-3", "B-1", "B-2", "B-3");
		}

		@Test
		@DisplayName("works with fully-qualified factory in nested class")
		void fullyQualifiedNestedFactory() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BasicConfigurationTestCases.class,
						"testWithFullyQualifiedNestedFactory", String.class, String.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(4).hasNumberOfSucceededTests(4);

			assertThat(results).hasNumberOfReportEntries(4).withValues("A-B", "A-A", "B-A", "B-B");
		}

		@Test
		@DisplayName("disregards any parameters passed in the factory name")
		void explicitFactoryDisregardsParameters() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BasicConfigurationTestCases.class,
						"explicitFactoryWithParentheses", String.class, TimeUnit.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(4).hasNumberOfSucceededTests(4);
			assertThat(results)
					.hasNumberOfReportEntries(4)
					.withValues("War,SECONDS", "War,DAYS", "Peace,SECONDS", "Peace,DAYS");
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

			@Test
			@DisplayName("when test class has a constructor with auto-injected values")
			void testClassWithConstructor() {
				ExecutionResults results = PioneerTestKit.executeTestClass(TestClassWithConstructorTestCases.class);

				assertThat(results).hasNumberOfDynamicallyRegisteredTests(4).hasNumberOfSucceededTests(4);
				assertThat(results).hasNumberOfReportEntries(4).withValues("13", "14", "23", "24");
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
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.hasCauseExactlyInstanceOf(PreconditionViolationException.class);
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
		@DisplayName("not all parameters have a corresponding @CartesianEnumSource")
		void missingAnnotationCartesianEnumSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "missing", TestEnum.class,
						AnotherTestEnum.class);

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessageContaining("No ParameterResolver registered");
		}

		@Test
		@DisplayName("not all parameters have a corresponding @CartesianEnumSource with omitted types")
		void missingAnnotationCartesianEnumSourceWithOmittedTypes() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "missingWithOmittedType",
						TestEnum.class, AnotherTestEnum.class);

			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessageContaining("No ParameterResolver registered");
		}

		@Test
		@DisplayName("there is no parameter with @CartesianEnumSource and omitted type")
		void missingParameterWithCartesianEnumSourceOmittedType() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class,
						"missingParameterWithOmittedType");

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.getCause()
					.isInstanceOf(PreconditionViolationException.class)
					.hasMessageContaining("Test method must declare at least one parameter");
		}

		@Test
		@DisplayName("there is no Enum parameter with @CartesianEnumSource and omitted type")
		void nonEnumParameterWithCartesianEnumSourceOmittedType() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class,
						"nonEnumParameterWithOmittedType", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.getCause()
					.isInstanceOf(PreconditionViolationException.class)
					.hasMessageContaining("Parameter of type %s must reference an Enum type", int.class);
		}

		@Test
		@DisplayName("the @CartesianEnumSource has the wrong type")
		void wrongTypeCartesianEnumSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "wrongType",
						AnotherTestEnum.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(2).hasNumberOfFailedTests(2);
		}

		@Test
		@DisplayName("the @CartesianEnumSource annotations are not in order")
		void wrongOrderCartesianEnumSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "wrongOrder",
						AnotherTestEnum.class, TestEnum.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(9).hasNumberOfFailedTests(9);
		}

		@Test
		@DisplayName("the @CartesianEnumSource annotation contains non existing names")
		void nonExistingNamesCartesianEnumSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "nonExistingNames",
						TestEnum.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.hasCauseExactlyInstanceOf(PreconditionViolationException.class);
		}

		@Test
		@DisplayName("the @CartesianEnumSource annotation with omitted type contains non existing names")
		void nonExistingNamesCartesianEnumSourceWithOmittedType() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class,
						"nonExistingNamesWithOmittedType", TestEnum.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.hasCauseExactlyInstanceOf(PreconditionViolationException.class);
		}

		@Test
		@DisplayName("the @CartesianEnumSource annotation contains duplicate names")
		void duplicateNamesCartesianEnumSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "duplicateNames",
						TestEnum.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.hasCauseExactlyInstanceOf(PreconditionViolationException.class);
		}

		@Test
		@DisplayName("the @CartesianEnumSource annotation with omitted type contains duplicate names")
		void duplicateNamesCartesianEnumSourceWithOmittedType() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class,
						"duplicateNamesWithOmittedType", TestEnum.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.hasCauseExactlyInstanceOf(PreconditionViolationException.class);
		}

		@Test
		@DisplayName("the @CartesianEnumSource annotation contains invalid pattern with MATCH_ANY mode")
		void wrongAnyPatternCartesianEnumSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "wrongAnyPattern",
						TestEnum.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.hasCauseExactlyInstanceOf(PreconditionViolationException.class);
		}

		@Test
		@DisplayName("the @CartesianEnumSource annotation with omitted type contains invalid pattern with MATCH_ANY mode")
		void wrongAnyPatternCartesianEnumSourceWIthOmittedType() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class,
						"wrongAnyPatternWithOmittedType", TestEnum.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.hasCauseExactlyInstanceOf(PreconditionViolationException.class);
		}

		@Test
		@DisplayName("the @CartesianEnumSource annotation contains invalid pattern with MATCH_ALL mode")
		void wrongAllPatternCartesianEnumSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "wrongAllPattern",
						TestEnum.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.hasCauseExactlyInstanceOf(PreconditionViolationException.class);
		}

		@Test
		@DisplayName("the @CartesianEnumSource annotation with omitted type contains invalid pattern with MATCH_ALL mode")
		void wrongAllPatternCartesianEnumSourceWithOmittedType() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class,
						"wrongAllPatternWithOmittedType", TestEnum.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.hasCauseExactlyInstanceOf(PreconditionViolationException.class);
		}

		@Test
		@DisplayName("has both a value and a factory method specified")
		void conflictValueVsFactory() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "conflictValueAndFactory",
						String.class, String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessage("CartesianProductTest can only take exactly one type of arguments source.");
		}

		@Test
		@DisplayName("has both a value and @CartesianValueSource annotations")
		void conflictValueVsAnnotation() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "conflictValueAndValueSource",
						String.class, String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessage("CartesianProductTest can only take exactly one type of arguments source.");
		}

		@Test
		@DisplayName("has both a factory method and @CartesianValueSource annotations")
		void conflictAnnotationVsFactory() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class,
						"conflictValueSourceAndFactory", int.class, String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessage("CartesianProductTest can only take exactly one type of arguments source.");
		}

		@Test
		@DisplayName("annotated with @ValueSource")
		void valueSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ArgumentsSourceTestCases.class, "valueSource", int.class,
						int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.hasCauseExactlyInstanceOf(PreconditionViolationException.class);
		}

		@Test
		@DisplayName("ParameterizedTest does not work with @CartesianValueSource")
		void parameterizedWithCartesianValues() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ArgumentsSourceTestCases.class, "parameterizedTest",
						int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					// CartesianValueArgumentsProvider does not get initialized because it does not implement AnnotationConsumer
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessageContaining("argument array must not be null");
		}

		@Test
		@DisplayName("primitive parameter is supplied with `null`")
		void primitiveNull() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "withNulls", int.class,
						int.class);

			assertThat(results)
					.hasNumberOfFailedTests(2)
					.withExceptionInstancesOf(ParameterResolutionException.class)
					.allMatch(exceptionMessage -> exceptionMessage.contains("No ParameterResolver registered"));
		}

		@Test
		@DisplayName("Factory with fully qualified name can't be found - missing class")
		void missingClass() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "missingClass", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContainingAll("Class", "not found, referenced in method");
		}

	}

	@Nested
	@DisplayName("sets")
	class ArgumentSetsTests {

		CartesianProductTest.Sets sets;

		@BeforeEach
		void setup() {
			sets = new CartesianProductTest.Sets();
		}

		@Test
		@DisplayName("should add distinct elements")
		void shouldAddDistinct() {
			List<Integer> list = list(4, 5, 6);
			Stream<Integer> stream = Stream.of(7, 8, 9);
			Iterable<Integer> iterable = list(10, 11, 12);

			sets.add(1, 2, 3).addAll(list).addAll(stream).addAll(iterable);

			assertThat(sets.getSets()).containsExactly(list(1, 2, 3), list, list(7, 8, 9), list(10, 11, 12));
		}

		@Test
		@DisplayName("should remove non-distinct elements")
		void shouldRemoveNonDistinct() {
			List<Integer> list = list(4, 5, 4);
			Stream<Integer> stream = Stream.of(7, 8, 7);
			Iterable<Integer> iterable = list(10, 11, 10);

			sets.add(1, 2, 1).addAll(list).addAll(stream).addAll(iterable);

			assertThat(sets.getSets()).containsExactly(list(1, 2), list(4, 5), list(7, 8), list(10, 11));
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
			assertThat(value).isBetween(0, 2);
		}

		@CartesianProductTest
		@CartesianValueSource(ints = { 1, 2 })
		@ReportEntry("{0}")
		void abstractParam(Number number) {
			assertThat(number).isIn(1, 2);
		}

		@CartesianProductTest({ "0", "1" })
		@ReportEntry("{0}{1}{2}")
		void threeBits(String a, String b, String c) {
			int value = Integer.parseUnsignedInt(a + b + c, 2);
			assertThat(value).isBetween(0b000, 0b111);
		}

		@CartesianProductTest
		@ReportEntry("{0}, {1}, {2}")
		void nFold(String string, Class<?> type, TimeUnit unit, TestInfo info) {
			assertThat(string).endsWith("a");
			assertThat(type).isInterface();
			assertThat(unit.name()).endsWith("S");
			assertThat(info.getTags()).isEmpty();
		}

		@CartesianProductTest(factory = "supplyValues")
		@ReportEntry("{0},{1}")
		void explicitFactory(String string, TimeUnit unit) {
			assertThat(string).isIn("War", "Peace");
			assertThat(unit.name()).endsWith("S");
		}

		@CartesianProductTest(factory = "withNulls")
		@ReportEntry("{0},{1}")
		void withNulls(TimeUnit unit, int i) {
		}

		@CartesianProductTest(factory = "supplyValues()")
		@ReportEntry("{0},{1}")
		void explicitFactoryWithParentheses(String string, TimeUnit unit) {
		}

		@CartesianProductTest(factory = "org.junitpioneer.jupiter.CartesianProductTestExtensionTests#explicitFactory")
		@ReportEntry("{1}-{0}")
		void testWithFullyQualifiedFactory(int i, String s) {
		}

		@CartesianProductTest(factory = "org.junitpioneer.jupiter.CartesianProductTestExtensionTests$NestedClass#explicitFactory")
		@ReportEntry("{1}-{0}")
		void testWithFullyQualifiedNestedFactory(String i, String s) {
		}

	}

	public static CartesianProductTest.Sets explicitFactory() {
		return new CartesianProductTest.Sets().add(1, 2, 3).add("A", "B");
	}

	public static class NestedClass {

		public static CartesianProductTest.Sets explicitFactory() {
			return new CartesianProductTest.Sets().add("A", "B").add("A", "B");
		}

	}

	static CartesianProductTest.Sets withNulls() {
		return new CartesianProductTest.Sets().add(new Object[] { null }).add(1, 2);
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

		@CartesianProductTest(factory = "org.bad.class#noFactory")
		void missingClass(int i) {
		}

		@CartesianProductTest(factory = "withNulls")
		void withNulls(int i, int j) {
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
			assertThat(line).startsWith("And");
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

	static class CartesianEnumSourceTestCases {

		@CartesianProductTest
		@CartesianEnumSource(TestEnum.class)
		@CartesianEnumSource(AnotherTestEnum.class)
		@ReportEntry("{0} - {1}")
		void allValues(TestEnum e1, AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianProductTest
		@CartesianEnumSource
		@ReportEntry("{0}")
		void allValuesWithSingleOmittedType(TestEnum e) {
		}

		@CartesianProductTest
		@CartesianEnumSource
		@CartesianEnumSource
		@ReportEntry("{0} - {1}")
		void allValuesWithMultipleOmittedTypes(TestEnum e1, AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianProductTest
		@CartesianEnumSource(value = TestEnum.class, names = { "ONE", "TWO" }, mode = Mode.INCLUDE)
		@CartesianEnumSource(value = AnotherTestEnum.class, names = { "BETA", "GAMMA" }, mode = Mode.EXCLUDE)
		@ReportEntry("{0} - {1}")
		void someValues(TestEnum e1, AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianProductTest
		@CartesianEnumSource(names = { "ONE", "TWO" }, mode = Mode.INCLUDE)
		@CartesianEnumSource(names = { "BETA", "GAMMA" }, mode = Mode.EXCLUDE)
		@ReportEntry("{0} - {1}")
		void someValuesWithOmittedTypes(TestEnum e1, AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianProductTest
		@CartesianEnumSource(value = TestEnum.class, names = { "O.*", "TW.*" }, mode = Mode.MATCH_ANY)
		@CartesianEnumSource(value = AnotherTestEnum.class, names = { "AL.*", ".*PHA" }, mode = Mode.MATCH_ALL)
		@ReportEntry("{0} - {1}")
		void patternValues(TestEnum e1, AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianProductTest
		@CartesianEnumSource(names = { "O.*", "TW.*" }, mode = Mode.MATCH_ANY)
		@CartesianEnumSource(names = { "AL.*", ".*PHA" }, mode = Mode.MATCH_ALL)
		@ReportEntry("{0} - {1}")
		void patternValuesWithOmittedTypes(TestEnum e1, AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianProductTest
		@CartesianEnumSource(TestEnum.class)
		@ReportEntry("{0}")
		void injected(TestEnum e, TestReporter reporter) {
		}

		@CartesianProductTest
		@CartesianEnumSource(value = TestEnum.class, names = "ONE")
		void missing(TestEnum e1, AnotherTestEnum e2) {
		}

		@CartesianProductTest
		@CartesianEnumSource(names = "ONE")
		void missingWithOmittedType(TestEnum e1, AnotherTestEnum e2) {
		}

		@CartesianProductTest
		@CartesianEnumSource
		void missingParameterWithOmittedType() {
		}

		@CartesianProductTest
		@CartesianEnumSource
		void nonEnumParameterWithOmittedType(int i) {
		}

		@CartesianProductTest
		@CartesianEnumSource(value = TestEnum.class, names = { "ONE", "TWO" })
		void wrongType(AnotherTestEnum e) {
		}

		@CartesianProductTest
		@CartesianEnumSource(TestEnum.class)
		@CartesianEnumSource(AnotherTestEnum.class)
		void wrongOrder(AnotherTestEnum e1, TestEnum e2) {
		}

		@CartesianProductTest
		@CartesianEnumSource(value = TestEnum.class, names = { "ONE", "FOUR", "FIVE" })
		void nonExistingNames(TestEnum e1) {
		}

		@CartesianProductTest
		@CartesianEnumSource(names = { "ONE", "FOUR", "FIVE" })
		void nonExistingNamesWithOmittedType(TestEnum e1) {
		}

		@CartesianProductTest
		@CartesianEnumSource(value = TestEnum.class, names = { "ONE", "ONE" })
		void duplicateNames(TestEnum e1) {
		}

		@CartesianProductTest
		@CartesianEnumSource(names = { "ONE", "ONE" })
		void duplicateNamesWithOmittedType(TestEnum e1) {
		}

		@CartesianProductTest
		@CartesianEnumSource(value = TestEnum.class, names = { "T.*", "[" }, mode = Mode.MATCH_ANY)
		void wrongAnyPattern(TestEnum e1) {
		}

		@CartesianProductTest
		@CartesianEnumSource(names = { "T.*", "[" }, mode = Mode.MATCH_ANY)
		void wrongAnyPatternWithOmittedType(TestEnum e1) {
		}

		@CartesianProductTest
		@CartesianEnumSource(value = TestEnum.class, names = { "T.*", "[" }, mode = Mode.MATCH_ALL)
		void wrongAllPattern(TestEnum e1) {
		}

		@CartesianProductTest
		@CartesianEnumSource(names = { "T.*", "[" }, mode = Mode.MATCH_ALL)
		void wrongAllPatternWithOmittedType(TestEnum e1) {
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

	static class ArgumentsSourceTestCases {

		@CartesianProductTest
		@IntRangeSource(from = 1, to = 4, closed = true)
		@IntRangeSource(from = 2, to = 4, step = 2, closed = true)
		@ReportEntry("{0},{1}")
		void basicIntRangeSource(int i, int j) {
		}

		@CartesianProductTest
		@IntRangeSource(from = 0, to = 4)
		@ValueSource(ints = { 2, 4 })
		void valueSource(int i, int j) {
		}

		@CartesianProductTest
		@IntRangeSources({ @IntRangeSource(from = 1, to = 3, closed = true),
				@IntRangeSource(from = 2, to = 3, closed = true) })
		@ReportEntry("{0}{1}")
		void containerIntSource(int i, int j) {
		}

		@CartesianProductTest
		@IntRangeSource(from = 0, to = 4)
		@CartesianValueSource(ints = { 2, 4 })
		@ReportEntry("{0},{1}")
		void cartesianValueSource(int i, int j) {
		}

		@CartesianProductTest
		@FloatRangeSource(from = 1.2f, to = 1.7f, step = 0.5f, closed = true)
		@ByteRangeSource(from = 1, to = 4, closed = true)
		@ReportEntry("f:{0},b:{1}")
		void floatByteSource(float f, byte b) {
		}

		@CartesianProductTest
		@DoubleRangeSource(from = 1.2, to = 2.2, step = 0.5) // 1.2, 1.7
		@LongRangeSource(from = 1L, to = 3L) // 1, 2
		@ShortRangeSource(from = 4, to = 5, closed = true) // 4, 5
		@ReportEntry("d:{0},l:{1},s:{2}")
		void doubleLongShortSource(double d, long l, short s) {
		}

		@ParameterizedTest
		@CartesianValueSource(ints = { 1, 2, 3, 4 })
		void parameterizedTest(int i) {
		}

		@CartesianProductTest
		@IntRangeSource(from = 0, to = 4)
		@CartesianEnumSource(TestEnum.class)
		@ReportEntry("{0},{1}")
		void cartesianEnumSource(int i, TestEnum e) {
		}

		@CartesianProductTest
		@CartesianValueSource(ints = { 0, 1, 2, 3 })
		@CartesianEnumSource(TestEnum.class)
		@ReportEntry("{0},{1}")
		void cartesianValueSourceWithCartesianEnumSource(int i, TestEnum e) {
		}

		@CartesianProductTest
		@IntRangeSource(from = 0, to = 2)
		@CartesianEnumSource
		@CartesianEnumSource
		@CartesianValueSource(longs = { 2, 3 })
		@ReportEntry("{0},{1},{2},{3}")
		void mixedArgumentSourcesWithCartesianEnumSourceHavingOmittedTypes(int i, TestEnum e1, AnotherTestEnum e2,
				long l) {
			assertThat(i).isEqualTo(1);
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

	}

	static class TestClassWithConstructorTestCases {

		private final TestInfo testInfo;

		TestClassWithConstructorTestCases(TestInfo info) {
			this.testInfo = info;
		}

		@CartesianProductTest
		@ReportEntry("{0}{1}")
		@CartesianValueSource(ints = { 1, 2 })
		@CartesianValueSource(ints = { 3, 4 })
		void shouldHaveTestInfo(int i, int j) {
			assertThat(testInfo).isNotNull();
		}

	}

	private enum TestEnum {
		ONE, TWO, THREE
	}

	private enum AnotherTestEnum {
		ALPHA, BETA, GAMMA
	}

}
