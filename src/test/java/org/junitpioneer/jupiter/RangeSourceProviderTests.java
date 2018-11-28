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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

/**
 * Tests for the {@link RangeSourceProvider}.
 */
public class RangeSourceProviderTests extends AbstractPioneerTestEngineTests {

	private Number[] expectedValues;

	@BeforeEach
	public void populateValues() {
		expectedValues = Stream.of(IntStream.range(0, 10).mapToObj(i -> (byte) i),
			IntStream.rangeClosed(-7, -3).mapToObj(i -> (byte) i), IntStream.range(130, 136).mapToObj(i -> (short) i),
			IntStream.rangeClosed(-144, -140).filter(i -> i % 2 == 0).mapToObj(i -> (short) i),
			IntStream.range(40_000, 40_005).boxed(),
			IntStream.rangeClosed(-42_000, -40_000).filter(i -> i % 2000 == 0).boxed(),
			LongStream.range(6_000_000_000L, 6_000_000_003L).boxed(),
			LongStream.rangeClosed(-6_000_000_400L, -6_000_000_000L).filter(l -> l % 100L == 0L).boxed(),
			IntStream.range(0, 3).mapToObj(i -> i + 2.2F), IntStream.rangeClosed(3, 6).mapToObj(i -> i * -0.1F),
			IntStream.range(0, 2).mapToObj(i -> i + 8.4),
			IntStream.rangeClosed(-3, -2).mapToObj(i -> (double) i)).flatMap(Function.identity()).toArray(
				Number[]::new);
	}

	@Test
	public void assertAllValuesSupplied() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(RangeTestCases.class);

		List<Number> actualValues = eventRecorder.eventStream().filter(
			e -> e.getType() == ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED).map(
				e -> e.getTestDescriptor().getDisplayName()).map(RangeSourceProviderTests::displayNameToNumber).collect(
					Collectors.toList());

		assertThat(actualValues).containsExactlyInAnyOrder(expectedValues);
	}

	private static Number displayNameToNumber(String displayName) {
		String[] split = displayName.split(" ");
		String type = split[0];
		String value = split[1];

		try {
			Class<?> valueClass = Class.forName("java.lang." + type);
			Method parseMethod = valueClass.getMethod("valueOf", String.class);
			return (Number) parseMethod.invoke(null, value);
		}
		catch (Exception ignore) {
			return null;
		}
	}

	@Nested
	class RangeTestCases {
		@ParameterizedTest(name = "Byte {0}")
		@ByteRangeSource(from = 0, to = 10)
		public void ascendingByte(byte param) {
		}

		@ParameterizedTest(name = "Byte {0}")
		@ByteRangeSource(from = -3, to = -7, step = -1, closed = true)
		public void descendingByte(byte param) {
		}

		@ParameterizedTest(name = "Short {0}")
		@ShortRangeSource(from = 130, to = 136)
		public void ascendingShort(short param) {
		}

		@ParameterizedTest(name = "Short {0}")
		@ShortRangeSource(from = -140, to = -144, step = -2, closed = true)
		public void descendingShort(short param) {
		}

		@ParameterizedTest(name = "Integer {0}")
		@IntRangeSource(from = 40_000, to = 40_005)
		public void ascendingInt(int param) {
		}

		@ParameterizedTest(name = "Integer {0}")
		@IntRangeSource(from = -40_000, to = -42_000, step = -2000, closed = true)
		public void descendingInt(int param) {
		}

		@ParameterizedTest(name = "Long {0}")
		@LongRangeSource(from = 6_000_000_000L, to = 6_000_000_003L)
		public void ascendingLong(long param) {
		}

		@ParameterizedTest(name = "Long {0}")
		@LongRangeSource(from = -6_000_000_000L, to = -6_000_000_400L, step = -100, closed = true)
		public void descendingLong(long param) {
		}

		@ParameterizedTest(name = "Float {0}")
		@FloatRangeSource(from = 2.2F, to = 5.2F)
		public void ascendingFloat(float param) {
		}

		@ParameterizedTest(name = "Float {0}")
		@FloatRangeSource(from = -0.3F, to = -0.6F, step = -0.1F, closed = true)
		public void descendingFloat(float param) {
		}

		@ParameterizedTest(name = "Double {0}")
		@DoubleRangeSource(from = 8.4, to = 10.4)
		public void ascendingDouble(double param) {
		}

		@ParameterizedTest(name = "Double {0}")
		@DoubleRangeSource(from = -2.0, to = -3.0, step = -1, closed = true)
		public void descendingDouble(double param) {
		}

		@Test
		public void noAnnotation() throws NoSuchMethodException {
			Object dummy = new Object() {
				@Override
				public String toString() {
					return "";
				}
			};

			ExtensionContext ec = spy(ExtensionContext.class);
			when(ec.getElement()).thenReturn(Optional.of(dummy.getClass().getMethod("toString")));

			RangeSourceProvider provider = new RangeSourceProvider();
			PreconditionViolationException e = assertThrows(PreconditionViolationException.class,
				() -> provider.provideArguments(ec));
			assertEquals("Expected exactly one annotation to provide an ArgumentSource, found 0.", e.getMessage());
		}

		@Test
		public void zeroStep() throws NoSuchMethodException {
			Object dummy = new Object() {
				@IntRangeSource(from = 1, to = 2, step = 0)
				@Override
				public String toString() {
					return "";
				}
			};

			ExtensionContext ec = spy(ExtensionContext.class);
			when(ec.getElement()).thenReturn(Optional.of(dummy.getClass().getMethod("toString")));

			RangeSourceProvider provider = new RangeSourceProvider();
			PreconditionViolationException e = assertThrows(PreconditionViolationException.class,
				() -> provider.provideArguments(ec));
			assertEquals("Illegal range. The step cannot be 0.", e.getMessage());
		}

		@Test
		public void illegalStep() throws NoSuchMethodException {
			Object dummy = new Object() {
				@IntRangeSource(from = 10, to = 0, step = 1)
				@Override
				public String toString() {
					return "";
				}
			};

			ExtensionContext ec = spy(ExtensionContext.class);
			when(ec.getElement()).thenReturn(Optional.of(dummy.getClass().getMethod("toString")));

			RangeSourceProvider provider = new RangeSourceProvider();
			PreconditionViolationException e = assertThrows(PreconditionViolationException.class,
				() -> provider.provideArguments(ec));
			assertEquals("Illegal range. There's no way to get from 10.000000 to 0.000000 with a step of 1.000000.",
				e.getMessage());
		}

		@Test
		public void emptyRange() throws NoSuchMethodException {
			Object dummy = new Object() {
				@IntRangeSource(from = 7, to = 7, step = 1)
				@Override
				public String toString() {
					return "";
				}
			};

			ExtensionContext ec = spy(ExtensionContext.class);
			when(ec.getElement()).thenReturn(Optional.of(dummy.getClass().getMethod("toString")));

			RangeSourceProvider provider = new RangeSourceProvider();
			PreconditionViolationException e = assertThrows(PreconditionViolationException.class,
				() -> provider.provideArguments(ec));

			assertEquals("Illegal range. Equal from and to will produce an empty range.", e.getMessage());
		}
	}

	@Test
	public void twoAnnotations() throws NoSuchMethodException {
		Object dummy = new Object() {
			@LongRangeSource(from = 1, to = 2, step = 1)
			@IntRangeSource(from = 1, to = 2, step = 1)
			@Override
			public String toString() {
				return "";
			}
		};

		ExtensionContext ec = spy(ExtensionContext.class);
		when(ec.getElement()).thenReturn(Optional.of(dummy.getClass().getMethod("toString")));

		RangeSourceProvider provider = new RangeSourceProvider();
		PreconditionViolationException e = assertThrows(PreconditionViolationException.class,
			() -> provider.provideArguments(ec));

		assertEquals("Expected exactly one annotation to provide an ArgumentSource, found 2.", e.getMessage());
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ArgumentsSource(RangeSourceProvider.class)
	private @interface InvalidRangeSource {
		int from() default 1;

		double to() default 2;

		int step() default 1;

		boolean closed() default false;
	}
}
