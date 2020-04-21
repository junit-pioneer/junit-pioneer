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
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethod;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;

public class ReportEntryExtensionTest {

	@Test
	void explicitKey_keyAndValueAreReported() {
		List<Map<String, String>> reportEntries = executeTestMethod(ReportEntriesTest.class, "explicitKey")
				.publishedTestReportEntries();

		assertThat(reportEntries).hasSize(1);
		Map<String, String> reportEntry = reportEntries.get(0);
		assertThat(reportEntry).hasSize(1);
		assertThat(reportEntry).containsExactly(TestUtils.entryOf("Crow2", "While I pondered weak and weary"));
	}

	@Test
	void implicitKey_keyIsNamedValue() {
		List<Map<String, String>> reportEntries = executeTestMethod(ReportEntriesTest.class, "implicitKey")
				.publishedTestReportEntries();

		assertThat(reportEntries).hasSize(1);
		assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
			assertThat(reportEntry).hasSize(1);
			assertThat(reportEntry).containsExactly(TestUtils.entryOf("value", "Once upon a midnight dreary"));
		});
	}

	@Test
	void emptyKey_fails() {
		ExecutionResults results = executeTestMethod(ReportEntriesTest.class, "emptyKey");

		assertThat(results.numberOfFailedTests()).isEqualTo(1);
		assertThat(results.firstFailuresThrowableMessage())
				.contains("Report entries can't have blank key or value",
					"Over many a quaint and curious volume of forgotten lore");
	}

	@Test
	void emptyValue_fails() {
		ExecutionResults results = executeTestMethod(ReportEntriesTest.class, "emptyValue");
		assertThat(results.numberOfFailedTests()).isEqualTo(1);
		assertThat(results.firstFailuresThrowableMessage())
				.contains("Report entries can't have blank key or value", "While I nodded, nearly napping");
	}

	@Test
	void repeatedAnnotation_logEachKeyValuePairAsIndividualEntry() {
		List<Map<String, String>> reportEntries = executeTestMethod(ReportEntriesTest.class, "repeatedAnnotation")
				.publishedTestReportEntries();

		assertAll("Verifying report entries " + reportEntries, //
			() -> assertThat(reportEntries).hasSize(3),
			() -> assertThat(reportEntries).extracting(Map::size).containsExactlyInAnyOrder(1, 1, 1),
			() -> assertThat(reportEntries)
					.extracting(entry -> entry.get("value"))
					.containsExactlyInAnyOrder("suddenly there came a tapping", "As if some one gently rapping",
						"rapping at my chamber door"));
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
