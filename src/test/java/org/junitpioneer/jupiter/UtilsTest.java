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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

@DisplayName("Utils")
class UtilsTest {

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
		class SimpleAnnotations {

			@Nested
			class StopOnFirst {

				@BeforeEach
				void enableStopOnFirst() {
					FailExtension.STACKABLE.set(true);
				}

				@Test
				void discoversMethodAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(UtilsAnnotationTestCases.FailTestCases.class,
						"methodIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "method");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.FailTestCases.RootClassTestCases.class, "rootClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.FailTestCases.NestedTestCases.class, "classIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
				}

			}

			@Nested
			class DontStopOnFirst {

				@BeforeEach
				void enableStopOnFirst() {
					FailExtension.STACKABLE.set(false);
				}

				@Test
				void discoversMethodAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(UtilsAnnotationTestCases.FailTestCases.class,
						"methodIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "method", "root class");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.FailTestCases.RootClassTestCases.class, "rootClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.FailTestCases.NestedTestCases.class, "classIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.FailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
				}

			}

		}

		@Nested
		class RepeatableAnnotations {

			@Nested
			class StopOnFirst {

				@BeforeEach
				void enableStopOnFirst() {
					RepeatableFailExtension.STACKABLE.set(true);
				}

				@Test
				void discoversMethodAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.class, "methodIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "method");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.RootClassTestCases.class,
						"rootClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "root class");
				}

				@Test
				void discoversRepeatedMethodAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.class, "methodIsRepeatablyAnnotated");
					assertFailedTestHasMessage(eventRecorder, "repeated", "annotation");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.NestedTestCases.class, "classIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class");
				}

				@Test
				void discoversRepeatedOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.NestedRepeatableTestCases.class,
						"outerClassIsRepeatablyAnnotatedAnnotated");
					assertFailedTestHasMessage(eventRecorder, "repeated", "annotation");
				}

			}

			@Nested
			class DontStopOnFirst {

				@BeforeEach
				void enableStopOnFirst() {
					RepeatableFailExtension.STACKABLE.set(false);
				}

				@Test
				void discoversMethodAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.class, "methodIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "method", "root class");
				}

				@Test
				void discoversRootClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.RootClassTestCases.class,
						"rootClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "root class");
				}

				@Test
				void discoversRepeatedMethodAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.class, "methodIsRepeatablyAnnotated");
					assertFailedTestHasMessage(eventRecorder, "repeated", "annotation", "root class");
				}

				@Test
				void discoversClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.NestedTestCases.class, "classIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
				}

				@Test
				void discoversOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
				}

				@Test
				void discoversOuterOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.NestedTestCases.TwiceNestedTestCases.ThriceNestedTestCases.class,
						"outerClassIsAnnotated");
					assertFailedTestHasMessage(eventRecorder, "nested class", "root class");
				}

				@Test
				void discoversRepeatedOuterClassAnnotation() {
					ExecutionEventRecorder eventRecorder = executeTests(
						UtilsAnnotationTestCases.RepeatableFailTestCases.NestedRepeatableTestCases.class,
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

	@Nested
	@DisplayName("to distinct set")
	class ToDistinctSetTests {

		Collector<Object, Set<Object>, Set<Object>> collector;

		@BeforeEach
		void setUp() throws Exception {
			collector = Utils.distinctToSet();
		}

		@Test
		@DisplayName("should add new elements")
		void accumulatorShouldAddNewElements() throws Exception {
			Set<Object> set = new HashSet<>();
			Object element = new Object();

			BiConsumer<Set<Object>, Object> accumulator = collector.accumulator();

			accumulator.accept(set, element);

			assertThat(set).containsExactly(element);
		}

		@Test
		@DisplayName("should not add duplicate elements")
		void accumulatorShouldNotAddDuplicateElements() throws Exception {
			Set<Object> set = new HashSet<>();
			Object element = new Object();
			set.add(element);

			BiConsumer<Set<Object>, Object> accumulator = collector.accumulator();

			assertThatThrownBy(() -> accumulator.accept(set, element)).isInstanceOf(IllegalStateException.class);
		}

		@Test
		@DisplayName("should combine sets with new elements")
		void combinerShouldCombineSetsWithNewElements() throws Exception {
			Set<Object> left = new HashSet<>();
			Object leftElement = new Object();
			left.add(leftElement);

			Set<Object> right = new HashSet<>();
			Object rightElement = new Object();
			right.add(rightElement);

			BinaryOperator<Set<Object>> combiner = collector.combiner();

			assertThat(combiner.apply(left, right)).containsExactlyInAnyOrder(leftElement, rightElement);
		}

		@Test
		@DisplayName("should not combine sets with duplicate elements")
		void combinerShouldNotCombineSetsWithDuplicates() throws Exception {
			Object element = new Object();

			Set<Object> left = new HashSet<>();
			left.add(element);

			Set<Object> right = new HashSet<>();
			right.add(element);

			BinaryOperator<Set<Object>> combiner = collector.combiner();

			assertThatThrownBy(() -> combiner.apply(left, right)).isInstanceOf(IllegalStateException.class);
		}

	}

}
