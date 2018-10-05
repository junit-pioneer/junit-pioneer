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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

/**
 * Tests for the {@link RangeSource} annotation and the underlying {@link RangeSourceProvider}.
 */
public class RangeSourceTests extends AbstractPioneerTestEngineTests {

	private Integer[] expectedValues;

	@BeforeEach
	public void populateValues() {
		expectedValues = IntStream.concat(IntStream.range(0, 10),
			IntStream.rangeClosed(-7, -3).filter(i -> i % 2 == -1)).boxed().toArray(Integer[]::new);
	}

	@Test
	public void assertAllValuesSupplied() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(RangeTestCases.class);

		List<Integer> actualValues = eventRecorder.eventStream().filter(
			e -> e.getType() == ExecutionEvent.Type.DYNAMIC_TEST_REGISTERED).map(e -> e.getTestDescriptor()).map(
				TestDescriptor::getDisplayName).map(Integer::parseInt).collect(Collectors.toList());

		assertThat(actualValues).containsExactlyInAnyOrder(expectedValues);
	}

	@Nested
	class RangeTestCases {
		@ParameterizedTest(name = "{0}")
		@RangeSource(from = 0, to = 10)
		public void ascending(int param) {
		}

		@ParameterizedTest(name = "{0}")
		@RangeSource(from = -3, to = -7, step = -2, closed = true)
		public void descending(int param) {
		}

		@Test
		public void zeroStep() {
			RangeSource zeroStepRange = mock(RangeSource.class);
			when(zeroStepRange.step()).thenReturn(0);

			RangeSourceProvider provider = new RangeSourceProvider();
			PreconditionViolationException e = assertThrows(PreconditionViolationException.class,
				() -> provider.accept(zeroStepRange));
			assertEquals("Illegal RangeSource. The step cannot be 0.", e.getMessage());
		}

		@Test
		public void illegalStep() {
			RangeSource zeroStepRange = mock(RangeSource.class);
			when(zeroStepRange.step()).thenReturn(1);
			when(zeroStepRange.from()).thenReturn(10);
			when(zeroStepRange.to()).thenReturn(0);

			RangeSourceProvider provider = new RangeSourceProvider();
			PreconditionViolationException e = assertThrows(PreconditionViolationException.class,
				() -> provider.accept(zeroStepRange));
			assertEquals("Illegal RangeSource. There's no way to get from 10 to 0 with a step of 1.", e.getMessage());
		}

		@Test
		public void emptyRange() {
			RangeSource zeroStepRange = mock(RangeSource.class);
			when(zeroStepRange.step()).thenReturn(1);
			when(zeroStepRange.from()).thenReturn(7);
			when(zeroStepRange.to()).thenReturn(7);

			RangeSourceProvider provider = new RangeSourceProvider();
			PreconditionViolationException e = assertThrows(PreconditionViolationException.class,
				() -> provider.accept(zeroStepRange));
			assertEquals("Illegal RangeSource. Equal from and to will produce an empty range.", e.getMessage());
		}
	}
}
