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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

/**
 * Tests for the {@link RangeSourceProvider}.
 */
public class RangeSourceProviderTests extends AbstractPioneerTestEngineTests {

	private Integer[] expectedValues;

	@BeforeEach
	public void populateValues() {
		expectedValues = IntStream.concat(IntStream.range(0, 10),
			IntStream.rangeClosed(-7, -3).filter(i -> i % 2 == -1)).boxed().toArray(Integer[]::new);
	}

	@Test
	public void assertAllValuesSupplied() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(RangeTestCases.class);

		List<Number> actualValues = eventRecorder.eventStream().filter(
			e -> e.getType() == ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED).map(e -> e.getTestDescriptor()).map(
				TestDescriptor::getDisplayName).map(Integer::parseInt).collect(Collectors.toList());

		assertThat(actualValues).containsExactlyInAnyOrder(expectedValues);
	}

	@Nested
	class RangeTestCases {
		@ParameterizedTest(name = "{0}")
		@IntRangeSource(from = 0, to = 10)
		public void ascendingInt(int param) {
		}

		@ParameterizedTest(name = "{0}")
		@IntRangeSource(from = -3, to = -7, step = -2, closed = true)
		public void descendingInt(int param) {
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
}
