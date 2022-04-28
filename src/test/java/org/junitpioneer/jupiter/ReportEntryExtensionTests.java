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

import static org.junit.jupiter.api.Assertions.fail;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ALWAYS;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_ABORTED;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_FAILURE;
import static org.junitpioneer.jupiter.ReportEntry.PublishCondition.ON_SUCCESS;
import static org.junitpioneer.testkit.PioneerTestKit.abort;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

/**
 * Edgar Allan Poe: The Raven is in the public domain.
 */
@DisplayName("ReportEntry extension")
public class ReportEntryExtensionTests {

	@Test
	@DisplayName("reports given explicit key and value")
	void explicitKey_keyAndValueAreReported() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntryTestCases.class, "explicitKey");

		assertThat(results).hasSingleReportEntry().withKeyAndValue("Crow2", "While I pondered weak and weary");
	}

	@Test
	@DisplayName("reports given explicit value with default key 'value'")
	void implicitKey_keyIsNamedValue() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntryTestCases.class, "implicitKey");

		assertThat(results).hasSingleReportEntry().withKeyAndValue("value", "Once upon a midnight dreary");
	}

	@Test
	@DisplayName("fails when given an empty key explicitly")
	void emptyKey_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntryTestCases.class, "emptyKey");

		assertThat(results)
				.hasSingleFailedTest()
				.withException()
				.hasMessageContainingAll("Report entries can't have blank key or value",
					"Over many a quaint and curious volume of forgotten lore");
	}

	@Test
	@DisplayName("fails when given an empty value")
	void emptyValue_fails() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntryTestCases.class, "emptyValue");

		assertThat(results)
				.hasSingleFailedTest()
				.withException()
				.hasMessageContainingAll("Report entries can't have blank key or value",
					"While I nodded, nearly napping");
	}

	@Test
	@DisplayName("logs each value as individual entry when annotation is repeated")
	void repeatedAnnotation_logEachKeyValuePairAsIndividualEntry() {
		ExecutionResults results = PioneerTestKit.executeTestMethod(ReportEntryTestCases.class, "repeatedAnnotation");

		assertThat(results)
				.hasNumberOfReportEntries(3)
				.withValues("suddenly there came a tapping", "As if some one gently rapping",
					"rapping at my chamber door");
	}

	@Nested
	@DisplayName("with explicitly set 'when' parameter")
	class PublishConditionTests {

		@Nested
		@DisplayName("to 'ALWAYS'")
		class LogAlwaysTests {

			@Test
			@DisplayName("logs for successful test")
			void successfulTest_logsMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "always_success");

				assertThat(results).hasSingleSucceededTest();
				assertThat(results).hasSingleReportEntry().withKeyAndValue("value", "'Tis some visitor', I muttered");
			}

			@Test
			@DisplayName("logs for failed test")
			void failingTest_logsMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "always_failure");

				assertThat(results).hasSingleFailedTest();
				assertThat(results).hasSingleReportEntry().withKeyAndValue("value", "'Tapping at my chamber door' -");
			}

			@Test
			@DisplayName("logs for aborted test")
			void abortedTest_logsMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "always_aborted");

				assertThat(results).hasSingleAbortedTest();
				assertThat(results).hasSingleReportEntry().withKeyAndValue("value", "'Only this and nothing more.'");
			}

			@Test
			@DisplayName("does not log for disabled test")
			void disabledTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "always_disabled");

				assertThat(results).hasSingleSkippedTest();
				assertThat(results).hasNoReportEntries();
			}

		}

		@Nested
		@DisplayName("to 'ON_SUCCESS'")
		class LogOnSuccessTests {

			@Test
			@DisplayName("logs for successful test")
			void successfulTest_logsMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onSuccess_success");

				assertThat(results).hasSingleSucceededTest();
				assertThat(results).hasSingleReportEntry().withKeyAndValue("value", "it was in the bleak December");
			}

			@Test
			@DisplayName("does not log for failed test")
			void failedTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onSuccess_failure");

				assertThat(results).hasSingleFailedTest();
				assertThat(results).hasNoReportEntries();
			}

			@Test
			@DisplayName("does not log for aborted test")
			void abortedTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onSuccess_aborted");

				assertThat(results).hasSingleAbortedTest();
				assertThat(results).hasNoReportEntries();
			}

			@Test
			@DisplayName("does not log for disabled test")
			void disabledTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onSuccess_disabled");

				assertThat(results).hasSingleSkippedTest();
				assertThat(results).hasNoReportEntries();
			}

		}

		@Nested
		@DisplayName("to 'ON_FAILURE'")
		class LogOnFailureTests {

			@Test
			@DisplayName("does not log for successful test")
			void successfulTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onFailure_success");

				assertThat(results).hasSingleSucceededTest();
				assertThat(results).hasNoReportEntries();
			}

			@Test
			@DisplayName("logs for failed test")
			void failedTest_logsMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onFailure_failure");

				assertThat(results).hasSingleFailedTest();
				assertThat(results).hasSingleReportEntry().withKeyAndValue("value", "Nameless here for evermore.");
			}

			@Test
			@DisplayName("does not log for aborted test")
			void abortedTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onFailure_aborted");

				assertThat(results).hasSingleAbortedTest();
				assertThat(results).hasNoReportEntries();
			}

			@Test
			@DisplayName("does not log for disabled test")
			void disabledTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onFailure_disabled");

				assertThat(results).hasSingleSkippedTest();
				assertThat(results).hasNoReportEntries();
			}

		}

		@Nested
		@DisplayName("to 'ON_ABORTED'")
		class LogOnAbortedTests {

			@Test
			@DisplayName("does not log for successful test")
			void successfulTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onAborted_success");

				assertThat(results).hasSingleSucceededTest();
				assertThat(results).hasNoReportEntries();
			}

			@Test
			@DisplayName("does not log for failed test")
			void failedTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onAborted_failure");

				assertThat(results).hasSingleFailedTest();
				assertThat(results).hasNoReportEntries();
			}

			@Test
			@DisplayName("logs for aborted test")
			void abortedTest_logsMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onAborted_aborted");

				assertThat(results)
						.hasSingleReportEntry()
						.withKeyAndValue("value", "Some late visitor entreating entrance at my chamber door;—");
			}

			@Test
			@DisplayName("does not log for disabled test")
			void disabledTest_logsNoMessage() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "onAborted_disabled");

				assertThat(results).hasSingleSkippedTest();
				assertThat(results).hasNoReportEntries();
			}

		}

		@Nested
		@DisplayName("to multiple conditions")
		class LogOnMultipleConditionsTests {

			@Test
			@DisplayName("logs entries independently on success, based on publish condition")
			void conditional_logOnSuccessIndependently() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "repeated_success");

				assertThat(results).hasSingleSucceededTest();
				assertThat(results)
						.hasNumberOfReportEntries(2)
						.withValues("Deep into that darkness peering, long I stood there wondering, fearing,",
							"Doubting, dreaming dreams no mortal ever dared to dream before;");
			}

			@Test
			@DisplayName("logs entries independently on failure, based on publish condition")
			void conditional_logOnFailureIndependently() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "repeated_failure");

				assertThat(results).hasSingleFailedTest();
				assertThat(results)
						.hasNumberOfReportEntries(2)
						.withValues("And the only word there spoken was the whispered word, “Lenore?”",
							"murmured back the word, “Lenore!”—");
			}

			@Test
			@DisplayName("logs entries independently on abortion, based on publish condition")
			void conditional_logOnAbortedIndependently() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "repeated_aborted");

				assertThat(results).hasSingleAbortedTest();
				assertThat(results)
						.hasNumberOfReportEntries(2)
						.withValues("Back into the chamber turning, all my soul within me burning,",
							"“surely that is something at my window lattice;");
			}

			@Test
			@DisplayName("does not log entries if disabled")
			void conditional_doesNotLogOnDisabled() {
				ExecutionResults results = PioneerTestKit
						.executeTestMethod(ReportEntryTestCases.class, "repeated_disabled");

				assertThat(results).hasSingleSkippedTest();
				assertThat(results).hasNoReportEntries();
			}

		}

	}

	@Nested
	@DisplayName("with parameterized tests")
	class ParameterEntriesTests {

		@Test
		@DisplayName("publishes the parameter")
		void parameterized_publishes() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ReportEntryTestCases.class, "parameterized_basic",
						String.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(2).hasNumberOfSucceededTests(2);
			assertThat(results)
					.hasNumberOfReportEntries(2)
					.withValues("Open here I flung the shutter, when, with many a flirt and flutter,",
						"In there stepped a stately Raven of the saintly days of yore;");
		}

		@Test
		@DisplayName("throws if there are unresolved (too many) parameter variables")
		void parameterized_unresolvedVars() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ReportEntryTestCases.class, "parameterized_unresolved",
						String.class);

			assertThat(results).hasNumberOfFailedTests(1);
			assertThat(results).hasNoReportEntries();
			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageStartingWith("Report entry contains unresolved variable(s)");
		}

		@Test
		@DisplayName("throw if the key has a parameter variable")
		void parameterized_keyCantBeParameterized() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ReportEntryTestCases.class, "parameterized_key_fail",
						String.class);

			assertThat(results).hasNoReportEntries();
			assertThat(results)
					.hasSingleFailedTest()
					.withExceptionInstanceOf(ExtensionConfigurationException.class)
					.hasMessageStartingWith("Report entry can not have variables in the key");
		}

		@Test
		@DisplayName("can publish multiple parameters")
		void parameterized_multiple() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ReportEntryTestCases.class, "parameterized_multiple",
						String.class, int.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(2).hasNumberOfSucceededTests(2);
			assertThat(results)
					.hasNumberOfReportEntries(2)
					.withValues("1 - 1: Perched, and sat, and nothing more.",
						"2 - 2: Then this ebony bird beguiling my sad fancy into smiling,");
		}

		@Test
		@DisplayName("can publish null arguments")
		void parameterized_with_nulls() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(ReportEntryTestCases.class, "parameterized_with_nulls",
						String.class, String.class);

			assertThat(results).hasSingleSucceededTest();
			assertThat(results)
					.hasNumberOfReportEntries(1)
					.withValues("null,By the grave and stern decorum of the countenance it wore,");
		}

	}

	static class ReportEntryTestCases {

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

		@ParameterizedTest
		@ValueSource(strings = { "Open here I flung the shutter, when, with many a flirt and flutter,",
				"In there stepped a stately Raven of the saintly days of yore;" })
		@ReportEntry("{0}")
		void parameterized_basic(String line) {
		}

		@ParameterizedTest
		@ValueSource(strings = { "Not the least obeisance made he; not a minute stopped or stayed he;" })
		@ReportEntry("{0}, {1}")
		void parameterized_unresolved(String line) {
		}

		@ParameterizedTest
		@ValueSource(strings = { "But, with mien of lord or lady, perched above my chamber door—" })
		@ReportEntry(key = "{0}", value = "Perched upon a bust of Pallas just above my chamber door—")
		void parameterized_key_fail(String line) {
		}

		@ParameterizedTest
		@CsvSource(value = { "Perched, and sat, and nothing more.; 1",
				"Then this ebony bird beguiling my sad fancy into smiling,; 2" }, delimiter = ';')
		@ReportEntry("{1} - {1}: {0}")
		void parameterized_multiple(String line, int number) {
		}

		@ParameterizedTest
		@MethodSource("withNulls")
		@ReportEntry("{1},{0}")
		void parameterized_with_nulls(String line, String value) {
		}

		private static Stream<Arguments> withNulls() {
			return Stream.of(Arguments.of("By the grave and stern decorum of the countenance it wore,", null));
		}

	}

}
