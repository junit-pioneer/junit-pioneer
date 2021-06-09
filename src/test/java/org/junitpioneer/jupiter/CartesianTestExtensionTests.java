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
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.PreconditionViolationException;
import org.junitpioneer.jupiter.CartesianEnumSource.Mode;
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

		@Nested
		@DisplayName("removes redundant parameters from input sets")
		class CartesianProductRedundancyTests {

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
			@DisplayName("when test class has a constructor with auto-injected values")
			void testClassWithConstructor() {
				ExecutionResults results = PioneerTestKit.executeTestClass(TestClassWithConstructor.class);

				assertThat(results).hasNumberOfDynamicallyRegisteredTests(4).hasNumberOfSucceededTests(4);
				assertThat(results).hasNumberOfReportEntries(4).withValues("13", "14", "23", "24");
			}

		}

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
					.hasMessageContaining("CartesianTest can not have a non-empty display name");
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
		@DisplayName("ParameterizedTest does not work with @CartesianValueSource")
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

	}

	@Nested
	@DisplayName("sets")
	class SetsTests {

		CartesianTest.Sets sets = new CartesianTest.Sets();

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

		@CartesianTest
		void empty() {
		}

		@CartesianTest
		@ReportEntry("{0}")
		void singleParameter(@CartesianValueSource(strings = { "0", "1", "2" }) String param) {
			int value = Integer.parseInt(param);
			assertThat(value).isBetween(0, 2);
		}

		@CartesianTest
		@ReportEntry("{0}")
		void abstractParam(@CartesianValueSource(ints = { 1, 2 }) Number number) {
			assertThat(number).isIn(1, 2);
		}

	}

	static class BadConfigurationTestCases {

		@CartesianTest(name = "")
		void noName(@CartesianValueSource(strings = "A") String a, @CartesianValueSource(strings = "B") String b) {
		}

		@CartesianTest
		void noAnnotation(int i) {
		}

	}

	static class CartesianValueSourceTestCases {

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void poeticValues(
				@CartesianValueSource(strings = { "Two roads diverged in a yellow wood,",
						"And sorry I could not travel both", "And be one traveler, long I stood" }) String line,
				@CartesianValueSource(strings = { "And looked down one as far as I could",
						"To where it bent in the undergrowth;" }) String endLine) {
			assertThat(line).startsWith("And");
		}

		@CartesianTest
		@ReportEntry("{0}")
		void injected(@CartesianValueSource(strings = { "Then took the other, as just as fair,",
				"And having perhaps the better claim", "Because it was grassy and wanted wear,",
				"Though as for that the passing there", "Had worn them really about the same," }) String poemLine,
				TestReporter reporter) {
		}

		@CartesianTest
		void missing(@CartesianValueSource(ints = { 1 }) int i, int j) {
		}

		@CartesianTest
		void wrongType(@CartesianValueSource(strings = { "And both that morning equally lay",
				"In leaves no step had trodden black." }) float f) {
		}

		@CartesianTest
		void badMultiple(@CartesianValueSource(strings = { "Oh, I marked the first for another day!",
				"Yet knowing how way leads on to way",
				"I doubted if I should ever come back." }, ints = { 1, 3, 5 }) String line, int number) {
		}

		@CartesianTest
		void wrongOrder(@CartesianValueSource(ints = { 1, 2 }) String line,
				@CartesianValueSource(strings = { "I shall be telling this with a sigh",
						"Somewhere ages and ages hence:", "Two roads diverged in a wood, and I,",
						"I took the one less traveled by,", "And that has made all the difference." }) int number) {
		}

	}

	static class CartesianEnumSourceTestCases {

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void allValues(@CartesianEnumSource(TestEnum.class) TestEnum e1,
				@CartesianEnumSource(AnotherTestEnum.class) AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0}")
		void allValuesWithSingleOmittedType(@CartesianEnumSource TestEnum e) {
		}

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void allValuesWithMultipleOmittedTypes(@CartesianEnumSource TestEnum e1,
				@CartesianEnumSource AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void someValues(
				@CartesianEnumSource(value = TestEnum.class, names = { "ONE", "TWO" }, mode = Mode.INCLUDE) TestEnum e1,
				@CartesianEnumSource(value = AnotherTestEnum.class, names = { "BETA",
						"GAMMA" }, mode = Mode.EXCLUDE) AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void someValuesWithOmittedTypes(@CartesianEnumSource(names = { "ONE", "TWO" }, mode = Mode.INCLUDE) TestEnum e1,
				@CartesianEnumSource(names = { "BETA", "GAMMA" }, mode = Mode.EXCLUDE) AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void patternValues(
				@CartesianEnumSource(value = TestEnum.class, names = { "O.*",
						"TW.*" }, mode = Mode.MATCH_ANY) TestEnum e1,
				@CartesianEnumSource(value = AnotherTestEnum.class, names = { "AL.*",
						".*PHA" }, mode = Mode.MATCH_ALL) AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0} - {1}")
		void patternValuesWithOmittedTypes(
				@CartesianEnumSource(names = { "O.*", "TW.*" }, mode = Mode.MATCH_ANY) TestEnum e1,
				@CartesianEnumSource(names = { "AL.*", ".*PHA" }, mode = Mode.MATCH_ALL) AnotherTestEnum e2) {
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

		@CartesianTest
		@ReportEntry("{0}")
		void injected(@CartesianEnumSource(TestEnum.class) TestEnum e, TestReporter reporter) {
		}

		@CartesianTest
		void missing(@CartesianEnumSource(value = TestEnum.class, names = "ONE") TestEnum e1, AnotherTestEnum e2) {
		}

		@CartesianTest
		void missingWithOmittedType(@CartesianEnumSource(names = "ONE") TestEnum e1, AnotherTestEnum e2) {
		}

		@CartesianTest
		void nonEnumParameterWithOmittedType(@CartesianEnumSource int i) {
		}

		@CartesianTest
		void wrongType(@CartesianEnumSource(value = TestEnum.class, names = { "ONE", "TWO" }) AnotherTestEnum e) {
		}

		@CartesianTest
		void wrongOrder(@CartesianEnumSource(TestEnum.class) AnotherTestEnum e1,
				@CartesianEnumSource(AnotherTestEnum.class) TestEnum e2) {
		}

		@CartesianTest
		void nonExistingNames(
				@CartesianEnumSource(value = TestEnum.class, names = { "ONE", "FOUR", "FIVE" }) TestEnum e1) {
		}

		@CartesianTest
		void nonExistingNamesWithOmittedType(@CartesianEnumSource(names = { "ONE", "FOUR", "FIVE" }) TestEnum e1) {
		}

		@CartesianTest
		void duplicateNames(@CartesianEnumSource(value = TestEnum.class, names = { "ONE", "ONE" }) TestEnum e1) {
		}

		@CartesianTest
		void duplicateNamesWithOmittedType(@CartesianEnumSource(names = { "ONE", "ONE" }) TestEnum e1) {
		}

		@CartesianTest
		void wrongAnyPattern(@CartesianEnumSource(value = TestEnum.class, names = { "T.*",
				"[" }, mode = Mode.MATCH_ANY) TestEnum e1) {
		}

		@CartesianTest
		void wrongAnyPatternWithOmittedType(
				@CartesianEnumSource(names = { "T.*", "[" }, mode = Mode.MATCH_ANY) TestEnum e1) {
		}

		@CartesianTest
		void wrongAllPattern(@CartesianEnumSource(value = TestEnum.class, names = { "T.*",
				"[" }, mode = Mode.MATCH_ALL) TestEnum e1) {
		}

		@CartesianTest
		void wrongAllPatternWithOmittedType(
				@CartesianEnumSource(names = { "T.*", "[" }, mode = Mode.MATCH_ALL) TestEnum e1) {
		}

	}

	static class RedundantInputSetTestCases {

		@CartesianTest
		@ReportEntry("{0}{1}")
		void distinctInputsAnnotations(@CartesianValueSource(ints = { 1, 1, 4 }) int i,
				@CartesianValueSource(strings = { "A", "B", "C", "C" }) String string) {
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
				@CartesianValueSource(ints = { 2, 4 }) int j) {
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
		void parameterizedTest(@CartesianValueSource(ints = { 1, 2, 3, 4 }) int i) {
		}

		@CartesianTest
		@ReportEntry("{0},{1}")
		void cartesianEnumSource(@IntRangeSource(from = 0, to = 4) int i,
				@CartesianEnumSource(TestEnum.class) TestEnum e) {
		}

		@CartesianTest
		@ReportEntry("{0},{1}")
		void cartesianValueSourceWithCartesianEnumSource(@CartesianValueSource(ints = { 0, 1, 2, 3 }) int i,
				@CartesianEnumSource(TestEnum.class) TestEnum e) {
		}

		@CartesianTest
		@ReportEntry("{0},{1},{2},{3}")
		void mixedArgumentSourcesWithCartesianEnumSourceHavingOmittedTypes(@IntRangeSource(from = 0, to = 2) int i,
				@CartesianEnumSource TestEnum e1, @CartesianEnumSource AnotherTestEnum e2,
				@CartesianValueSource(longs = { 2, 3 }) long l) {
			assertThat(i).isEqualTo(1);
			assertThat(e1).isEqualTo(TestEnum.ONE);
		}

	}

	static class TestClassWithConstructor {

		private final TestInfo testInfo;

		TestClassWithConstructor(TestInfo info) {
			this.testInfo = info;
		}

		@CartesianTest
		@ReportEntry("{0}{1}")
		void shouldHaveTestInfo(@CartesianValueSource(ints = { 1, 2 }) int i,
				@CartesianValueSource(ints = { 3, 4 }) int j) {
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
