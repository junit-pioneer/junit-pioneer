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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ALWAYS;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_FAILURE;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_SUCCESS;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.opentest4j.TestAbortedException;

/**
 * Edgar Allan Poe: The Raven is in the public domain.
 */
@DisplayName("ReportEntry extension")
public class ReportEntryExtensionTest extends AbstractJupiterTestEngineTests {

	@Test
	@DisplayName("reports given explicit key and value")
	void explicitKey_keyAndValueAreReported() {
		ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "explicitKey");

		List<Map<String, String>> reportEntries = TestUtils.reportEntries(recorder);
		assertThat(reportEntries).hasSize(1);
		Map<String, String> reportEntry = reportEntries.get(0);
		assertThat(reportEntry).hasSize(1);
		assertThat(reportEntry).containsExactly(TestUtils.entryOf("Crow2", "While I pondered weak and weary"));
	}

	@Test
	@DisplayName("reports given explicit value with default key 'value'")
	void implicitKey_keyIsNamedValue() {
		ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "implicitKey");

		List<Map<String, String>> reportEntries = TestUtils.reportEntries(recorder);
		assertThat(reportEntries).hasSize(1);
		assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
			assertThat(reportEntry).hasSize(1);
			assertThat(reportEntry).containsExactly(TestUtils.entryOf("value", "Once upon a midnight dreary"));
		});
	}

	@Test
	@DisplayName("fails when given an empty key explicitly")
	void emptyKey_fails() {
		ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "emptyKey");

		assertThat(recorder.getFailedTestFinishedEvents()).hasSize(1);
		assertThat(getFirstFailuresThrowable(recorder).getMessage())
				.contains("Report entries can't have blank key or value",
					"Over many a quaint and curious volume of forgotten lore");
	}

	@Test
	@DisplayName("fails when given an empty value")
	void emptyValue_fails() {
		ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "emptyValue");

		assertThat(recorder.getFailedTestFinishedEvents()).hasSize(1);
		assertThat(getFirstFailuresThrowable(recorder).getMessage())
				.contains("Report entries can't have blank key or value", "While I nodded, nearly napping");
	}

	@Test
	@DisplayName("logs each value as individual entry when annotation is repeated")
	void repeatedAnnotation_logEachKeyValuePairAsIndividualEntry() {
		ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "repeatedAnnotation");

		List<Map<String, String>> reportEntries = TestUtils.reportEntries(recorder);

		assertAll("Verifying report entries " + reportEntries, //
			() -> assertThat(reportEntries).hasSize(3),
			() -> assertThat(reportEntries).extracting(Map::size).containsExactlyInAnyOrder(1, 1, 1),
			() -> assertThat(reportEntries)
					.extracting(entry -> entry.get("value"))
					.containsExactlyInAnyOrder("suddenly there came a tapping", "As if some one gently rapping",
						"rapping at my chamber door"));
	}

	@Nested
	@DisplayName("with explicitly set 'when' parameter")
	class PublishConditionTests {

		@Test
		@DisplayName("logs report entry regardless of the outcome when publish condition is ALWAYS")
		void conditional_logAlwaysWhenTestRuns() {
			ExecutionEventRecorder successRecorder = executeTestsForMethod(ReportEntriesTest.class, "always_success");

			List<Map<String, String>> successReportEntries = TestUtils.reportEntries(successRecorder);

			assertThat(successRecorder.getSuccessfulTestFinishedEvents()).hasSize(1);
			assertThat(successReportEntries.get(0)).satisfies(reportEntry -> {
				assertThat(reportEntry).hasSize(1);
				assertThat(reportEntry).containsExactly(entryOf("value", "'Tis some visitor', I muttered"));
			});

			ExecutionEventRecorder failureRecorder = executeTestsForMethod(ReportEntriesTest.class, "always_failure");

			List<Map<String, String>> failureReportEntries = TestUtils.reportEntries(failureRecorder);

			assertThat(failureRecorder.getFailedTestFinishedEvents()).hasSize(1);
			assertThat(failureReportEntries.get(0)).satisfies(reportEntry -> {
				assertThat(reportEntry).hasSize(1);
				assertThat(reportEntry).containsExactly(entryOf("value", "'Tapping at my chamber door' -"));
			});
		}

		@Test
		@DisplayName("does not log if the test does not run")
		void conditional_doNotLogOnDisabled() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "always_disabled");

			List<Map<String, String>> reportEntries = TestUtils.reportEntries(recorder);

			assertThat(recorder.getTestFinishedCount()).isEqualTo(0);
			assertThat(reportEntries).isEmpty();
		}

		@Test
		@DisplayName("logs after success, if publish condition is ON_SUCCESS")
		void conditional_logOnSuccess() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "onSuccess_success");

			List<Map<String, String>> reportEntries = TestUtils.reportEntries(recorder);

			assertThat(recorder.getSuccessfulTestFinishedEvents()).hasSize(1);
			assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
				assertThat(reportEntry).hasSize(1);
				assertThat(reportEntry).containsExactly(entryOf("value", "Ah, distinctly I remember"));
			});
		}

		@Test
		@DisplayName("does not logs after failure, if publish condition is ON_SUCCESS")
		void conditional_doNotLogOnFailure() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "onSuccess_failure");

			List<Map<String, String>> reportEntries = TestUtils.reportEntries(recorder);

			assertThat(recorder.getFailedTestFinishedEvents()).hasSize(1);
			assertThat(reportEntries).isEmpty();
		}

		@Test
		@DisplayName("logs after failure, if publish condition is ON_FAILURE")
		void conditional_logOnFailure() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "onFailure_failure");

			List<Map<String, String>> reportEntries = TestUtils.reportEntries(recorder);

			assertThat(recorder.getFailedTestFinishedEvents()).hasSize(1);
			assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
				assertThat(reportEntry).hasSize(1);
				assertThat(reportEntry).containsExactly(entryOf("value", "And each separate dying ember"));
			});
		}

		@Test
		@DisplayName("logs entries if test was aborted, treating it as a failure if publish condition is ON_FAILURE")
		void conditional_logOnAbort() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "onFailure_abort");

			List<Map<String, String>> reportEntries = TestUtils.reportEntries(recorder);

			assertThat(recorder.getTestAbortedCount()).isEqualTo(1);
			assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
				assertThat(reportEntry).hasSize(1);
				assertThat(reportEntry).containsExactly(entryOf("value", "Eagerly I wished the morrow;"));
			});
		}

		@Test
		@DisplayName("does not log after success, if publish condition is ON_FAILURE")
		void conditional_doNotLogOnSuccess() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "onFailure_success");

			List<Map<String, String>> reportEntries = TestUtils.reportEntries(recorder);

			assertThat(recorder.getSuccessfulTestFinishedEvents()).hasSize(1);
			assertThat(reportEntries).isEmpty();
		}

		@Test
		@DisplayName("logs entries independently on success, based on publish condition")
		void conditional_logOnSuccessIndependently() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "repeatedSuccess");

			List<Map<String, String>> reportEntries = TestUtils.reportEntries(recorder);

			assertThat(recorder.getSuccessfulTestFinishedEvents()).hasSize(1);
			assertAll("Verifying report entries " + reportEntries, //
				() -> assertThat(reportEntries).hasSize(2),
				() -> assertThat(reportEntries).extracting(Map::size).containsExactlyInAnyOrder(1, 1),
				() -> assertThat(reportEntries)
						.extracting(entry -> entry.get("value"))
						.containsExactlyInAnyOrder("Eagerly I wished the morrow;", "vainly I had sought to borrow"));
		}

		@Test
		@DisplayName("logs entries independently on failure, based on publish condition")
		void conditional_logOnFailureIndependently() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "repeatedFailure");

			List<Map<String, String>> reportEntries = TestUtils.reportEntries(recorder);

			assertThat(recorder.getFailedTestFinishedEvents()).hasSize(1);
			assertAll("Verifying report entries " + reportEntries, //
				() -> assertThat(reportEntries).hasSize(2),
				() -> assertThat(reportEntries).extracting(Map::size).containsExactlyInAnyOrder(1, 1),
				() -> assertThat(reportEntries)
						.extracting(entry -> entry.get("value"))
						.containsExactlyInAnyOrder("For the rare and radiant maiden", "Nameless here for evermore"));
		}

	}

	static class ReportEntriesTest {

		@Test
		@ReportEntry(key = "Crow2", value = "While I pondered weak and weary")
		void explicitKey() {
		}

		@Test
		@ReportEntry("Once upon a midnight dreary")
		void implicitKey() {
		}

		@Test
		@ReportEntry(key = "", value = "Over many a quaint and curious volume of forgotten lore")
		void emptyKey() {
		}

		@Test
		@ReportEntry(key = "While I nodded, nearly napping", value = "")
		void emptyValue() {
		}

		@Test
		@ReportEntry("suddenly there came a tapping")
		@ReportEntry("As if some one gently rapping")
		@ReportEntry("rapping at my chamber door")
		void repeatedAnnotation() {
		}

		@Test
		@ReportEntry(value = "'Tis some visitor', I muttered", when = ALWAYS)
		void always_success() {
		}

		@Test
		@ReportEntry(value = "'Tapping at my chamber door' -", when = ALWAYS)
		void always_failure() {
			fail();
		}

		@Test
		@Disabled("wanted here for showing that report entries are disabled")
		@ReportEntry(value = "'Only this and nothing more.'", when = ALWAYS)
		void always_disabled() {
		}

		@Test
		@ReportEntry(value = "Ah, distinctly I remember", when = ON_SUCCESS)
		void onSuccess_success() {
		}

		@Test
		@ReportEntry(value = "it was in the bleak December", when = ON_SUCCESS)
		void onSuccess_failure() {
			fail();
		}

		@Test
		@ReportEntry(value = "And each separate dying ember", when = ON_FAILURE)
		void onFailure_failure() {
			fail();
		}

		@Test
		@ReportEntry(value = "wrought its ghost upon the floor", when = ON_FAILURE)
		void onFailure_success() {
		}

		@Test
		@ReportEntry(value = "Eagerly I wished the morrow;", when = ON_FAILURE)
		void onFailure_abort() {
			throw new TestAbortedException();
		}

		@Test
		@ReportEntry(value = "Eagerly I wished the morrow;", when = ALWAYS)
		@ReportEntry(value = "vainly I had sought to borrow", when = ON_SUCCESS)
		@ReportEntry(value = "From my books surcease of sorrow-—sorrow for the lost Lenore-", when = ON_FAILURE)
		void repeatedSuccess() {
		}

		@Test
		@ReportEntry(value = "For the rare and radiant maiden", when = ALWAYS)
		@ReportEntry(value = "whom the angels name Lenore—", when = ON_SUCCESS)
		@ReportEntry(value = "Nameless here for evermore", when = ON_FAILURE)
		void repeatedFailure() {
			fail();
		}

	}

}
