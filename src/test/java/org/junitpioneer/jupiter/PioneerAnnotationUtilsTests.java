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
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("JUnitPioneer annotation utilities")
class PioneerAnnotationUtilsTests {

	@Nested
	@DisplayName("for annotations")
	class AnnotationUtilsTests {

		/*
		 * Cases covered here:
		 *  - directly present
		 *  - present on enclosing class
		 *
		 * Cases covered by Jupiter's `AnnotationSupport::findRepeatableAnnotations`:
		 *  - indirectly present (i.e. inherited from super class)
		 *  - meta present (i.e. on another annotation)
		 */

		@Nested
		@DisplayName("that are not repeatable")
		class SimpleAnnotations {

			@Nested
			@ResourceLock(value = "org.junitpioneer.jupiter.FailExtension", mode = READ_WRITE)
			@DisplayName("and not stackable")
			class StopOnFirst {

				@BeforeEach
				void enableStopOnFirst() {
					FailExtension.FIND_ENCLOSING.set(false);
				}

				@Test
				void discoversMethodAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.FailTestCases.class,
								"methodIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContaining("method");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.FailTestCases.RootClassTestCases.class,
								"rootClassIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContainingAll("root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.class,
								"classIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContaining("nested class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.class,
								"outerClassIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContaining("nested class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
								"outerClassIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContaining("nested class");
				}

			}

			@Nested
			@ResourceLock(value = "org.junitpioneer.jupiter.FailExtension", mode = READ_WRITE)
			@DisplayName("but stackable")
			class Stackable {

				@BeforeEach
				void enableStopOnFirst() {
					FailExtension.FIND_ENCLOSING.set(true);
				}

				@Test
				void discoversMethodAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.FailTestCases.class,
								"methodIsAnnotated");
					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("method", "root class");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.FailTestCases.RootClassTestCases.class,
								"rootClassIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContaining("root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.class,
								"classIsAnnotated");
					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("nested class", "root class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.class,
								"outerClassIsAnnotated");
					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("nested class", "root class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
								"outerClassIsAnnotated");
					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("nested class", "root class");
				}

			}

		}

		@Nested
		@DisplayName("that are repeatable")
		class RepeatableAnnotations {

			@Nested
			@ResourceLock(value = "org.junitpioneer.jupiter.RepeatableFailExtension", mode = READ_WRITE)
			@DisplayName("but not stackable")
			class StopOnFirst {

				@BeforeEach
				void enableStopOnFirst() {
					RepeatableFailExtension.FIND_ENCLOSING.set(false);
				}

				@Test
				void discoversMethodAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.class,
								"methodIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContainingAll("method");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.RootClassTestCases.class,
								"rootClassIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContainingAll("root class");
				}

				@Test
				void discoversRepeatedMethodAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.class,
								"methodIsRepeatablyAnnotated");
					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("repeated", "annotation");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.class,
								"classIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContainingAll("nested class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.class,
								"outerClassIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContainingAll("nested class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
								"outerClassIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContainingAll("nested class");
				}

				@Test
				void discoversRepeatedOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedRepeatableTestCases.class,
								"outerClassIsRepeatablyAnnotatedAnnotated");
					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("repeated", "annotation");
				}

			}

			@Nested
			@ResourceLock(value = "org.junitpioneer.jupiter.RepeatableFailExtension", mode = READ_WRITE)
			@DisplayName("and stackable")
			class Stackable {

				@BeforeEach
				void enableStopOnFirst() {
					RepeatableFailExtension.FIND_ENCLOSING.set(true);
				}

				@Test
				void discoversMethodAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.class,
								"methodIsAnnotated");
					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("method", "root class");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.RootClassTestCases.class,
								"rootClassIsAnnotated");
					assertThat(results).hasSingleFailedTest().withException().hasMessageContainingAll("root class");
				}

				@Test
				void discoversRepeatedMethodAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.class,
								"methodIsRepeatablyAnnotated");
					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("repeated", "annotation", "root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.class,
								"classIsAnnotated");
					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("nested class", "root class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.class,
								"outerClassIsAnnotated");
					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("nested class", "root class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
								"outerClassIsAnnotated");
					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("nested class", "root class");
				}

				@Test
				void discoversRepeatedOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedRepeatableTestCases.class,
								"outerClassIsRepeatablyAnnotatedAnnotated");

					assertThat(results)
							.hasSingleFailedTest()
							.withException()
							.hasMessageContainingAll("repeated", "annotation", "root class");
				}

				@Test
				void discoversAllAnnotationsAnnotatedWithAnAnnotation() throws NoSuchMethodException {
					Method annotatedMethod = AnnotatedAnnotationTestCases.class.getMethod("annotatedMethod");

					List<Annotation> annotatedAnnotations = PioneerAnnotationUtils
							.findAnnotatedAnnotations(annotatedMethod, ExtendWith.class);

					assertThat(annotatedAnnotations)
							.hasSize(5)
							.isInstanceOf(java.util.ArrayList.class)
							.extracting(Annotation::annotationType)
							.allSatisfy(annotation -> assertThat(annotation)
									.isIn(ParameterizedTest.class, ReportEntry.class, RepeatableFail.class));
				}

			}

		}

	}

}
