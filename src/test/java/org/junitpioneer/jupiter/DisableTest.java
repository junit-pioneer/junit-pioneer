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
        ExecutionEventRecorder recorder = executeTests(DisableTestCases.class, "disableOne(int,java.lang.String)");

        assertThat(recorder.getTestAbortedCount()).isEqualTo(1);
        assertThat(recorder.getTestSuccessfulCount()).isEqualTo(8);
    }

    static class DisableTestCases {
        @ParameterizedTest
        @CsvSource({"1, a", "1, b", "1, c", "2, a", "2, b", "2, c", "3, a", "3, b", "3, c"})
        public void disableOne(int number, String text) {
            Disable
                .when(number, text)
                .is(1, "a")
                .becauseOf("a specific issue.");
            assertThat(Arrays.asList(number, text)).isNotEqualTo(Arrays.asList(1, "a"));
        }
    }
}
