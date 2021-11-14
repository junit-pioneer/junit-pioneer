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
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Tests for the DisabledUntil extension")
class DisabledUntilExtensionTests {

	@Test
	@DisplayName("Should enable test without annotation")
	void shouldEnableTestWithoutAnnotation() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisabledUntilExtensionTests.DisabledUntilDummyTestClass.class, "testNoAnnotation");
		assertThat(results).hasSingleStartedTest();
		assertThat(results).hasSingleSucceededTest();
		assertThat(results).hasNumberOfSkippedTests(0);
		assertThat(results).hasNoReportEntries();
	}

	@Test
	@DisplayName("Should enable test with unparsable `date`` string")
	void shouldEnableTestWithUnparsableUntilDateString() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisabledUntilExtensionTests.DisabledUntilDummyTestClass.class,
					"testUnparsableUntilDateString");
		assertThat(results).hasSingleStartedTest();
		assertThat(results).hasSingleFailedTest();
		assertThat(results).hasNumberOfSkippedTests(0);
		assertThat(results).hasNoReportEntries();
	}

	@Test
	@DisplayName("Should enable test with `date` in the past")
	void shouldEnableTestWithUntilDateInThePast() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisabledUntilExtensionTests.DisabledUntilDummyTestClass.class,
					"testIsAnnotatedWithDateInThePast");
		assertThat(results).hasSingleStartedTest();
		assertThat(results).hasSingleSucceededTest();
		assertThat(results).hasNumberOfSkippedTests(0);
		assertThat(results)
				.hasSingleReportEntry()
				.andThen(entry -> assertThat(entry.getValue())
						.contains("1993-01-01", LocalDate.now().format(DateTimeFormatter.ISO_DATE)));
	}

	@Test
	@DisplayName("Should disable test with `date` in the future")
	void shouldDisableTestWithUntilDateInTheFuture() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisabledUntilExtensionTests.DisabledUntilDummyTestClass.class,
					"testIsAnnotatedWithDateInTheFuture");
		assertThat(results).hasNumberOfStartedTests(0);
		assertThat(results).hasSingleSkippedTest();
		assertThat(results).hasNoReportEntries();
	}

	@Test
	@DisplayName("Should disable nested test with `date` in the future when meta annotated by higher level container")
	void shouldDisableNestedTestWithUntilDateInTheFutureWhenMetaAnnotated() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisabledUntilExtensionTests.DisabledUntilDummyTestClass.NestedDummyTestClass.class,
					"shouldRetrieveFromClass");
		assertThat(results).hasSingleSkippedContainer(); // NestedDummyTestClass is skipped as container
		assertThat(results).hasNumberOfStartedTests(0);
		assertThat(results).hasNumberOfSkippedTests(0);
		assertThat(results).hasNoReportEntries();
	}

	static class DisabledUntilDummyTestClass {

		@Test
		void testNoAnnotation() {

		}

		@Test
		@DisabledUntil(reason = "Boom!", date = "xxxx-yy-zz")
		void testUnparsableUntilDateString() {

		}

		@Test
		@DisabledUntil(reason = "Zoink!", date = "1993-01-01")
		void testIsAnnotatedWithDateInThePast() {

		}

		@Test
		@DisabledUntil(reason = "Ka-pow!", date = "2199-01-01")
		void testIsAnnotatedWithDateInTheFuture() {

		}

		@Nested
		@DisabledUntil(reason = "Yowza!", date = "2199-01-01")
		class NestedDummyTestClass {

			@Test
			void shouldRetrieveFromClass() {

			}

		}

	}

}
