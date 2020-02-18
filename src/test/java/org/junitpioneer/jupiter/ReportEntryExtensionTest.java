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

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;

public class ReportEntryExtensionTest extends AbstractJupiterTestEngineTests {

	@Test
	void test() {
		final ExecutionEventRecorder recorder = executeTestsForClass(ReportEntriesTest.class);
		assertThat(recorder.getReportingEntryPublishedCount()).isEqualTo(7);
		assertThat(values(recorder))
				.contains("Once upon a midnight dreary", "While I pondered weak and weary",
					"Over many a quaint and curious volume of forgotten lore", "While I nodded, nearly napping",
					"suddenly there came a tapping", "As if some one gently rapping", "rapping at my chamber door");
	}

	private Stream<String> values(ExecutionEventRecorder recorder) {
		return recorder.executionEvents
				.stream()
				.filter(event -> event.getType().equals(ExecutionEvent.Type.REPORTING_ENTRY_PUBLISHED))
				.map(executionEvent -> executionEvent.getPayload(org.junit.platform.engine.reporting.ReportEntry.class))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(org.junit.platform.engine.reporting.ReportEntry::getKeyValuePairs)
				.flatMap(map -> map.values().stream());
	}

	static class ReportEntriesTest {

		private static final String VERSE_2 = "While I pondered weak and weary";

		@Test
		@ReportEntry("Once upon a midnight dreary")
		@ReportEntry(key = "Crow2", value = VERSE_2)
		void test1() {
		}

		@Test
		@ReportEntry("Over many a quaint and curious volume of forgotten lore")
		void test2() {
		}

		@Test
		@ReportEntry("While I nodded, nearly napping")
		@ReportEntry("suddenly there came a tapping")
		@ReportEntry("As if some one gently rapping")
		@ReportEntry("rapping at my chamber door")
		void test3() {
		}

	}

}
