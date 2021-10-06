package org.junitpioneer.jupiter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;
import org.junitpioneer.testkit.assertion.PioneerAssert;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests for the DisabledUntil extension")
class DisabledUntilExtensionTest {

    @Test
    @DisplayName("Should enable test without annotation")
    void shouldEnableTestWithoutAnnotation() {
        final ExecutionResults results = PioneerTestKit
            .executeTestMethod(DisabledUntilExtensionTest.DisabledUntilDummyTestClass.class, "testNoAnnotation");
        PioneerAssert.assertThat(results).hasNumberOfStartedTests(1);
        PioneerAssert.assertThat(results).hasNumberOfSucceededTests(1);
        PioneerAssert.assertThat(results).hasNumberOfSkippedTests(0);
        PioneerAssert.assertThat(results).hasNumberOfReportEntries(0);
    }

    @Test
    @DisplayName("Should enable test with unparseable untilDate string")
    void shouldEnableTestWithUnparseableUntilDateString() {
        final ExecutionResults results = PioneerTestKit
            .executeTestMethod(DisabledUntilExtensionTest.DisabledUntilDummyTestClass.class, "testUnparseableUntilDateString");
        PioneerAssert.assertThat(results).hasNumberOfStartedTests(1);
        PioneerAssert.assertThat(results).hasNumberOfSucceededTests(1);
        PioneerAssert.assertThat(results).hasNumberOfSkippedTests(0);
        PioneerAssert.assertThat(results).hasNumberOfReportEntries(0);
    }

    @Test
    @DisplayName("Should enable test with untilDate in the past")
    void shouldEnableTestWithUntilDateInThePast() {
        final ExecutionResults results = PioneerTestKit
            .executeTestMethod(DisabledUntilExtensionTest.DisabledUntilDummyTestClass.class, "testIsAnnotatedWithDateInThePast");
        PioneerAssert.assertThat(results).hasNumberOfStartedTests(1);
        PioneerAssert.assertThat(results).hasNumberOfSucceededTests(1);
        PioneerAssert.assertThat(results).hasNumberOfSkippedTests(0);
        PioneerAssert.assertThat(results).hasNumberOfReportEntries(1);
    }

    @Test
    @DisplayName("Should disable test with untilDate in the future")
    void shouldDisableTestWithUntilDateInTheFuture() {
        final ExecutionResults results = PioneerTestKit
            .executeTestMethod(DisabledUntilExtensionTest.DisabledUntilDummyTestClass.class, "testIsAnnotatedWithDateInTheFuture");
        PioneerAssert.assertThat(results).hasNumberOfStartedTests(0);
        PioneerAssert.assertThat(results).hasNumberOfSucceededTests(0);
        PioneerAssert.assertThat(results).hasNumberOfSkippedTests(1);
        PioneerAssert.assertThat(results).hasNumberOfReportEntries(0);
    }

    @Test
    @DisplayName("Should disable nested test with untilDate in the future when meta annotated by higher level container")
    void shouldDisableNestedTestWithUntilDateInTheFutureWhenMetaAnnotated() {
        final ExecutionResults results = PioneerTestKit
            .executeTestMethod(DisabledUntilExtensionTest.DisabledUntilDummyTestClass.NestedDummyTestClass.class, "shouldRetrieveFromClass");
        PioneerAssert.assertThat(results).hasNumberOfSkippedContainers(1); // NestedDummyTestClass is skipped as container
        PioneerAssert.assertThat(results).hasNumberOfStartedTests(0);
        PioneerAssert.assertThat(results).hasNumberOfSucceededTests(0);
        PioneerAssert.assertThat(results).hasNumberOfSkippedTests(0);
        PioneerAssert.assertThat(results).hasNumberOfReportEntries(0);
    }

    @Test
    @DisplayName("Should return true if localDate is today")
    void shouldReturnTrueIfLocalDateIsToday() {
        assertThat(DisabledUntilExtension.isTodayOrInThePast(LocalDate.now())).isTrue();
    }

    @Test
    @DisplayName("Should return true if localDate is in the past")
    void shouldReturnTrueIfLocalDateIsInThePast() {
        assertThat(DisabledUntilExtension.isTodayOrInThePast(LocalDate.now().minusDays(1))).isTrue();
    }

    @Test
    @DisplayName("Should return false if localDate is in the future")
    void shouldReturnFalseIfLocalDateIsInTheFuture() {
        assertThat(DisabledUntilExtension.isTodayOrInThePast(LocalDate.now().plusDays(1))).isFalse();
    }

    static class DisabledUntilDummyTestClass {

        @Test
        void testNoAnnotation() {

        }

        @Test
        @DisabledUntil(reason = "Boom!", untilDate = "xxxx-yy-zz")
        void testUnparseableUntilDateString() {

        }

        @Test
        @DisabledUntil(reason = "Zoink!", untilDate = "1993-01-01")
        void testIsAnnotatedWithDateInThePast() {

        }

        @Test
        @DisabledUntil(reason = "Ka-pow!", untilDate = "2199-01-01")
        void testIsAnnotatedWithDateInTheFuture() {

        }

        @Nested
        @DisabledUntil(reason = "Yowza!", untilDate = "2199-01-01")
        class NestedDummyTestClass {

            @Test
            void shouldRetrieveFromClass() {

            }

        }

    }
}