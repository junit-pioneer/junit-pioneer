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

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

public class ReportEntryExtensionTest extends AbstractJupiterTestEngineTests {

	@Test
	void explicitKey_keyAndValueAreReported() {
		ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "explicitKey");

		List<Map<String, String>> reportEntries = reportEntries(recorder);
		assertThat(reportEntries).hasSize(1);
		Map<String, String> reportEntry = reportEntries.get(0);
		assertThat(reportEntry).hasSize(1);
		assertThat(reportEntry).containsExactly(entryOf("Crow2", "While I pondered weak and weary"));
	}

	@Test
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
	void emptyKey_fails() {
		ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "emptyKey");

		assertThat(recorder.getFailedTestFinishedEvents()).hasSize(1);
		assertThat(getFirstFailuresThrowable(recorder).getMessage())
				.contains("Report entries can't have blank key or value",
					"Over many a quaint and curious volume of forgotten lore");
	}

	@Test
	void emptyValue_fails() {
		ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "emptyValue");

		assertThat(recorder.getFailedTestFinishedEvents()).hasSize(1);
		assertThat(getFirstFailuresThrowable(recorder).getMessage())
				.contains("Report entries can't have blank key or value", "While I nodded, nearly napping");
	}

	@Test
	void repeatedAnnotation_logEachKeyValuePairAsIndividualEntry() {
		ExecutionEventRecorder recorder = executeTestsForMethod(ReportEntriesTest.class, "repeatedAnnotation");

		List<Map<String, String>> reportEntries = reportEntries(recorder);

		assertAll("Verifying report entries " + reportEntries, //
			() -> assertThat(reportEntries).hasSize(3),
			() -> assertThat(reportEntries).extracting(entry -> entry.size()).containsExactlyInAnyOrder(1, 1, 1),
			() -> assertThat(reportEntries)
					.extracting(entry -> entry.get("value"))
					.containsExactlyInAnyOrder("suddenly there came a tapping", "As if some one gently rapping",
						"rapping at my chamber door"));
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

	}

}
