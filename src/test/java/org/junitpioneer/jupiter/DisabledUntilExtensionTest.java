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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;
import org.junitpioneer.testkit.assertion.PioneerAssert;

@DisplayName("Tests for the DisabledUntil extension")
class DisabledUntilExtensionTest {

	@Test
	@DisplayName("Should enable test without annotation")
	void shouldEnableTestWithoutAnnotation() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisabledUntilExtensionTest.DisabledUntilDummyTestClass.class, "testNoAnnotation");
		PioneerAssert.assertThat(results).hasNumberOfStartedTests(1);
		PioneerAssert.assertThat(results).hasNumberOfSucceededTests(1);
		PioneerAssert.assertThat(results).hasNumberOfSkippedTests(0);
		PioneerAssert.assertThat(results).hasNumberOfReportEntries(0);
	}

	@Test
	@DisplayName("Should enable test with unparseable untilDate string")
	void shouldEnableTestWithUnparseableUntilDateString() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisabledUntilExtensionTest.DisabledUntilDummyTestClass.class,
					"testUnparseableUntilDateString");
		PioneerAssert.assertThat(results).hasNumberOfStartedTests(1);
		PioneerAssert.assertThat(results).hasNumberOfSucceededTests(1);
		PioneerAssert.assertThat(results).hasNumberOfSkippedTests(0);
		PioneerAssert.assertThat(results).hasNumberOfReportEntries(0);
	}

	@Test
	@DisplayName("Should enable test with untilDate in the past")
	void shouldEnableTestWithUntilDateInThePast() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisabledUntilExtensionTest.DisabledUntilDummyTestClass.class,
					"testIsAnnotatedWithDateInThePast");
		PioneerAssert.assertThat(results).hasNumberOfStartedTests(1);
		PioneerAssert.assertThat(results).hasNumberOfSucceededTests(1);
		PioneerAssert.assertThat(results).hasNumberOfSkippedTests(0);
		PioneerAssert.assertThat(results).hasNumberOfReportEntries(1);
	}

	@Test
	@DisplayName("Should disable test with untilDate in the future")
	void shouldDisableTestWithUntilDateInTheFuture() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisabledUntilExtensionTest.DisabledUntilDummyTestClass.class,
					"testIsAnnotatedWithDateInTheFuture");
		PioneerAssert.assertThat(results).hasNumberOfStartedTests(0);
		PioneerAssert.assertThat(results).hasNumberOfSucceededTests(0);
		PioneerAssert.assertThat(results).hasNumberOfSkippedTests(1);
		PioneerAssert.assertThat(results).hasNumberOfReportEntries(0);
	}

	@Test
	@DisplayName("Should disable nested test with untilDate in the future when meta annotated by higher level container")
	void shouldDisableNestedTestWithUntilDateInTheFutureWhenMetaAnnotated() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(DisabledUntilExtensionTest.DisabledUntilDummyTestClass.NestedDummyTestClass.class,
					"shouldRetrieveFromClass");
		PioneerAssert.assertThat(results).hasNumberOfSkippedContainers(1); // NestedDummyTestClass is skipped as container
		PioneerAssert.assertThat(results).hasNumberOfStartedTests(0);
		PioneerAssert.assertThat(results).hasNumberOfSucceededTests(0);
		PioneerAssert.assertThat(results).hasNumberOfSkippedTests(0);
		PioneerAssert.assertThat(results).hasNumberOfReportEntries(0);
	}

	static class DisabledUntilDummyTestClass {

		@Test
		void testNoAnnotation() {

		}

		@Test
		@DisabledUntil(reason = "Boom!", untilDate = "xxxx-yy-zz")
		void testUnparseableUntilDateString() {

		}

		@Test
		@DisabledUntil(reason = "Zoink!", untilDate = "1993-01-01")
		void testIsAnnotatedWithDateInThePast() {

		}

		@Test
		@DisabledUntil(reason = "Ka-pow!", untilDate = "2199-01-01")
		void testIsAnnotatedWithDateInTheFuture() {

		}

		@Nested
		@DisabledUntil(reason = "Yowza!", untilDate = "2199-01-01")
		class NestedDummyTestClass {

			@Test
			void shouldRetrieveFromClass() {

			}

		}

	}

}
