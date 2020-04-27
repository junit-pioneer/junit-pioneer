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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
					results.assertFailedTestHasMessage("method");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.FailTestCases.RootClassTestCases.class,
								"rootClassIsAnnotated");
					results.assertFailedTestHasMessage("root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.class,
								"classIsAnnotated");
					results.assertFailedTestHasMessage("nested class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.class,
								"outerClassIsAnnotated");
					results.assertFailedTestHasMessage("nested class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
								"outerClassIsAnnotated");
					results.assertFailedTestHasMessage("nested class");
				}

			}

			@Nested
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
					results.assertFailedTestHasMessage("method", "root class");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.FailTestCases.RootClassTestCases.class,
								"rootClassIsAnnotated");
					results.assertFailedTestHasMessage("root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.class,
								"classIsAnnotated");
					results.assertFailedTestHasMessage("nested class", "root class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.class,
								"outerClassIsAnnotated");
					results.assertFailedTestHasMessage("nested class", "root class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
								"outerClassIsAnnotated");
					results.assertFailedTestHasMessage("nested class", "root class");
				}

			}

		}

		@Nested
		@DisplayName("that are repeatable")
		class RepeatableAnnotations {

			@Nested
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
					results.assertFailedTestHasMessage("method");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.RootClassTestCases.class,
								"rootClassIsAnnotated");
					results.assertFailedTestHasMessage("root class");
				}

				@Test
				void discoversRepeatedMethodAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.class,
								"methodIsRepeatablyAnnotated");
					results.assertFailedTestHasMessage("repeated", "annotation");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.class,
								"classIsAnnotated");
					results.assertFailedTestHasMessage("nested class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.class,
								"outerClassIsAnnotated");
					results.assertFailedTestHasMessage("nested class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
								"outerClassIsAnnotated");
					results.assertFailedTestHasMessage("nested class");
				}

				@Test
				void discoversRepeatedOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedRepeatableTestCases.class,
								"outerClassIsRepeatablyAnnotatedAnnotated");
					results.assertFailedTestHasMessage("repeated", "annotation");
				}

			}

			@Nested
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
					results.assertFailedTestHasMessage("method", "root class");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.RootClassTestCases.class,
								"rootClassIsAnnotated");
					results.assertFailedTestHasMessage("root class");
				}

				@Test
				void discoversRepeatedMethodAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.class,
								"methodIsRepeatablyAnnotated");
					results.assertFailedTestHasMessage("repeated", "annotation", "root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.class,
								"classIsAnnotated");
					results.assertFailedTestHasMessage("nested class", "root class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.class,
								"outerClassIsAnnotated");
					results.assertFailedTestHasMessage("nested class", "root class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
								"outerClassIsAnnotated");
					results.assertFailedTestHasMessage("nested class", "root class");
				}

				@Test
				void discoversRepeatedOuterClassAnnotation() {
					ExecutionResults results = PioneerTestKit
							.executeTestMethod(
								PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedRepeatableTestCases.class,
								"outerClassIsRepeatablyAnnotatedAnnotated");

					results.assertFailedTestHasMessage("repeated", "annotation", "root class");
				}

			}

		}

	}

}
