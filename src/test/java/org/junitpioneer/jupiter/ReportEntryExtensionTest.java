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
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.Events;
import org.junitpioneer.platform.testkit.engine.PioneerTestKit;

public class ReportEntryExtensionTest {

	@Test
	void explicitKey_keyAndValueAreReported() {
		Events events = PioneerTestKit
				.execute(ReportEntriesTest.class, "explicitKey")
				.tests()
				.reportingEntryPublished();

		List<Map<String, String>> reportEntries = reportEntries(events);
		assertThat(reportEntries).hasSize(1);
		Map<String, String> reportEntry = reportEntries.get(0);
		assertThat(reportEntry).hasSize(1);
		assertThat(reportEntry).containsExactly(TestUtils.entryOf("Crow2", "While I pondered weak and weary"));
	}

	private static List<Map<String, String>> reportEntries(Events events) {
		return events.stream()
				.map(executionEvent -> executionEvent.getPayload(org.junit.platform.engine.reporting.ReportEntry.class))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(org.junit.platform.engine.reporting.ReportEntry::getKeyValuePairs)
				.collect(toList());
	}

	@Test
	void implicitKey_keyIsNamedValue() {
		Events events = PioneerTestKit
				.execute(ReportEntriesTest.class, "implicitKey")
				.tests()
				.reportingEntryPublished();

		List<Map<String, String>> reportEntries = reportEntries(events);
		assertThat(reportEntries).hasSize(1);
		assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
			assertThat(reportEntry).hasSize(1);
			assertThat(reportEntry).containsExactly(TestUtils.entryOf("value", "Once upon a midnight dreary"));
		});
	}

	@Test
	void emptyKey_fails() {
		Events events = PioneerTestKit
				.execute(ReportEntriesTest.class, "emptyKey")
				.tests();

		events.failed().assertThatEvents().hasSize(1);
		assertThat(getFirstFailuresThrowable(events).getMessage())
				.contains("Report entries can't have blank key or value",
					"Over many a quaint and curious volume of forgotten lore");
	}

	protected Throwable getFirstFailuresThrowable(Events events) {
		return events
				.failed().stream()
				.findFirst()
				.orElseThrow(AssertionError::new)
				// TODO do we break out of the testkit API here? If we are, maybe we shouldn't
				.getPayload(TestExecutionResult.class)
				.flatMap(TestExecutionResult::getThrowable)
				.orElseThrow(AssertionError::new);
	}

	@Test
	void emptyValue_fails() {
		Events events = PioneerTestKit
				.execute(ReportEntriesTest.class, "emptyValue")
				.tests();

		events.failed().assertThatEvents().hasSize(1);
		assertThat(getFirstFailuresThrowable(events).getMessage())
				.contains("Report entries can't have blank key or value", "While I nodded, nearly napping");
	}

	@Test
	void repeatedAnnotation_logEachKeyValuePairAsIndividualEntry() {
		Events events = PioneerTestKit
				.execute(ReportEntriesTest.class, "repeatedAnnotation")
				.tests();

		List<Map<String, String>> reportEntries = reportEntries(events);

		assertAll("Verifying report entries " + reportEntries, //
			() -> assertThat(reportEntries).hasSize(3),
			() -> assertThat(reportEntries).extracting(entry -> entry.size()).containsExactlyInAnyOrder(1, 1, 1),
			() -> assertThat(reportEntries)
					.extracting(entry -> entry.get("value"))
					.containsExactlyInAnyOrder("suddenly there came a tapping", "As if some one gently rapping",
						"rapping at my chamber door"));
	}

	private static Map.Entry<String, String> entryOf(String key, String value) {
		return new AbstractMap.SimpleEntry<>(key, value);
	}

	static class ReportEntriesTest {

		private static AtomicInteger executionCount = new AtomicInteger(0);

		@Test
		@ReportEntry(key = "Crow2", value = "While I pondered weak and weary")
		void explicitKey() {
			System.out.println("HI THERE! " + executionCount.getAndIncrement());
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
