/*
 * Copyright 2016-2026 the original author or authors.
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.engine.TestExecutionResult;
import org.junitpioneer.jupiter.params.IntRangeSource;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@org.junit.jupiter.api.parallel.Execution(org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD)
class CartesianClassExtensionTests {

	@Test
	void executesAllMethodsForEachCombination() {
		CombinationTestCases.seen.clear();
		ExecutionResults results = PioneerTestKit.executeTestClass(CombinationTestCases.class);
		assertThat(results).hasNumberOfSucceededTests(8).hasNumberOfFailedTests(0);
		assertThat(CombinationTestCases.seen)
				.containsExactlyInAnyOrder("1:a:first", "1:a:second", "1:b:first", "1:b:second", "2:a:first",
					"2:a:second", "2:b:first", "2:b:second");
	}

	@Test
	void rejectsPerClassLifecycle() {
		ExecutionResults results = PioneerTestKit.executeTestClass(PerClassTestCases.class);
		assertThat(results.containerEvents().failed().count()).isEqualTo(1);
		assertThat(results.testEvents().started().count()).isZero();
		assertThat(results
				.containerEvents()
				.failed()
				.list()
				.get(0)
				.getPayload(org.junit.platform.engine.TestExecutionResult.class)
				.orElseThrow()
				.getThrowable()
				.orElseThrow()).hasMessageContaining("PER_METHOD");
	}

	@Test
	void reusesEnumAndRangeProviders() {
		ExecutionResults results = PioneerTestKit.executeTestClass(EnumRangeTestCases.class);
		assertThat(results).hasNumberOfSucceededTests(4).hasNumberOfFailedTests(0);
	}

	@Test
	void leavesMethodLevelCartesianTestsIndependent() {
		ExecutionResults results = PioneerTestKit.executeTestClass(MethodCartesianTestCases.class);
		assertThat(results).hasNumberOfSucceededTests(4).hasNumberOfFailedTests(0);
	}

	@Test
	void zeroParameterConstructorRunsOnce() {
		assertThat(PioneerTestKit.executeTestClass(EmptyTestCases.class)).hasSingleSucceededTest();
	}

	@Test
	void rejectsMissingSources() {
		ExecutionResults results = PioneerTestKit.executeTestClass(MissingSourceTestCases.class);
		assertThat(results.containerEvents().failed().count()).isEqualTo(1);
		assertThat(results.testEvents().started().count()).isZero();
	}

	@Test
	void rejectsBlankName() {
		ExecutionResults results = PioneerTestKit.executeTestClass(BlankNameTestCases.class);
		assertThat(results.containerEvents().failed().count()).isEqualTo(1);
	}

	@Test
	void formatsInvocationNames() {
		ExecutionResults results = PioneerTestKit.executeTestClass(CombinationTestCases.class);
		assertThat(results
				.containerEvents()
				.dynamicallyRegistered()
				.list()
				.stream()
				.map(event -> event.getTestDescriptor().getDisplayName()))
				.containsExactlyInAnyOrder("combination / 1 / 1, a", "combination / 2 / 1, b", "combination / 3 / 2, a",
					"combination / 4 / 2, b");
	}

	@Test
	void supportsComposedAndInheritedClassAnnotations() {
		assertThat(PioneerTestKit.executeTestClass(ComposedTestCases.class)).hasNumberOfSucceededTests(2);
		assertThat(PioneerTestKit.executeTestClass(InheritedTestCases.class)).hasNumberOfSucceededTests(2);
	}

	@Test
	void supportsNamedCustomSourcesAndDeduplicates() {
		assertThat(PioneerTestKit.executeTestClass(CustomSourceTestCases.class))
				.hasNumberOfSucceededTests(2)
				.hasNumberOfFailedTests(0);
	}

	@Test
	void runsOrdinaryNestedTestsWithConstructorArguments() {
		assertThat(PioneerTestKit.executeTestClass(OuterTestCases.class))
				.hasNumberOfSucceededTests(4)
				.hasNumberOfFailedTests(0);
	}

	@Test
	void doesNotResolveUnrelatedMethodParameters() {
		assertThat(PioneerTestKit.executeTestClass(UnrelatedMethodTestCases.class))
				.hasNumberOfFailedTests(2)
				.hasNumberOfSucceededTests(0);
	}

	@Test
	void rejectsMultipleConstructors() {
		assertThat(failure(MultipleConstructorTestCases.class)).hasMessageContaining("single constructor");
	}

	@Test
	void rejectsMultipleSources() {
		assertThat(failure(MultipleSourcesTestCases.class)).hasMessageContaining("exactly one arguments source");
	}

	@Test
	void rejectsMultipleCartesianSources() {
		assertThat(failure(MultipleCartesianSourcesTestCases.class))
				.hasMessageContaining("exactly one arguments source");
	}

	@Test
	void rejectsMultipleComposedCartesianSources() {
		assertThat(failure(MultipleComposedCartesianSourcesTestCases.class))
				.hasMessageContaining("exactly one arguments source");
	}

	@Test
	void supportsDirectCartesianSources() {
		assertThat(PioneerTestKit.executeTestClass(DirectCartesianSourceTestCases.class))
				.hasNumberOfSucceededTests(2)
				.hasNumberOfFailedTests(0);
	}

	@Test
	void rejectsDirectAndComposedCartesianSources() {
		assertThat(failure(DirectAndComposedCartesianSourcesTestCases.class))
				.hasMessageContaining("exactly one arguments source");
	}

	@Test
	void rejectsEmptySources() {
		assertThat(failure(EmptySourceTestCases.class))
				.hasRootCauseMessage("Illegal range. Equal from and to will produce an empty range.");
	}

	@Test
	void doesNotLetOtherResolversHideInvalidLifecycle() {
		assertThat(failure(OtherResolverPerClassTestCases.class)).hasMessageContaining("PER_METHOD");
	}

	@Test
	void preservesInvalidArgumentTypeFailures() {
		assertThat(PioneerTestKit.executeTestClass(WrongTypeTestCases.class))
				.hasNumberOfFailedTests(2)
				.hasNumberOfSucceededTests(0);
	}

	@Test
	void rejectsZeroCombinationsFromCustomProvider() {
		assertThat(failure(ZeroCombinationsTestCases.class))
				.hasMessageContaining("did not provide any invocation contexts");
	}

	@Test
	void closesCustomProviderStreams() {
		CloseableArgumentsProvider.closed = false;
		assertThat(PioneerTestKit.executeTestClass(CloseableSourceTestCases.class)).hasSingleSucceededTest();
		assertThat(CloseableArgumentsProvider.closed).isTrue();
	}

	@CartesianClass
	static class WrongTypeTestCases {

		WrongTypeTestCases(@CartesianTest.Values(strings = { "a", "b" }) int number) {
		}

		@Test
		void never() {
		}

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@CartesianArgumentsSource(EmptyArgumentsProvider.class)
	@interface EmptySource {
	}
	static class EmptyArgumentsProvider implements CartesianParameterArgumentsProvider<String> {

		@Override
		public Stream<String> provideArguments(ExtensionContext context, Parameter parameter) {
			return Stream.empty();
		}

	}
	@CartesianClass
	static class ZeroCombinationsTestCases {

		ZeroCombinationsTestCases(@EmptySource String value) {
		}

		@Test
		void never() {
		}

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@CartesianArgumentsSource(CloseableArgumentsProvider.class)
	@interface CloseableSource {
	}
	static class CloseableArgumentsProvider implements CartesianParameterArgumentsProvider<String> {

		static boolean closed;
		@Override
		public Stream<String> provideArguments(ExtensionContext context, Parameter parameter) {
			return Stream.of("value").onClose(() -> closed = true);
		}

	}
	@CartesianClass
	static class CloseableSourceTestCases {

		CloseableSourceTestCases(@CloseableSource String value) {
		}

		@Test
		void once() {
		}

	}

	@Test
	void runsTemplateAndPerTestCallbacksAtTheirJupiterScopes() {
		LifecycleTestCases.beforeAll.set(0);
		LifecycleTestCases.afterAll.set(0);
		LifecycleTestCases.beforeEach.set(0);
		LifecycleTestCases.afterEach.set(0);
		assertThat(PioneerTestKit.executeTestClass(LifecycleTestCases.class))
				.hasNumberOfSucceededTests(4)
				.hasNumberOfFailedTests(0);
		assertThat(LifecycleTestCases.beforeAll.get()).isEqualTo(1);
		assertThat(LifecycleTestCases.afterAll.get()).isEqualTo(1);
		assertThat(LifecycleTestCases.beforeEach.get()).isEqualTo(4);
		assertThat(LifecycleTestCases.afterEach.get()).isEqualTo(4);
	}

	@Test
	void rejectsInvalidNamePatterns() {
		assertThat(failure(InvalidNameTestCases.class)).hasMessageContaining("display name pattern");
	}

	@CartesianClass(name = "{")
	static class InvalidNameTestCases {

		@Test
		void never() {
		}

	}

	@CartesianClass
	static class LifecycleTestCases {

		static final java.util.concurrent.atomic.AtomicInteger beforeAll = new java.util.concurrent.atomic.AtomicInteger();
		static final java.util.concurrent.atomic.AtomicInteger afterAll = new java.util.concurrent.atomic.AtomicInteger();
		static final java.util.concurrent.atomic.AtomicInteger beforeEach = new java.util.concurrent.atomic.AtomicInteger();
		static final java.util.concurrent.atomic.AtomicInteger afterEach = new java.util.concurrent.atomic.AtomicInteger();
		LifecycleTestCases(@CartesianTest.Values(ints = { 1, 2 }) int number) {
		}

		@org.junit.jupiter.api.BeforeAll
		static void beforeAll() {
			beforeAll.incrementAndGet();
		}

		@org.junit.jupiter.api.AfterAll
		static void afterAll() {
			afterAll.incrementAndGet();
		}

		@BeforeEach
		void beforeEach() {
			beforeEach.incrementAndGet();
		}

		@org.junit.jupiter.api.AfterEach
		void afterEach() {
			afterEach.incrementAndGet();
		}

		@Test
		void first() {
		}

		@Test
		void second() {
		}

	}

	@Test
	void rejectsNonStaticNestedCartesianClasses() {
		assertThat(failure(NestedCartesianOuterTestCases.class)).hasMessageContaining("top-level or static nested");
	}

	static class NestedCartesianOuterTestCases {

		@Nested
		@CartesianClass
		class NestedCartesianTestCases {

			NestedCartesianTestCases(@CartesianTest.Values(ints = { 1, 2 }) int number) {
			}

			@Test
			void never() {
			}

		}

	}

	@Test
	void doesNotRejectPerClassLifecycleOnOrdinaryNestedTests() {
		assertThat(PioneerTestKit.executeTestClass(NestedPerClassOuterTestCases.class))
				.hasNumberOfSucceededTests(2)
				.hasNumberOfFailedTests(0);
	}

	@CartesianClass
	static class NestedPerClassOuterTestCases {

		private final int number;
		NestedPerClassOuterTestCases(@CartesianTest.Values(ints = { 1, 2 }) int number) {
			this.number = number;
		}
		@Nested
		@TestInstance(Lifecycle.PER_CLASS)
		class NestedPerClassTestCases {

			@Test
			void valid() {
				assertThat(number).isBetween(1, 2);
			}

		}

	}

	private static Throwable failure(Class<?> testClass) {
		ExecutionResults results = PioneerTestKit.executeTestClass(testClass);
		assertThat(results.testEvents().started().count()).isZero();
		return results
				.containerEvents()
				.failed()
				.list()
				.get(0)
				.getPayload(TestExecutionResult.class)
				.orElseThrow()
				.getThrowable()
				.orElseThrow();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	@CartesianClass
	@interface ComposedClass {
	}

	@ComposedClass
	static class ComposedTestCases {

		ComposedTestCases(@CartesianTest.Values(ints = { 1, 2 }) int number) {
		}

		@Test
		void valid() {
		}

	}

	@CartesianClass
	static class BaseTestCases {

		@Test
		void inherited() {
		}

	}
	static class InheritedTestCases extends BaseTestCases {

		InheritedTestCases(@CartesianTest.Values(ints = { 1, 2 }) int number) {
		}

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@CartesianArgumentsSource(NamedArgumentsProvider.class)
	@interface CustomSource {
	}

	static class NamedArgumentsProvider implements CartesianParameterArgumentsProvider<Named<String>> {

		@Override
		public Stream<Named<String>> provideArguments(ExtensionContext context, Parameter parameter) {
			Named<String> first = Named.of("first", "a");
			return Stream.of(first, first, Named.of("second", "b"));
		}

	}

	@CartesianClass
	static class CustomSourceTestCases {

		private final String value;
		CustomSourceTestCases(@CustomSource String value) {
			this.value = value;
		}

		@Test
		void payload() {
			assertThat(value).isIn("a", "b");
		}

	}

	@CartesianClass
	static class OuterTestCases {

		private final int number;
		OuterTestCases(@CartesianTest.Values(ints = { 1, 2 }) int number) {
			this.number = number;
		}

		@Test
		void outer() {
			assertThat(number).isBetween(1, 2);
		}
		@Nested
		class InnerTestCases {

			@Test
			void inner() {
				assertThat(number).isBetween(1, 2);
			}

		}

	}

	@CartesianClass
	static class UnrelatedMethodTestCases {

		UnrelatedMethodTestCases(@CartesianTest.Values(ints = { 1, 2 }) int number) {
		}

		@Test
		void invalid(int unrelated) {
		}

	}

	@CartesianClass
	static class MultipleConstructorTestCases {

		MultipleConstructorTestCases() {
		}

		MultipleConstructorTestCases(int number) {
		}

		@Test
		void never() {
		}

	}

	@CartesianClass
	static class MultipleSourcesTestCases {

		MultipleSourcesTestCases(@CartesianTest.Values(ints = { 1 }) @IntRangeSource(from = 1, to = 2) int number) {
		}

		@Test
		void never() {
		}

	}

	@CartesianClass
	static class MultipleCartesianSourcesTestCases {

		MultipleCartesianSourcesTestCases(
				@CartesianTest.Values(strings = { "FIRST" }) @CartesianTest.Enum String value) {
		}

		@Test
		void never() {
		}

	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@CartesianTest.Values(strings = { "FIRST" })
	@interface FirstValue {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@CartesianTest.Values(strings = { "SECOND" })
	@interface SecondValue {
	}

	@CartesianClass
	static class MultipleComposedCartesianSourcesTestCases {

		MultipleComposedCartesianSourcesTestCases(@FirstValue @SecondValue String value) {
		}

		@Test
		void never() {
		}

	}

	@CartesianClass
	static class DirectCartesianSourceTestCases {

		DirectCartesianSourceTestCases(@CartesianArgumentsSource(NamedArgumentsProvider.class) String value) {
		}

		@Test
		void valid() {
		}

	}

	@CartesianClass
	static class DirectAndComposedCartesianSourcesTestCases {

		DirectAndComposedCartesianSourcesTestCases(
				@CartesianArgumentsSource(NamedArgumentsProvider.class) @FirstValue String value) {
		}

		@Test
		void never() {
		}

	}

	@CartesianClass
	static class EmptySourceTestCases {

		EmptySourceTestCases(@IntRangeSource(from = 1, to = 1) int number) {
		}

		@Test
		void never() {
		}

	}

	static class OtherResolver implements ParameterResolver {

		@Override
		public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
			return parameter.getParameter().getType() == int.class;
		}

		@Override
		public Object resolveParameter(ParameterContext parameter, ExtensionContext context) {
			return 7;
		}

	}

	@CartesianClass
	@TestInstance(Lifecycle.PER_CLASS)
	@ExtendWith(OtherResolver.class)
	static class OtherResolverPerClassTestCases {

		OtherResolverPerClassTestCases(@CartesianTest.Values(ints = { 1, 2 }) int number) {
		}

		@Test
		void never() {
		}

	}

	@CartesianClass(name = "{displayName} / {index} / {arguments}")
	@DisplayName("combination")
	static class CombinationTestCases {

		static final List<String> seen = new CopyOnWriteArrayList<>();
		private final int number;
		private final String word;
		private int state;
		CombinationTestCases(@CartesianTest.Values(ints = { 1, 2 }) int number,
				@CartesianTest.Values(strings = { "a", "b" }) String word) {
			this.number = number;
			this.word = word;
		}

		@BeforeEach
		void beforeEach() {
			assertThat(state++).isZero();
		}

		@Test
		void first(TestInfo info) {
			seen.add(number + ":" + word + ":first");
			assertThat(info.getDisplayName()).isEqualTo("first(TestInfo)");
		}

		@Test
		void second() {
			seen.add(number + ":" + word + ":second");
		}

	}

	@CartesianClass
	@TestInstance(Lifecycle.PER_CLASS)
	static class PerClassTestCases {

		static final List<String> seen = new CopyOnWriteArrayList<>();
		private final int number;
		private int count;
		PerClassTestCases(@CartesianTest.Values(ints = { 1, 2 }) int number) {
			this.number = number;
		}

		@Test
		void first() {
			seen.add(number + ":" + ++count);
		}

		@Test
		void second() {
			seen.add(number + ":" + ++count);
		}

	}

	enum Choice {
		A, B
	}

	@CartesianClass
	static class EnumRangeTestCases {

		private final Choice choice;
		private final int number;
		EnumRangeTestCases(@CartesianTest.Enum Choice choice, @IntRangeSource(from = 1, to = 3) int number) {
			this.choice = choice;
			this.number = number;
		}

		@Test
		void valid() {
			assertThat(choice).isIn(Choice.A, Choice.B);
			assertThat(number).isBetween(1, 2);
		}

	}

	@CartesianClass
	static class MethodCartesianTestCases {

		private final int number;
		MethodCartesianTestCases(@CartesianTest.Values(ints = { 1, 2 }) int number) {
			this.number = number;
		}

		@CartesianTest
		void independent(@CartesianTest.Values(strings = { "a", "b" }) String word) {
			assertThat(number).isBetween(1, 2);
			assertThat(word).isIn("a", "b");
		}

	}

	@CartesianClass
	static class EmptyTestCases {

		@Test
		void once() {
		}

	}
	@CartesianClass
	static class MissingSourceTestCases {

		MissingSourceTestCases(int value) {
		}

		@Test
		void never() {
		}

	}
	@CartesianClass(name = " ")
	static class BlankNameTestCases {

		@Test
		void never() {
		}

	}

}
