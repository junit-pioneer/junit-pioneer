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
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_ABORTED;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_FAILURE;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_SUCCESS;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;
import org.opentest4j.TestAbortedException;

/**
 * Edgar Allan Poe: The Raven is in the public domain.
 */
@DisplayName("ReportEntry extension")
public class ReportEntryExtensionTests {

	@Test
	@DisplayName("reports given explicit key and value")
	void explicitKey_keyAndValueAreReported() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntriesTest.class, "explicitKey");

		List<Map<String, String>> reportEntries = results.reportEntries();
		assertThat(reportEntries).hasSize(1);
		Map<String, String> reportEntry = reportEntries.get(0);
		assertThat(reportEntry).hasSize(1);
		assertThat(reportEntry).containsExactly(TestUtils.entryOf("Crow2", "While I pondered weak and weary"));
	}

	@Test
	@DisplayName("reports given explicit value with default key 'value'")
	void implicitKey_keyIsNamedValue() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntriesTest.class, "implicitKey");

		List<Map<String, String>> reportEntries = results.reportEntries();
		assertThat(reportEntries).hasSize(1);
		assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
			assertThat(reportEntry).hasSize(1);
			assertThat(reportEntry).containsExactly(TestUtils.entryOf("value", "Once upon a midnight dreary"));
		});
	}

	@Test
	@DisplayName("fails when given an empty key explicitly")
	void emptyKey_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntriesTest.class, "emptyKey");

		assertThat(results.numberOfFailedTests()).isEqualTo(1);
		assertThat(results.firstFailuresThrowableMessage())
				.contains("Report entries can't have blank key or value",
					"Over many a quaint and curious volume of forgotten lore");
	}

	@Test
	@DisplayName("fails when given an empty value")
	void emptyValue_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntriesTest.class, "emptyValue");

		assertThat(results.numberOfFailedTests()).isEqualTo(1);
		assertThat(results.firstFailuresThrowableMessage())
				.contains("Report entries can't have blank key or value", "While I nodded, nearly napping");
	}

	@Test
	@DisplayName("logs each value as individual entry when annotation is repeated")
	void repeatedAnnotation_logEachKeyValuePairAsIndividualEntry() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntriesTest.class, "repeatedAnnotation");

		List<Map<String, String>> reportEntries = results.reportEntries();

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

		@Nested
		@DisplayName("to 'ALWAYS'")
		class LogAlways {

			@Test
			@DisplayName("logs for successful test")
			void successfulTest_logsMessage() {
				ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntriesTest.class, "always_success");

				List<Map<String, String>> successReportEntries = results.reportEntries();

				assertThat(results.numberOfSucceededTests()).isEqualTo(1);
				assertThat(successReportEntries.get(0)).satisfies(reportEntry -> {
					assertThat(reportEntry).hasSize(1);
					assertThat(reportEntry)
							.containsExactly(TestUtils.entryOf("value", "'Tis some visitor', I muttered"));
				});
			}

			@Test
			@DisplayName("logs for failed test")
			void failingTest_logsMessage() {
				ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntriesTest.class, "always_failure");

				List<Map<String, String>> failureReportEntries = results.reportEntries();

				assertThat(results.numberOfFailedTests()).isEqualTo(1);
				assertThat(failureReportEntries.get(0)).satisfies(reportEntry -> {
					assertThat(reportEntry).hasSize(1);
					assertThat(reportEntry)
							.containsExactly(TestUtils.entryOf("value", "'Tapping at my chamber door' -"));
				});
			}

			@Test
			@DisplayName("logs for aborted test")
			void abortedTest_logsMessage() {
				ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntriesTest.class, "always_aborted");

				List<Map<String, String>> failureReportEntries = results.reportEntries();

				assertThat(results.numberOfAbortedTests()).isEqualTo(1);
				assertThat(failureReportEntries.get(0)).satisfies(reportEntry -> {
					assertThat(reportEntry).hasSize(1);
					assertThat(reportEntry)
							.containsExactly(TestUtils.entryOf("value", "'Only this and nothing more.'"));
				});
			}

			@Test
			@DisplayName("does not log for disabled test")
			void disabledTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntriesTest.class, "always_disabled");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfStartedTests()).isEqualTo(0);
				assertThat(reportEntries).isEmpty();
			}

		}

		@Nested
		@DisplayName("to 'ON_SUCCESS'")
		class LogOnSuccess {

			@Test
			@DisplayName("logs for successful test")
			void successfulTest_logsMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onSuccess_success");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfSucceededTests()).isEqualTo(1);
				assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
					assertThat(reportEntry).hasSize(1);
					assertThat(reportEntry).containsExactly(TestUtils.entryOf("value", "it was in the bleak December"));
				});
			}

			@Test
			@DisplayName("does not log for failed test")
			void failedTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onSuccess_failure");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfFailedTests()).isEqualTo(1);
				assertThat(reportEntries).isEmpty();
			}

			@Test
			@DisplayName("does not log for aborted test")
			void abortedTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onSuccess_aborted");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfAbortedTests()).isEqualTo(1);
				assertThat(reportEntries).isEmpty();
			}

			@Test
			@DisplayName("does not log for disabled test")
			void disabledTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onSuccess_disabled");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfStartedTests()).isEqualTo(0);
				assertThat(reportEntries).isEmpty();
			}

		}

		@Nested
		@DisplayName("to 'ON_FAILURE'")
		class LogOnFailure {

			@Test
			@DisplayName("does not log for successful test")
			void successfulTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onFailure_success");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfSucceededTests()).isEqualTo(1);
				assertThat(reportEntries).isEmpty();
			}

			@Test
			@DisplayName("logs for failed test")
			void failedTest_logsMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onFailure_failure");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfFailedTests()).isEqualTo(1);
				assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
					assertThat(reportEntry).hasSize(1);
					assertThat(reportEntry).containsExactly(TestUtils.entryOf("value", "Nameless here for evermore."));
				});
			}

			@Test
			@DisplayName("does not log for aborted test")
			void abortedTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onFailure_aborted");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfAbortedTests()).isEqualTo(1);
				assertThat(reportEntries).isEmpty();
			}

			@Test
			@DisplayName("does not log for disabled test")
			void disabledTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onFailure_disabled");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfStartedTests()).isEqualTo(0);
				assertThat(reportEntries).isEmpty();
			}

		}

		@Nested
		@DisplayName("to 'ON_ABORTED'")
		class LogOnAborted {

			@Test
			@DisplayName("does not log for successful test")
			void successfulTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onAborted_success");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfSucceededTests()).isEqualTo(1);
				assertThat(reportEntries).isEmpty();
			}

			@Test
			@DisplayName("does not log for failed test")
			void failedTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onAborted_failure");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfFailedTests()).isEqualTo(1);
				assertThat(reportEntries).isEmpty();
			}

			@Test
			@DisplayName("logs for aborted test")
			void abortedTest_logsMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onAborted_aborted");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfAbortedTests()).isEqualTo(1);
				assertThat(reportEntries.get(0)).satisfies(reportEntry -> {
					assertThat(reportEntry).hasSize(1);
					assertThat(reportEntry)
							.containsExactly(TestUtils
									.entryOf("value", "Some late visitor entreating entrance at my chamber door;—"));
				});
			}

			@Test
			@DisplayName("does not log for disabled test")
			void disabledTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "onAborted_disabled");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfStartedTests()).isEqualTo(0);
				assertThat(reportEntries).isEmpty();
			}

		}

		@Nested
		@DisplayName("to multiple conditions")
		class LogOnMultipleConditions {

			@Test
			@DisplayName("logs entries independently on success, based on publish condition")
			void conditional_logOnSuccessIndependently() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "repeated_success");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfSucceededTests()).isEqualTo(1);
				assertAll("Verifying report entries " + reportEntries, //
					() -> assertThat(reportEntries).hasSize(2),
					() -> assertThat(reportEntries).extracting(Map::size).containsExactlyInAnyOrder(1, 1),
					() -> assertThat(reportEntries)
							.extracting(entry -> entry.get("value"))
							.containsExactlyInAnyOrder(
								"Deep into that darkness peering, long I stood there wondering, fearing,",
								"Doubting, dreaming dreams no mortal ever dared to dream before;"));
			}

			@Test
			@DisplayName("logs entries independently on failure, based on publish condition")
			void conditional_logOnFailureIndependently() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "repeated_failure");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfFailedTests()).isEqualTo(1);
				assertAll("Verifying report entries " + reportEntries, //
					() -> assertThat(reportEntries).hasSize(2),
					() -> assertThat(reportEntries).extracting(Map::size).containsExactlyInAnyOrder(1, 1),
					() -> assertThat(reportEntries)
							.extracting(entry -> entry.get("value"))
							.containsExactlyInAnyOrder(
								"And the only word there spoken was the whispered word, “Lenore?”",
								"murmured back the word, “Lenore!”—"));
			}

			@Test
			@DisplayName("logs entries independently on abortion, based on publish condition")
			void conditional_logOnAbortedIndependently() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "repeated_aborted");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfAbortedTests()).isEqualTo(1);
				assertAll("Verifying report entries " + reportEntries, //
					() -> assertThat(reportEntries).hasSize(2),
					() -> assertThat(reportEntries).extracting(Map::size).containsExactlyInAnyOrder(1, 1),
					() -> assertThat(reportEntries)
							.extracting(entry -> entry.get("value"))
							.containsExactlyInAnyOrder("Back into the chamber turning, all my soul within me burning,",
								"“surely that is something at my window lattice;"));
			}

			@Test
			@DisplayName("does not log entries if disabled")
			void conditional_doesNotLogOnDisabled() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntriesTest.class, "repeated_disabled");

				List<Map<String, String>> reportEntries = results.reportEntries();

				assertThat(results.numberOfStartedTests()).isEqualTo(0);
				assertThat(reportEntries).isEmpty();
			}

		}

	}

	static class ReportEntriesTest {

		@Test
		@ReportEntry("Once upon a midnight dreary")
		void implicitKey() {
		}

		@Test
		@ReportEntry(key = "Crow2", value = "While I pondered weak and weary")
		void explicitKey() {
		}

		@Test
		@ReportEntry(key = "", value = "Over many a quaint and curious volume of forgotten lore-")
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
		@ReportEntry(value = "'Only this and nothing more.'", when = ALWAYS)
		void always_aborted() {
			abort();
		}

		@Test
		@Disabled("to show that report entries are disabled")
		@ReportEntry(value = "Ah, distinctly I remember", when = ALWAYS)
		void always_disabled() {
		}

		@Test
		@ReportEntry(value = "it was in the bleak December", when = ON_SUCCESS)
		void onSuccess_success() {
		}

		@Test
		@ReportEntry(value = "And each separate dying ember wrought its ghost upon the floor.", when = ON_SUCCESS)
		void onSuccess_failure() {
			fail();
		}

		@Test
		@ReportEntry(value = "Eagerly I wished the morrow;—vainly I had sought to borrow", when = ON_SUCCESS)
		void onSuccess_aborted() {
			abort();
		}

		@Test
		@Disabled("to show that report entries are disabled")
		@ReportEntry(value = "From my books surcease of sorrow—sorrow for the lost Lenore—", when = ON_SUCCESS)
		void onSuccess_disabled() {
		}

		@Test
		@ReportEntry(value = "For the rare and radiant maiden whom the angels name Lenore—", when = ON_FAILURE)
		void onFailure_success() {
		}

		@Test
		@ReportEntry(value = "Nameless here for evermore.", when = ON_FAILURE)
		void onFailure_failure() {
			fail();
		}

		@Test
		@ReportEntry(value = "And the silken, sad, uncertain rustling of each purple curtain", when = ON_FAILURE)
		void onFailure_aborted() {
			abort();
		}

		@Test
		@Disabled("to show that report entries are disabled")
		@ReportEntry(value = "Thrilled me—filled me with fantastic terrors never felt before;", when = ON_FAILURE)
		void onFailure_disabled() {
		}

		@Test
		@ReportEntry(value = "So that now, to still the beating of my heart, I stood repeating", when = ON_ABORTED)
		void onAborted_success() {
		}

		@Test
		@ReportEntry(value = "Tis some visitor entreating entrance at my chamber door—", when = ON_ABORTED)
		void onAborted_failure() {
			fail();
		}

		@Test
		@ReportEntry(value = "Some late visitor entreating entrance at my chamber door;—", when = ON_ABORTED)
		void onAborted_aborted() {
			abort();
		}

		@Test
		@Disabled("to show that report entries are disabled")
		@ReportEntry(value = "This it is and nothing more.", when = ON_ABORTED)
		void onAborted_disabled() {
		}

		@Test
		@ReportEntry(value = "Deep into that darkness peering, long I stood there wondering, fearing,", when = ALWAYS)
		@ReportEntry(value = "Doubting, dreaming dreams no mortal ever dared to dream before;", when = ON_SUCCESS)
		@ReportEntry(value = "But the silence was unbroken,", when = ON_FAILURE)
		@ReportEntry(value = "and the stillness gave no token,", when = ON_ABORTED)
		void repeated_success() {
		}

		@Test
		@ReportEntry(value = "And the only word there spoken was the whispered word, “Lenore?”", when = ALWAYS)
		@ReportEntry(value = "This I whispered, and an echo", when = ON_SUCCESS)
		@ReportEntry(value = "murmured back the word, “Lenore!”—", when = ON_FAILURE)
		@ReportEntry(value = "Merely this and nothing more.", when = ON_ABORTED)
		void repeated_failure() {
			fail();
		}

		@Test
		@ReportEntry(value = "Back into the chamber turning, all my soul within me burning,", when = ALWAYS)
		@ReportEntry(value = "Soon again I heard a tapping somewhat louder than before.", when = ON_SUCCESS)
		@ReportEntry(value = "“Surely,” said I,", when = ON_FAILURE)
		@ReportEntry(value = "“surely that is something at my window lattice;", when = ON_ABORTED)
		void repeated_aborted() {
			abort();
		}

		@Test
		@Disabled("to show that report entries are disabled")
		@ReportEntry(value = "Let me see, then, what thereat is, and this mystery explore—", when = ALWAYS)
		@ReportEntry(value = "Let my heart be still a moment", when = ON_SUCCESS)
		@ReportEntry(value = "and this mystery explore;—", when = ON_FAILURE)
		@ReportEntry(value = "’Tis the wind and nothing more!”", when = ON_ABORTED)
		void repeated_disabled() {
		}

	}

	private static void abort() {
		throw new TestAbortedException();
	}

}
