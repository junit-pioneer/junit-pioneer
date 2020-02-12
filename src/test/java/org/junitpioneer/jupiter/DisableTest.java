package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

public class DisableTest extends AbstractPioneerTestEngineTests {
    @Test
    @DisplayName("Incorrectly configured, will throw an exception.")
    void testIncorrect() {
        ExecutionEventRecorder recorder = executeTests(DisableTestCases.class, "failedDisable(int,java.lang.String)");

        assertThat(recorder.getTestFailedCount()).isEqualTo(9);
    }

    @Test
    @DisplayName("Disables tests for a matching input.")
    void testSingle() {
        ExecutionEventRecorder recorder = executeTests(DisableTestCases.class, "disableSingle(int,java.lang.String)");

        assertThat(recorder.getTestAbortedCount()).isEqualTo(1);
        assertThat(recorder.getTestSuccessfulCount()).isEqualTo(8);
    }

    @Test
    @DisplayName("Providing a reason does not change the outcome.")
    void testSingleWithReasoning() {
        ExecutionEventRecorder recorder = executeTests(DisableTestCases.class, "disableSingleWithReason(int,java.lang.String)");

        assertThat(recorder.getTestAbortedCount()).isEqualTo(1);
        assertThat(recorder.getTestSuccessfulCount()).isEqualTo(8);
    }

    @Test
    @DisplayName("Can disable tests if a single param matches.")
    void testContains() {
        ExecutionEventRecorder recorder = executeTests(DisableTestCases.class, "disableContains(java.lang.String,java.lang.String)");

        assertThat(recorder.getTestAbortedCount()).isEqualTo(5);
        assertThat(recorder.getTestSuccessfulCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("Given multiple conditions, will disable tests for any of them.")
    void testCompound() {
        ExecutionEventRecorder recorder = executeTests(DisableTestCases.class, "disableCompound(int,java.lang.String)");

        assertThat(recorder.getTestAbortedCount()).isEqualTo(5);
        assertThat(recorder.getTestSuccessfulCount()).isEqualTo(4);
    }

    static class DisableTestCases {
        @ParameterizedTest
        @CsvSource({"1, a", "1, b", "1, c", "2, a", "2, b", "2, c", "3, a", "3, b", "3, c"})
        public void failedDisable(int number, String text) {
            Disable
                .when(number, text)
                .is(1, "a", "c");
            assertThat(Arrays.asList(number, text)).isNotEqualTo(Arrays.asList(1, "a"));
        }

        @ParameterizedTest
        @CsvSource({"1, a", "1, b", "1, c", "2, a", "2, b", "2, c", "3, a", "3, b", "3, c"})
        public void disableSingle(int number, String text) {
            Disable
                .when(number, text)
                .is(1, "a");
            assertThat(Arrays.asList(number, text)).isNotEqualTo(Arrays.asList(1, "a"));
        }

        @ParameterizedTest
        @CsvSource({"1, a", "1, b", "1, c", "2, a", "2, b", "2, c", "3, a", "3, b", "3, c"})
        public void disableSingleWithReason(int number, String text) {
            Disable
                .when(number, text)
                .is(1, "a")
                .becauseOf("single issue");
            assertThat(Arrays.asList(number, text)).isNotEqualTo(Arrays.asList(1, "a"));
        }

        @ParameterizedTest
        @CsvSource({"a, a", "a, b", "a, c", "b, a", "b, b", "b, c", "c, a", "c, b", "c, c"})
        public void disableContains(String key, String value) {
            Disable
                .when(key, value)
                .contains("a")
                .becauseOf("should not matter if key or value");
            assertThat(key).isNotEqualTo("a");
            assertThat(value).isNotEqualTo("a");
        }

        @ParameterizedTest
        @CsvSource({"1, a", "1, b", "1, c", "2, a", "2, b", "2, c", "3, a", "3, b", "3, c"})
        public void disableCompound(int number, String text) {
            Disable
                .when(number, text)
                .contains(1)
                .is(3, "a")
                .is(2, "b")
                .becauseOf("something that should be fixed");
            assertThat(number).isNotEqualTo(1);
            assertThat(Arrays.asList(number, text)).isNotEqualTo(Arrays.asList(2, "b"));
            assertThat(Arrays.asList(number, text)).isNotEqualTo(Arrays.asList(3, "a"));
        }
    }
}
