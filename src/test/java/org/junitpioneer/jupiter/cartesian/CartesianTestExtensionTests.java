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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.PreconditionViolationException;
import org.junitpioneer.jupiter.ReportEntry;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Enum.Mode;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Values;
import org.junitpioneer.jupiter.params.ByteRangeSource;
import org.junitpioneer.jupiter.params.DoubleRangeSource;
import org.junitpioneer.jupiter.params.FloatRangeSource;
import org.junitpioneer.jupiter.params.IntRangeSource;
import org.junitpioneer.jupiter.params.LongRangeSource;
import org.junitpioneer.jupiter.params.ShortRangeSource;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

/**
 * Robert Frost: The Road Not Taken is in the public domain
 */
@DisplayName("CartesianTest")
public class CartesianTestExtensionTests {

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
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianTest.Values")
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
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianTest.Enum")
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
		@DisplayName("works with @CartesianTest.Enum with single omitted Enum type")
		void cartesianEnumSourceWithSingleOmittedType() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class,
						"allValuesWithSingleOmittedType", TestEnum.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(3).hasNumberOfSucceededTests(3);
			assertThat(results).hasNumberOfReportEntries(3).withValues("ONE", "TWO", "THREE");
		}

		@Test
		@DisplayName("works with @CartesianTest.Enum with multiple omitted Enum types")
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
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianTest.Enum with INCLUDE / EXCLUDE modes")
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
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianTest.Enum with INCLUDE / EXCLUDE modes and omitted types")
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
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianTest.Enum with MATCH_ANY / MATCH_ALL modes")
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
		@DisplayName("creates a 2-fold cartesian product when all parameters are supplied via @CartesianTest.Enum with MATCH_ANY / MATCH_ALL modes and omitted types")
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
		@DisplayName("works with @CartesianTest.Enum and auto-injected test parameters")
		void cartesianEnumSourceAutoInjectedParams() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "injected", TestEnum.class,
						TestReporter.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(3).hasNumberOfSucceededTests(3);
			assertThat(results).hasNumberOfReportEntries(3).withValues("ONE", "TWO", "THREE");
		}

		@Test
		@DisplayName("works with @CartesianTest.Values and auto-injected test parameters")
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
		@DisplayName("works with range source and @CartesianTest.Values combined")
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
		@DisplayName("works with range source and @CartesianTest.Enum combined")
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
		@DisplayName("works with @CartesianTest.Values and @CartesianTest.Enum combined")
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
		@DisplayName("works with mixed argument sources and @CartesianTest.Enum having omitted types")
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
		@DisplayName("works with @CartesianTest.Factory")
		void factorySource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianFactorySourceTestCases.class, "veryBasicTest",
						String.class, String.class);

			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(9)
					.hasNumberOfSucceededTests(6)
					.hasNumberOfFailedTests(3);
			assertThat(results)
					.hasNumberOfReportEntries(9)
					.withValues("And on the pedestal these words appear:Nothing beside remains. Round the decay",
						"And on the pedestal these words appear:Of that colossal wreck, boundless and bare",
						"And on the pedestal these words appear:The lone and level sands stretch far away.",
						"My name is Ozymandias, king of kings;Nothing beside remains. Round the decay",
						"My name is Ozymandias, king of kings;Of that colossal wreck, boundless and bare",
						"My name is Ozymandias, king of kings;The lone and level sands stretch far away.",
						"Look on my works, ye Mighty, and despair!Nothing beside remains. Round the decay",
						"Look on my works, ye Mighty, and despair!Of that colossal wreck, boundless and bare",
						"Look on my works, ye Mighty, and despair!The lone and level sands stretch far away.");
		}

		@Test
		@DisplayName("works with @CartesianTest.Factory and auto-injected params")
		void factorySourceWithTestReporter() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianFactorySourceTestCases.class, "autoInjectedParam",
						String.class, String.class, TestReporter.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(9).hasNumberOfSucceededTests(9);
			assertThat(results)
					.hasNumberOfReportEntries(9)
					.withValues("And on the pedestal these words appear:Nothing beside remains. Round the decay",
						"And on the pedestal these words appear:Of that colossal wreck, boundless and bare",
						"And on the pedestal these words appear:The lone and level sands stretch far away.",
						"My name is Ozymandias, king of kings;Nothing beside remains. Round the decay",
						"My name is Ozymandias, king of kings;Of that colossal wreck, boundless and bare",
						"My name is Ozymandias, king of kings;The lone and level sands stretch far away.",
						"Look on my works, ye Mighty, and despair!Nothing beside remains. Round the decay",
						"Look on my works, ye Mighty, and despair!Of that colossal wreck, boundless and bare",
						"Look on my works, ye Mighty, and despair!The lone and level sands stretch far away.");
		}

		@Test
		@DisplayName("ignores 'oversupplied' parameters")
		void factorySourceWithTestReporterNoSecondParam() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianFactorySourceTestCases.class, "ignoredParam",
						String.class, TestReporter.class);

			assertThat(results)
					.hasNumberOfReportEntries(9)
					.withValues("And on the pedestal these words appear:", "My name is Ozymandias, king of kings;",
						"Look on my works, ye Mighty, and despair!", "And on the pedestal these words appear:",
						"My name is Ozymandias, king of kings;", "Look on my works, ye Mighty, and despair!",
						"And on the pedestal these words appear:", "My name is Ozymandias, king of kings;",
						"Look on my works, ye Mighty, and despair!");
		}

		@Test
		@DisplayName("works when test class has a constructor with auto-injected values")
		void testClassWithConstructor() {
			ExecutionResults results = PioneerTestKit.executeTestClass(TestClassWithConstructorTestCases.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(4).hasNumberOfSucceededTests(4);
			assertThat(results).hasNumberOfReportEntries(4).withValues("13", "14", "23", "24");
		}

		@Test
		@DisplayName("works when test class has @BeforeEach with auto-injected values")
		void testClassWithBeforeEach() {
			ExecutionResults results = PioneerTestKit.executeTestClass(TestClassWithBeforeEachTestCases.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(4).hasNumberOfSucceededTests(4);
			assertThat(results).hasNumberOfReportEntries(4).withValues("13", "14", "23", "24");
		}

		@Nested
		@DisplayName("removes redundant parameters from input sets")
		class CartesianProductRedundancyTests {

			@Test
			@DisplayName("when test is annotated with @CartesianTest.Values")
			void removesExtraFromAnnotation() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethodWithParameterTypes(RedundantInputSetTestCases.class,
							"distinctInputsAnnotations", int.class, String.class);

				assertThat(results).hasNumberOfDynamicallyRegisteredTests(6).hasNumberOfSucceededTests(6);
				assertThat(results).hasNumberOfReportEntries(6).withValues("1A", "1B", "1C", "4A", "4B", "4C");

			}

		}

		@Nested
		@DisplayName("use custom arguments provider")
		class CartesianProductCustomArgumentsProviderTests {

			@Test
			@DisplayName("when configured on parameters")
			void usesCustomCartesianArgumentsProviderOnParameters() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethodWithParameterTypes(CustomCartesianArgumentsProviderTestCases.class,
							"twoCustomCartesianArgumentProviders", String.class, int.class);

				assertThat(results).hasNumberOfDynamicallyRegisteredTests(6).hasNumberOfSucceededTests(6);
				assertThat(results)
						.hasNumberOfReportEntries(6)
						.withValues("first(1)", "first(2)", "second(1)", "second(2)", "third(1)", "third(2)");
			}

			@Test
			@DisplayName("when configured with array parameters")
			void usesCustomCartesianArgumentsProviderWithArrayArgumentOnParameters() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethodWithParameterTypes(CustomCartesianArgumentsProviderTestCases.class,
							"singleArrayArgument", String[].class);

				assertThat(results).hasNumberOfDynamicallyRegisteredTests(2).hasNumberOfSucceededTests(2);
			}

		}

	}

	@Nested
	@DisplayName("fails when")
	class BadConfigurationTests {

		@Test
		@DisplayName("it has no arguments sources")
		void noArgumentsSources() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "noAnnotation", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessage("No arguments sources were found for @CartesianTest");
		}

		@Test
		@DisplayName("the name is overwritten with empty string")
		void throwsForEmptyName() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "noName", String.class,
						String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("CartesianTest can not have an empty display name");
		}

		@Test
		@DisplayName("not all parameters have a corresponding @CartesianTest.Values")
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
		@DisplayName("the @CartesianTest.Values has the wrong type")
		void wrongType() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "wrongType", float.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(2).hasNumberOfFailedTests(2);
		}

		@Test
		@DisplayName("the @CartesianTest.Values defines multiple input values")
		void badMultiple() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "badMultiple",
						String.class, int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageContaining("Could not provide arguments")
					.hasRootCauseExactlyInstanceOf(PreconditionViolationException.class);
		}

		@Test
		@DisplayName("the @CartesianTest.Values annotations are not in order")
		void wrongOrder() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "wrongOrder",
						String.class, int.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(10).hasNumberOfFailedTests(10);
		}

		@Test
		@DisplayName("not all parameters have a corresponding @CartesianTest.Enum")
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
		@DisplayName("not all parameters have a corresponding @CartesianTest.Enum with omitted types")
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
		@DisplayName("there is no Enum parameter with @CartesianTest.Enum and omitted type")
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
		@DisplayName("the @CartesianTest.Enum has the wrong type")
		void wrongTypeCartesianEnumSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "wrongType",
						AnotherTestEnum.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(2).hasNumberOfFailedTests(2);
		}

		@Test
		@DisplayName("the @CartesianTest.Enum annotations are not in order")
		void wrongOrderCartesianEnumSource() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianEnumSourceTestCases.class, "wrongOrder",
						AnotherTestEnum.class, TestEnum.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(9).hasNumberOfFailedTests(9);
		}

		@Test
		@DisplayName("the @CartesianTest.Enum annotation contains non existing names")
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
		@DisplayName("the @CartesianTest.Enum annotation with omitted type contains non existing names")
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
		@DisplayName("the @CartesianTest.Enum annotation contains duplicate names")
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
		@DisplayName("the @CartesianTest.Enum annotation with omitted type contains duplicate names")
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
		@DisplayName("the @CartesianTest.Enum annotation contains invalid pattern with MATCH_ANY mode")
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
		@DisplayName("the @CartesianTest.Enum annotation with omitted type contains invalid pattern with MATCH_ANY mode")
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
		@DisplayName("the @CartesianTest.Enum annotation contains invalid pattern with MATCH_ALL mode")
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
		@DisplayName("the @CartesianTest.Enum annotation with omitted type contains invalid pattern with MATCH_ALL mode")
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
		@DisplayName("ParameterizedTest does not work with @CartesianTest.Values")
		void parameterizedWithCartesianValues() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ArgumentsSourceTestCases.class, "parameterizedTest",
						int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					// CartesianValueArgumentsProvider does not get initialized because it does not implement AnnotationConsumer
					.withExceptionInstanceOf(PreconditionViolationException.class)
					.hasMessageContaining(
						"You must configure at least one set of arguments for this @ParameterizedTest");
		}

		@Test
		@DisplayName("there are both method-level and parameter-level arguments sources")
		void tooManyArgumentsSources() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "bothMethodAndParam",
						String.class, String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessage(
						"Providing both method-level and parameter-level argument sources for @CartesianTest is not supported.");
		}

		@Test
		@DisplayName("there are multiple method-level arguments sources")
		void multipleMethodLevelArgumentsSources() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianFactorySourceTestCases.class,
						"multipleMethodLevelAnnotations", String.class, String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessage("Only one method-level arguments source can be used with @CartesianTest");
		}

		@Test
		@DisplayName("provider throws an exception, wrapping in ExtensionConfigurationException")
		void rethrowProviderException() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(CartesianValueSourceTestCases.class, "empty", String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessage("Could not provide arguments because of exception.");
		}

		@Test
		@DisplayName("parameter annotation arguments provider implements CartesianMethodArgumentsProvider")
		void mismatchingInterfaceParam() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "mismatch",
						ArgumentSets.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.andThenCheckException(exception -> assertThat(exception)
							.extracting(Throwable::getCause)
							.isExactlyInstanceOf(PreconditionViolationException.class)
							.extracting(Throwable::getMessage)
							.matches(message -> message
									.matches(
										"^.* does not implement CartesianParameterArgumentsProvider interface\\.$")));
		}

		@Test
		@DisplayName("method annotation arguments provider implements CartesianParameterArgumentsProvider")
		void mismatchingInterfaceMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTestCases.class, "otherMismatch",
						String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.andThenCheckException(exception -> assertThat(exception)
							.extracting(Throwable::getCause)
							.isExactlyInstanceOf(PreconditionViolationException.class)
							.extracting(Throwable::getMessage)
							.matches(message -> message
									.matches("^.* does not implement CartesianMethodArgumentsProvider interface\\.$")));
		}

	}

	static class BasicConfigurationTestCases {

		@CartesianTest
		void empty() {
		}

		@CartesianTest
		@ReportEntry("{0}")
		void singleParameter(@CartesianTest.Values(strings = { "0", "1", "2" }) String param) {
			int value = Integer.parseInt(param);
			assertThat(value).isBetween(0, 2);
		}

		@CartesianTest
		@ReportEntry("{0}")
		void abstractParam(@CartesianTest.Values(ints = { 1, 2 }) Number number) {
			assertThat(number).isIn(1, 2);
		}

	}

	static class BadConfigurationTestCases {

		@CartesianTest(name = "")
		void noName(@CartesianTest.Values(strings = "A") String a, @CartesianTest.Values(strings = "B") String b) {
		}

		@CartesianTest
		void noAnnotation(int i) {
		}

		@CartesianTest
		@CartesianTest.MethodFactory("poem")
		void bothMethodAndParam(@Values(strings = "A") String a, @Values(strings = "B") String b) {
		}

		@CartesianTest
		void mismatch(@Mismatch ArgumentSets s) {
		}

		@CartesianTest
		@OtherMismatch
		void otherMismatch(String s) {
		}

	}

	static class CartesianValueSourceTestCases {

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void poeticValues(
				@Values(strings = { "Two roads diverged in a yellow wood,", "And sorry I could not travel both",
						"And be one traveler, long I stood" }) String line,
				@Values(strings = { "And looked down one as far as I could",
						"To where it bent in the undergrowth;" }) String endLine) {
			assertThat(line).startsWith("And");
		}

		@CartesianTest
		@ReportEntry("{0}")
		void injected(@CartesianTest.Values(strings = { "Then took the other, as just as fair,",
				"And having perhaps the better claim", "Because it was grassy and wanted wear,",
				"Though as for that the passing there", "Had worn them really about the same," }) String poemLine,
				TestReporter reporter) {
		}

		@CartesianTest
		void missing(@CartesianTest.Values(ints = { 1 }) int i, int j) {
		}

		@CartesianTest
		void empty(@CartesianTest.Values String s) {
		}

		@CartesianTest
		void wrongType(@CartesianTest.Values(strings = { "And both that morning equally lay",
				"In leaves no step had trodden black." }) float f) {
		}

		@CartesianTest
		void badMultiple(@CartesianTest.Values(strings = { "Oh, I marked the first for another day!",
				"Yet knowing how way leads on to way",
				"I doubted if I should ever come back." }, ints = { 1, 3, 5 }) String line, int number) {
		}

		@CartesianTest
		void wrongOrder(@CartesianTest.Values(ints = { 1, 2 }) String line,
				@CartesianTest.Values(strings = { "I shall be telling this with a sigh",
						"Somewhere ages and ages hence:", "Two roads diverged in a wood, and I,",
						"I took the one less traveled by,", "And that has made all the difference." }) int number) {
		}

	}

	static class CartesianEnumSourceTestCases {

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void allValues(@CartesianTest.Enum(TestEnum.class) TestEnum e1,
				@CartesianTest.Enum(AnotherTestEnum.class) AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0}")
		void allValuesWithSingleOmittedType(@CartesianTest.Enum TestEnum e) {
		}

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void allValuesWithMultipleOmittedTypes(@CartesianTest.Enum TestEnum e1,
				@CartesianTest.Enum AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void someValues(
				@CartesianTest.Enum(value = TestEnum.class, names = { "ONE", "TWO" }, mode = Mode.INCLUDE) TestEnum e1,
				@CartesianTest.Enum(value = AnotherTestEnum.class, names = { "BETA",
						"GAMMA" }, mode = Mode.EXCLUDE) AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void someValuesWithOmittedTypes(@CartesianTest.Enum(names = { "ONE", "TWO" }, mode = Mode.INCLUDE) TestEnum e1,
				@CartesianTest.Enum(names = { "BETA", "GAMMA" }, mode = Mode.EXCLUDE) AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void patternValues(
				@CartesianTest.Enum(value = TestEnum.class, names = { "O.*",
						"TW.*" }, mode = Mode.MATCH_ANY) TestEnum e1,
				@CartesianTest.Enum(value = AnotherTestEnum.class, names = { "AL.*",
						".*PHA" }, mode = Mode.MATCH_ALL) AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void patternValuesWithOmittedTypes(
				@CartesianTest.Enum(names = { "O.*", "TW.*" }, mode = Mode.MATCH_ANY) TestEnum e1,
				@CartesianTest.Enum(names = { "AL.*", ".*PHA" }, mode = Mode.MATCH_ALL) AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0}")
		void injected(@CartesianTest.Enum(TestEnum.class) TestEnum e, TestReporter reporter) {
		}

		@CartesianTest
		void missing(@CartesianTest.Enum(value = TestEnum.class, names = "ONE") TestEnum e1, AnotherTestEnum e2) {
		}

		@CartesianTest
		void missingWithOmittedType(@CartesianTest.Enum(names = "ONE") TestEnum e1, AnotherTestEnum e2) {
		}

		@CartesianTest
		void nonEnumParameterWithOmittedType(@CartesianTest.Enum int i) {
		}

		@CartesianTest
		void wrongType(@CartesianTest.Enum(value = TestEnum.class, names = { "ONE", "TWO" }) AnotherTestEnum e) {
		}

		@CartesianTest
		void wrongOrder(@CartesianTest.Enum(TestEnum.class) AnotherTestEnum e1,
				@CartesianTest.Enum(AnotherTestEnum.class) TestEnum e2) {
		}

		@CartesianTest
		void nonExistingNames(
				@CartesianTest.Enum(value = TestEnum.class, names = { "ONE", "FOUR", "FIVE" }) TestEnum e1) {
		}

		@CartesianTest
		void nonExistingNamesWithOmittedType(@CartesianTest.Enum(names = { "ONE", "FOUR", "FIVE" }) TestEnum e1) {
		}

		@CartesianTest
		void duplicateNames(@CartesianTest.Enum(value = TestEnum.class, names = { "ONE", "ONE" }) TestEnum e1) {
		}

		@CartesianTest
		void duplicateNamesWithOmittedType(@CartesianTest.Enum(names = { "ONE", "ONE" }) TestEnum e1) {
		}

		@CartesianTest
		void wrongAnyPattern(@CartesianTest.Enum(value = TestEnum.class, names = { "T.*",
				"[" }, mode = Mode.MATCH_ANY) TestEnum e1) {
		}

		@CartesianTest
		void wrongAnyPatternWithOmittedType(
				@CartesianTest.Enum(names = { "T.*", "[" }, mode = Mode.MATCH_ANY) TestEnum e1) {
		}

		@CartesianTest
		void wrongAllPattern(@CartesianTest.Enum(value = TestEnum.class, names = { "T.*",
				"[" }, mode = Mode.MATCH_ALL) TestEnum e1) {
		}

		@CartesianTest
		void wrongAllPatternWithOmittedType(
				@CartesianTest.Enum(names = { "T.*", "[" }, mode = Mode.MATCH_ALL) TestEnum e1) {
		}

	}

	static class CartesianFactorySourceTestCases {

		@CartesianTest
		@CartesianTest.MethodFactory("poem")
		@ReportEntry("{0}{1}")
		void veryBasicTest(String firstLine, String secondLine) {
			assertThat(firstLine).contains("on");
		}

		@CartesianTest
		@CartesianTest.MethodFactory("poem")
		@MethodLevelCartesianArgumentSource
		void multipleMethodLevelAnnotations(String line, String otherLine) {
		}

		@CartesianTest
		@CartesianTest.MethodFactory("poem")
		@ReportEntry("{0}{1}")
		void autoInjectedParam(String line, String otherLine, TestReporter reporter) {
		}

		@CartesianTest
		@CartesianTest.MethodFactory("poem")
		void ignoredParam(String line, TestReporter reporter) {
			reporter.publishEntry(line);
		}

		static ArgumentSets poem() {
			// use `Arrays.asList` to call those method overloads during tests as well
			return ArgumentSets
					.argumentsForFirstParameter(Arrays
							.asList("And on the pedestal these words appear:", "My name is Ozymandias, king of kings;",
								"Look on my works, ye Mighty, and despair!"))
					.argumentsForNextParameter(Arrays
							.asList("Nothing beside remains. Round the decay",
								"Of that colossal wreck, boundless and bare",
								"The lone and level sands stretch far away."));
		}

	}

	static class RedundantInputSetTestCases {

		@CartesianTest
		@ReportEntry("{0}{1}")
		void distinctInputsAnnotations(@CartesianTest.Values(ints = { 1, 1, 4 }) int i,
				@CartesianTest.Values(strings = { "A", "B", "C", "C" }) String string) {
		}

	}

	static class ArgumentsSourceTestCases {

		@CartesianTest
		@ReportEntry("{0},{1}")
		void basicIntRangeSource(@IntRangeSource(from = 1, to = 4, closed = true) int i,
				@IntRangeSource(from = 2, to = 4, step = 2, closed = true) int j) {
		}

		@CartesianTest
		@ReportEntry("{0},{1}")
		void cartesianValueSource(@IntRangeSource(from = 0, to = 4) int i,
				@CartesianTest.Values(ints = { 2, 4 }) int j) {
		}

		@CartesianTest
		@ReportEntry("f:{0},b:{1}")
		void floatByteSource(@FloatRangeSource(from = 1.2f, to = 1.7f, step = 0.5f, closed = true) float f,
				@ByteRangeSource(from = 1, to = 4, closed = true) byte b) {
		}

		@CartesianTest
		@ReportEntry("d:{0},l:{1},s:{2}")
		void doubleLongShortSource(@DoubleRangeSource(from = 1.2, to = 2.2, step = 0.5) double d,
				@LongRangeSource(from = 1L, to = 3L) long l,
				@ShortRangeSource(from = 4, to = 5, closed = true) short s) {
		}

		@ParameterizedTest
		void parameterizedTest(@CartesianTest.Values(ints = { 1, 2, 3, 4 }) int i) {
		}

		@CartesianTest
		@ReportEntry("{0},{1}")
		void cartesianEnumSource(@IntRangeSource(from = 0, to = 4) int i,
				@CartesianTest.Enum(TestEnum.class) TestEnum e) {
		}

		@CartesianTest
		@ReportEntry("{0},{1}")
		void cartesianValueSourceWithCartesianEnumSource(@CartesianTest.Values(ints = { 0, 1, 2, 3 }) int i,
				@CartesianTest.Enum(TestEnum.class) TestEnum e) {
		}

		@CartesianTest
		@ReportEntry("{0},{1},{2},{3}")
		void mixedArgumentSourcesWithCartesianEnumSourceHavingOmittedTypes(@IntRangeSource(from = 0, to = 2) int i,
				@CartesianTest.Enum TestEnum e1, @CartesianTest.Enum AnotherTestEnum e2,
				@CartesianTest.Values(longs = { 2, 3 }) long l) {
			assertThat(i).isEqualTo(1);
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

	}

	static class TestClassWithConstructorTestCases {

		private final TestInfo testInfo;

		TestClassWithConstructorTestCases(TestInfo info) {
			this.testInfo = info;
		}

		@CartesianTest
		@ReportEntry("{0}{1}")
		void shouldHaveTestInfo(@CartesianTest.Values(ints = { 1, 2 }) int i,
				@CartesianTest.Values(ints = { 3, 4 }) int j) {
			assertThat(testInfo).isNotNull();
		}

	}

	static class TestClassWithBeforeEachTestCases {

		private TestInfo info;

		@BeforeEach
		void setUp(TestInfo info) {
			this.info = info;
		}

		@CartesianTest
		@ReportEntry("{0}{1}")
		void shouldHaveTestInfo(@CartesianTest.Values(ints = { 1, 2 }) int i,
				@CartesianTest.Values(ints = { 3, 4 }) int j) {
			assertThat(info).isNotNull();
		}

	}

	static class CustomCartesianArgumentsProviderTestCases {

		@CartesianTest
		@ReportEntry("{0}({1})")
		void twoCustomCartesianArgumentProviders(
				@CartesianArgumentsSource(FirstCustomCartesianArgumentsProvider.class) String string,
				@CartesianArgumentsSource(SecondCustomCartesianArgumentsProvider.class) int integer) {

		}

		@CartesianTest
		void singleArrayArgument(@CartesianArgumentsSource(StringArrayArgumentsProvider.class) String[] source) {
			assertThat(source).hasSize(2);
		}

	}

	private enum TestEnum {
		ONE, TWO, THREE
	}

	private enum AnotherTestEnum {
		ALPHA, BETA, GAMMA
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@CartesianArgumentsSource(FirstCustomCartesianArgumentsProvider.class)
	@interface MethodLevelCartesianArgumentSource {
	}

	static class FirstCustomCartesianArgumentsProvider implements CartesianParameterArgumentsProvider<String> {

		@Override
		public Stream<String> provideArguments(ExtensionContext context, Parameter parameter) {
			return Stream.of("first", "second", "third");
		}

	}

	static class SecondCustomCartesianArgumentsProvider implements CartesianParameterArgumentsProvider<Integer> {

		@Override
		public Stream<Integer> provideArguments(ExtensionContext context, Parameter parameter) {
			return Stream.of(1, 2);
		}

	}

	static class StringArrayArgumentsProvider implements CartesianParameterArgumentsProvider<String[]> {

		@Override
		public Stream<String[]> provideArguments(ExtensionContext context, Parameter parameter) {
			return Stream.of(new String[] { "1", "2" }, new String[] { "3", "4" });
		}

	}

	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@CartesianArgumentsSource(MismatchingProvider.class)
	@interface Mismatch {
	}

	static class MismatchingProvider implements CartesianMethodArgumentsProvider {

		@Override
		public ArgumentSets provideArguments(ExtensionContext context) {
			return ArgumentSets.argumentsForFirstParameter("1", "2");
		}

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@CartesianArgumentsSource(OtherMismatchingProvider.class)
	@interface OtherMismatch {
	}

	static class OtherMismatchingProvider implements CartesianParameterArgumentsProvider<String> {

		@Override
		public Stream<String> provideArguments(ExtensionContext context, Parameter parameter) {
			return Stream.of("1", "2");
		}

	}

}
