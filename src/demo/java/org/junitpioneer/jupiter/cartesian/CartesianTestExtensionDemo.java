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
import static org.junitpioneer.jupiter.cartesian.CartesianTest.Enum.Mode.EXCLUDE;
import static org.junitpioneer.jupiter.cartesian.CartesianTest.Enum.Mode.MATCH_ALL;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Enum;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Values;
import org.junitpioneer.jupiter.params.LongRangeSource;
import org.junitpioneer.jupiter.params.ShortRangeSource;

// @formatter:off
public class CartesianTestExtensionDemo {

	// tag::cartesian_simple_demo[]
	@CartesianTest
	void myCartesianTestMethod(
			@Values(ints = { 1, 2 }) int x,
			@Values(ints = { 3, 4 }) int y) {
		// passing test code
	}
	// end::cartesian_simple_demo[]

	// tag::cartesian_annotating_parameters[]
	@CartesianTest
	void testIntChars(
			@ShortRangeSource(from = 1, to = 3, step = 1) short s,
			@Values(strings = { "A", "B" }) String character,
			@Enum ChronoUnit unit) {
		// passing test code
	}
	// end::cartesian_annotating_parameters[]

	// tag::cartesian_combining_values[]
	@CartesianTest
	void testIntChars(
			@Values(ints = { 1, 2, 4 }) int number,
			@Values(strings = { "A", "B" }) String character) {
		// passing test code
	}
	// end::cartesian_combining_values[]

	// tag::cartesian_with_enum[]
	@CartesianTest
	void testWithEnum(@Enum ChronoUnit unit) {
		assertThat(unit).isNotNull();
	}
	// end::cartesian_with_enum[]

	// tag::cartesian_enum_with_type[]
	@CartesianTest
	void testExplicitEnum(
			@Enum(ChronoUnit.class) TemporalUnit unit) {
		assertThat(unit).isNotNull();
	}
	// end::cartesian_enum_with_type[]

	// tag::cartesian_enum_with_type_and_names[]
	@CartesianTest
	void testEnumNames(
			@Enum(names = { "DAYS", "HOURS" }) ChronoUnit unit) {
		assertThat(EnumSet.of(ChronoUnit.DAYS, ChronoUnit.HOURS)).contains(unit);
	}
	// end::cartesian_enum_with_type_and_names[]

	// tag::cartesian_enum_with_mode[]
	@CartesianTest
	void testWithEnumModes(
			@Enum(mode = EXCLUDE, names = { "ERAS", "FOREVER" })
			ChronoUnit unit) {
		assertThat(EnumSet.of(ChronoUnit.ERAS, ChronoUnit.FOREVER)).doesNotContain(unit);
	}
	// end::cartesian_enum_with_mode[]

	// tag::cartesian_enum_with_regex[]
	@CartesianTest
	void testWithEnumRegex(
			@Enum(mode = MATCH_ALL, names = "^.*DAYS$") ChronoUnit unit) {
		assertThat(unit.name()).endsWith("DAYS");
	}
	// end::cartesian_enum_with_regex[]

	// tag::cartesian_enum_with_enums[]
	enum MyEnum {
		ONE, TWO, THREE
	}

	enum AnotherEnum {
		ALPHA, BETA, GAMMA, DELTA
	}

	@CartesianTest
	void testEnumValues(
			@Enum MyEnum myEnum,
			@Enum(names = { "ALPHA", "DELTA" }, mode = Enum.Mode.EXCLUDE)
			AnotherEnum anotherEnum) {
		// passing test code
	}
	// end::cartesian_enum_with_enums[]

	// tag::cartesian_enum_with_range_sources[]
	@CartesianTest
	void testShortAndLong(
			@ShortRangeSource(from = 1, to = 3, step = 1) short s,
			@LongRangeSource(from = 0L, to = 2L, step = 1, closed = true) long l) {
		// passing test code
	}
	// end::cartesian_enum_with_range_sources[]

	// tag::cartesian_argument_sets[]
	@CartesianTest
	@CartesianTest.MethodFactory("stringClassTimeUnitFactory")
	void testStringClassTimeUnit(
			String string,
			Class<?> clazz,
			TimeUnit unit) {
		// passing test code
	}

	static ArgumentSets stringClassTimeUnitFactory() {
		return ArgumentSets
				.argumentsForFirstParameter(
						"Alpha", "Omega")
				.argumentsForNextParameter(
						Runnable.class, Cloneable.class, Predicate.class)
				.argumentsForNextParameter(
						TimeUnit.DAYS, TimeUnit.HOURS);
	}
	// end::cartesian_argument_sets[]

	// tag::cartesian_argument_sets_reuse[]
	@CartesianTest
	@CartesianTest.MethodFactory("stringIntFactory")
	void testStringInt(String string, int i) {
		// passing test code
	}

	@CartesianTest
	@CartesianTest.MethodFactory("stringIntFactory")
	void testSameStringInt(String string, int i) {
		// different passing test code
	}

	static ArgumentSets stringIntFactory() {
		return ArgumentSets
				.argumentsForFirstParameter("Mercury", "Earth", "Venus")
				.argumentsForNextParameter(1, 12, 144);
	}
	// end::cartesian_argument_sets_reuse[]

	static class MisconfiguredExamples {

		// tag::cartesian_bad_examples[]
		@CartesianTest
		@CartesianTest.MethodFactory("unsuitableStringIntFactory")
		void testHasTooFewParameters(String string) {
			// fails because we try to supply a non-existent integer parameter
		}

		@CartesianTest
		@CartesianTest.MethodFactory("unsuitableStringIntFactory")
		void testHasTooManyParameters(String string, int i, boolean b) {
			// fails because the boolean parameter is not resolved
		}

		@CartesianTest
		@CartesianTest.MethodFactory("unsuitableStringIntFactory")
		void testHasParametersInWrongOrder(int i, String string) {
			// fails because the factory method declared
			// parameter sets in the wrong order
		}

		@CartesianTest
		@CartesianTest.MethodFactory("resolveTestReporterParam")
		void testHasConflictingParameters(String string, TestReporter info) {
			// fails because both the factory method and JUnit
			// try to inject TestReporter
		}

		static ArgumentSets unsuitableStringIntFactory() {
			return ArgumentSets
					.argumentsForFirstParameter("A", "B", "C")
					.argumentsForNextParameter(1, 2, 3);
		}

		static ArgumentSets resolveTestReporterParam() {
			return ArgumentSets
					.argumentsForFirstParameter("A", "B", "C")
					// in this case MyTestReporter implements TestReporter
					.argumentsForNextParameter(new MyTestReporter());
		}
		// end::cartesian_bad_examples[]

	}

	// tag::cartesian_argument_sets_ints_annotation[]
	@Target(ElementType.PARAMETER)
	@Retention(RetentionPolicy.RUNTIME)
	@CartesianArgumentsSource(IntArgumentsProvider.class)
	public @interface Ints {

		int[] value();

	}
	// end::cartesian_argument_sets_ints_annotation[]

	// tag::cartesian_argument_sets_int_argument_provider[]
	class IntArgumentsProvider
			implements CartesianParameterArgumentsProvider {

		@Override
		public Stream<Integer> provideArguments(
				ExtensionContext context, Parameter parameter) {
			Ints source = Objects.requireNonNull(
					parameter.getAnnotation(Ints.class));
			return Arrays.stream(source.value()).boxed();
		}

	}
	// end::cartesian_argument_sets_int_argument_provider[]

	// tag::cartesian_testWithCustomDisplayName[]
	@CartesianTest(name = "{index} => first bit: {0} second bit: {1}")
	@DisplayName("Basic bit test")
	void testWithCustomDisplayName(
			@Values(strings = { "0", "1" }) String a,
			@Values(strings = { "0", "1" }) String b) {
		// passing test code
	}
	// end::cartesian_testWithCustomDisplayName[]

	// tag::cartesian_argument_sets_with_non_static_factory[]
	@Nested
	// with this lifecycle annotation the factory method can be non-static
	@TestInstance(Lifecycle.PER_CLASS)
	class MyCartesianTests {

		@CartesianTest
		@CartesianTest.MethodFactory("provideArguments")
		void testNeedingArguments(String string, int i) {
			// passing test code
		}

		// this provider method doesn't have to be static
		ArgumentSets provideArguments() {
			return ArgumentSets
					.argumentsForFirstParameter(
							"Mercury", "Earth", "Venus")
					.argumentsForNextParameter(1, 12, 144);
		}

	}
	// end::cartesian_argument_sets_with_non_static_factory[]

}
// @formatter:on
