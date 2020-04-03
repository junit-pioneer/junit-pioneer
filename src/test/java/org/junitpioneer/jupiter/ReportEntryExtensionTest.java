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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.*;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

/**
 * Edgar Allan Poe: The Raven is in the public domain.
 */
@DisplayName("ReportEntry extension")
public class ReportEntryExtensionTest extends AbstractJupiterTestEngineTests {

	@Test
	@DisplayName("reports given explicit key and value")
	void explicitKey_keyAndValueAreReported() {
		ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "explicitKey");

		List<Map<String, String>> reportEntries = reportEntries(recorder);
		assertThat(reportEntries).hasSize(1);
		Map<String, String> reportEntry = reportEntries.get(0);
		assertThat(reportEntry).hasSize(1);
		assertThat(reportEntry).containsExactly(entryOf("Crow2", "While I pondered weak and weary"));
	}

	@Test
	@DisplayName("reports given explicit value with default key 'value'")
	void implicitKey_keyIsNamedValue() {
		ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "implicitKey");

		List<Map<String, String>> reportEntries = reportEntries(recorder);
		assertThat(reportEntries).hasSize(1);
		assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
			assertThat(reportEntry).hasSize(1);
			assertThat(reportEntry).containsExactly(entryOf("value", "Once upon a midnight dreary"));
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

		List<Map<String, String>> reportEntries = reportEntries(recorder);

		assertAll("Verifying report entries " + reportEntries, //
			() -> assertThat(reportEntries).hasSize(3),
			() -> assertThat(reportEntries).extracting(Map::size).containsExactlyInAnyOrder(1, 1, 1),
			() -> assertThat(reportEntries)
					.extracting(entry -> entry.get("value"))
					.containsExactlyInAnyOrder("suddenly there came a tapping", "As if some one gently rapping",
						"rapping at my chamber door"));
	}

	@Nested
	@DisplayName("with explicitly set when parameter")
	class PublishConditionTests {

		@Test
		@DisplayName("logs report entry regardless of the outcome when publish condition is ALWAYS")
		void conditional_logBeforeRegardlessOfOutcome() {
			ExecutionEventRecorder successRecorder = executeTestsForMethod(ReportEntriesTest.class, "beforeSuccess");

			List<Map<String, String>> successReportEntries = reportEntries(successRecorder);

			assertThat(successRecorder.getSuccessfulTestFinishedEvents()).hasSize(1);
			assertThat(successReportEntries.get(0)).satisfies(reportEntry -> {
				assertThat(reportEntry).hasSize(1);
				assertThat(reportEntry).containsExactly(entryOf("value", "'Tis some visitor', I muttered"));
			});

			ExecutionEventRecorder failureRecorder = executeTestsForMethod(ReportEntriesTest.class, "beforeFailure");

			List<Map<String, String>> failureReportEntries = reportEntries(failureRecorder);

			assertThat(failureRecorder.getFailedTestFinishedEvents()).hasSize(1);
			assertThat(failureReportEntries.get(0)).satisfies(reportEntry -> {
				assertThat(reportEntry).hasSize(1);
				assertThat(reportEntry).containsExactly(entryOf("value", "'Tapping at my chamber door' -"));
			});
		}

		@Test
		@DisplayName("logs after success, if publish condition is ON_SUCCESS")
		void conditional_logOnSuccess() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "onSuccess");

			List<Map<String, String>> reportEntries = reportEntries(recorder);

			assertThat(recorder.getSuccessfulTestFinishedEvents()).hasSize(1);
			assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
				assertThat(reportEntry).hasSize(1);
				assertThat(reportEntry).containsExactly(entryOf("value", "'Only this and nothing more.'"));
			});
		}

		@Test
		@DisplayName("does not logs after failure, if publish condition is ON_SUCCESS")
		void conditional_doNotLogOnFailure() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "notOnFailure");

			List<Map<String, String>> reportEntries = reportEntries(recorder);

			assertThat(recorder.getFailedTestFinishedEvents()).hasSize(1);
			assertThat(reportEntries).isEmpty();
		}

		@Test
		@DisplayName("logs after failure, if publish condition is ON_FAILURE")
		void conditional_logOnFailure() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "onFailure");

			List<Map<String, String>> reportEntries = reportEntries(recorder);

			assertThat(recorder.getFailedTestFinishedEvents()).hasSize(1);
			assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
				assertThat(reportEntry).hasSize(1);
				assertThat(reportEntry).containsExactly(entryOf("value", "And each separate dying ember"));
			});
		}

		@Test
		@DisplayName("does not log after success, if publish condition is ON_FAILURE")
		void conditional_doNotLogOnSuccess() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "notOnSuccess");

			List<Map<String, String>> reportEntries = reportEntries(recorder);

			assertThat(recorder.getSuccessfulTestFinishedEvents()).hasSize(1);
			assertThat(reportEntries).isEmpty();
		}

		@Test
		@DisplayName("logs entries independently on success, based on publish condition")
		void conditional_logOnSuccessIndependently() {
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "repeatedOnSuccess");

			List<Map<String, String>> reportEntries = reportEntries(recorder);

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
			ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "repeatedOnFailure");

			List<Map<String, String>> reportEntries = reportEntries(recorder);

			assertThat(recorder.getFailedTestFinishedEvents()).hasSize(1);
			assertAll("Verifying report entries " + reportEntries, //
				() -> assertThat(reportEntries).hasSize(2),
				() -> assertThat(reportEntries).extracting(Map::size).containsExactlyInAnyOrder(1, 1),
				() -> assertThat(reportEntries)
						.extracting(entry -> entry.get("value"))
						.containsExactlyInAnyOrder("For the rare and radiant maiden", "Nameless here"));
		}

		@Test
		@DisplayName("does not log entry when publish condition is NEVERMORE")
		void conditional_nevermore() {
			ExecutionEventRecorder successRecorder = executeTestsForMethod(ReportEntriesTest.class,
				"nevermoreOnSuccess");

			List<Map<String, String>> successReports = reportEntries(successRecorder);

			assertThat(successRecorder.getSuccessfulTestFinishedEvents()).hasSize(1);
			assertThat(successReports).isEmpty();

			ExecutionEventRecorder failureRecorder = executeTestsForMethod(ReportEntriesTest.class,
				"nevermoreOnFailure");

			List<Map<String, String>> failureReports = reportEntries(failureRecorder);

			assertThat(failureRecorder.getFailedTestFinishedEvents()).hasSize(1);
			assertThat(failureReports).isEmpty();
		}

	}

	private static List<Map<String, String>> reportEntries(ExecutionEventRecorder recorder) {
		return recorder
				.eventStream()
				.filter(event -> event.getType().equals(ExecutionEvent.Type.REPORTING_ENTRY_PUBLISHED))
				.map(executionEvent -> executionEvent.getPayload(org.junit.platform.engine.reporting.ReportEntry.class))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(org.junit.platform.engine.reporting.ReportEntry::getKeyValuePairs)
				.collect(toList());
	}

	private static Map.Entry<String, String> entryOf(String key, String value) {
		return new AbstractMap.SimpleEntry<>(key, value);
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
		void beforeSuccess() {
		}

		@Test
		@ReportEntry(value = "'Tapping at my chamber door' -", when = ALWAYS)
		void beforeFailure() {
			throw new AssertionError();
		}

		@Test
		@ReportEntry(value = "'Only this and nothing more.'", when = ON_SUCCESS)
		void onSuccess() {
		}

		@Test
		@ReportEntry(value = "Ah, distinctly I remember it was in the bleak December", when = ON_SUCCESS)
		void notOnFailure() {
			throw new AssertionError();
		}

		@Test
		@ReportEntry(value = "And each separate dying ember", when = ON_FAILURE)
		void onFailure() {
			throw new AssertionError();
		}

		@Test
		@ReportEntry(value = "wrought its ghost upon the floor", when = ON_FAILURE)
		void notOnSuccess() {
		}

		@Test
		@ReportEntry(value = "Tell me what thy lordly name is on the Night’s Plutonian shore!", when = NEVERMORE)
		void nevermoreOnSuccess() {
		}

		@Test
		@ReportEntry(value = "On the morrow he will leave me, as my Hopes have flown before.", when = NEVERMORE)
		void nevermoreOnFailure() {
			throw new AssertionError();
		}

		@Test
		@ReportEntry(value = "Eagerly I wished the morrow;", when = ALWAYS)
		@ReportEntry(value = "vainly I had sought to borrow", when = ON_SUCCESS)
		@ReportEntry(value = "From my books surcease of sorrow—", when = ON_FAILURE)
		@ReportEntry(value = "sorrow for the lost Lenore", when = NEVERMORE)
		void repeatedOnSuccess() {
		}

		@Test
		@ReportEntry(value = "For the rare and radiant maiden", when = ALWAYS)
		@ReportEntry(value = "whom the angels name Lenore—", when = ON_SUCCESS)
		@ReportEntry(value = "Nameless here", when = ON_FAILURE)
		@ReportEntry(value = "for evermore.", when = NEVERMORE)
		void repeatedOnFailure() {
			throw new AssertionError();
		}

	}

}
