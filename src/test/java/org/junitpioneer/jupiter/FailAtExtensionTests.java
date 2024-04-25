/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Tests for the FailAt extension")
class FailAtExtensionTests {

	@Test
	@DisplayName("Should not fail test without annotation")
	void shouldNotFailTestWithoutAnnotation() {
		final ExecutionResults results = PioneerTestKit.executeTestMethod(FailAtTestCases.class, "testNoAnnotation");
		assertThat(results).hasSingleStartedTest();
		assertThat(results).hasSingleSucceededTest();
		assertThat(results).hasNumberOfSkippedTests(0);
		assertThat(results).hasNoReportEntries();
	}

	@Test
	@DisplayName("Should enable test with unparsable `date`` string")
	void shouldEnableTestWithUnparsableUntilDateString() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(FailAtTestCases.class, "testUnparsableUntilDateString");
		assertThat(results).hasSingleStartedTest();
		assertThat(results).hasSingleFailedTest();
		assertThat(results).hasNoReportEntries();
	}

	@Test
	@DisplayName("Should fail test with `date` in the past")
	void shouldFailTestWithFailAtDateInThePast() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(FailAtTestCases.class, "testIsAnnotatedWithDateInThePast");
		assertThat(results).hasSingleStartedTest();
		assertThat(results).hasSingleFailedTest();
		assertThat(results).hasSingleReportEntry().firstValue().contains("is after or on the `date`");

	}

	@Test
	@DisplayName("Should not fail a test with `date` in the future")
	void shouldNotFailTestWithFailAtDateInTheFuture() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(FailAtTestCases.class, "testIsAnnotatedWithDateInTheFuture");
		assertThat(results).hasSingleSucceededTest();
		assertThat(results).hasSingleReportEntry().firstValue().contains("2199-01-01", "did not fails the test");
	}

	@Test
	@DisplayName("Should fail nested test with `date` in the past when meta annotated by higher level container")
	void shouldFailNestedTestWithFailAtDateInThPastWhenMetaAnnotated() {
		final ExecutionResults results = PioneerTestKit
				.executeTestMethod(FailAtTestCases.NestedTestCases.class, "shouldRetrieveFromClass");
		assertThat(results).hasSingleFailedContainer();
		assertThat(results).hasNumberOfStartedTests(0);
		assertThat(results).hasSingleReportEntry().firstValue().contains("is after or on the `date`");
	}

	static class FailAtTestCases {

		@Test
		void testNoAnnotation() {

		}

		@Test
		@FailAt(reason = "I don't know how to write dates!", date = "xxxx-yy-zz")
		void testUnparsableUntilDateString() {

		}

		@Test
		@FailAt(reason = "Everything was better in the past!", date = "1993-01-01")
		void testIsAnnotatedWithDateInThePast() {

		}

		@Test
		@FailAt(reason = "Keep on running!", date = "2199-01-01")
		void testIsAnnotatedWithDateInTheFuture() {

		}

		@Nested
		@FailAt(reason = "My child is younger than me, but still old!", date = "1993-01-01")
		class NestedTestCases {

			@Test
			void shouldRetrieveFromClass() {

			}

		}

	}

}
