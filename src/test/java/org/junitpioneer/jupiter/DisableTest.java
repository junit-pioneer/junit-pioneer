package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

public class DisableTest extends AbstractPioneerTestEngineTests {

    @Test
    void executesEightTimes_abortsOnce() {
        ExecutionEventRecorder recorder = executeTests(DisableTestCases.class, "disableSingle(int,java.lang.String)");

        assertThat(recorder.getTestAbortedCount()).isEqualTo(1);
        assertThat(recorder.getTestSuccessfulCount()).isEqualTo(8);
    }

    @Test
    void executesSixTimes_abortsThreeTimes() {
        ExecutionEventRecorder recorder = executeTests(DisableTestCases.class, "disableContains(int,java.lang.String)");

        assertThat(recorder.getTestAbortedCount()).isEqualTo(3);
        assertThat(recorder.getTestSuccessfulCount()).isEqualTo(6);
    }

    @Test
    void executesFourTimes_abortsFiveTimes() {
        ExecutionEventRecorder recorder = executeTests(DisableTestCases.class, "disableCompound(int,java.lang.String)");

        assertThat(recorder.getTestAbortedCount()).isEqualTo(5);
        assertThat(recorder.getTestSuccessfulCount()).isEqualTo(4);
    }

    static class DisableTestCases {
        @ParameterizedTest
        @CsvSource({"1, a", "1, b", "1, c", "2, a", "2, b", "2, c", "3, a", "3, b", "3, c"})
        public void disableSingle(int number, String text) {
            Disable
                .when(number, text)
                .is(1, "a")
                .becauseOf("single issue");
            assertThat(Arrays.asList(number, text)).isNotEqualTo(Arrays.asList(1, "a"));
        }

        @ParameterizedTest
        @CsvSource({"1, a", "1, b", "1, c", "2, a", "2, b", "2, c", "3, a", "3, b", "3, c"})
        public void disableContains(int number, String text) {
            Disable
                .when(number, text)
                .contains(1)
                .becauseOf("1 is the loneliest number.");
            assertThat(number).isNotEqualTo(1);
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
