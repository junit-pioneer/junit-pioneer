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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

@DisplayName("JUnitPioneer annotation utilities")
class PioneerAnnotationUtilsTests {

	@Nested
	@DisplayName("for annotations")
	class AnnotationUtilsTests extends AbstractPioneerTestEngineTests {

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
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.FailTestCases.class, "methodIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "method");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.FailTestCases.RootClassTestCases.class, "rootClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.class, "classIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
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
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.FailTestCases.class, "methodIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "method", "root class");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.FailTestCases.RootClassTestCases.class, "rootClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.class, "classIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
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
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.class, "methodIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "method");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.RootClassTestCases.class,
						"rootClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "root class");
				}

				@Test
				void discoversRepeatedMethodAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.class, "methodIsRepeatablyAnnotated");
					assertFailedTestHasMessage(eventRecorder, "repeated", "annotation");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.class,
						"classIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
				}

				@Test
				void discoversRepeatedOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedRepeatableTestCases.class,
						"outerClassIsRepeatablyAnnotatedAnnotated");
					assertFailedTestHasMessage(eventRecorder, "repeated", "annotation");
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
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.class, "methodIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "method", "root class");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.RootClassTestCases.class,
						"rootClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "root class");
				}

				@Test
				void discoversRepeatedMethodAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.class, "methodIsRepeatablyAnnotated");
					assertFailedTestHasMessage(eventRecorder, "repeated", "annotation", "root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.class,
						"classIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
				}

				@Test
				void discoversRepeatedOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						PioneerAnnotationUtilsTestCases.RepeatableFailTestCases.NestedRepeatableTestCases.class,
						"outerClassIsRepeatablyAnnotatedAnnotated");
					assertFailedTestHasMessage(eventRecorder, "repeated", "annotation", "root class");
				}

			}

		}

		private void assertFailedTestHasMessage(ExecutionEventRecorder eventRecorder, String... messages) {
			List<ExecutionEvent> failed = eventRecorder.getFailedTestFinishedEvents();
			assertThat(failed).hasSize(1);
			assertThat(getFirstFailuresThrowable(eventRecorder).getMessage()).isEqualTo(String.join(",", messages));
		}

	}

}
