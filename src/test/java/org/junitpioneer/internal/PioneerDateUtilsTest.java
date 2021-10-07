package org.junitpioneer.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pioneer date utilities")
class PioneerDateUtilsTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Test
    @DisplayName("Should return true if localDate is today")
    void shouldReturnTrueIfLocalDateIsToday() {
        assertThat(PioneerDateUtils.isTodayOrInThePast(TODAY)).isTrue();
    }

    @Test
    @DisplayName("Should return true if localDate is in the past")
    void shouldReturnTrueIfLocalDateIsInThePast() {
        assertThat(PioneerDateUtils.isTodayOrInThePast(TODAY.minusDays(1))).isTrue();
    }

    @Test
    @DisplayName("Should return false if localDate is in the future")
    void shouldReturnFalseIfLocalDateIsInTheFuture() {
        assertThat(PioneerDateUtils.isTodayOrInThePast(TODAY.plusDays(1))).isFalse();
    }

    @Test
    @DisplayName("Should return Optional containing LocalDate for valid ISO 8601 date string")
    void shouldReturnOptionalContainingLocalDateForValidIso8601DateString() {
        assertThat(PioneerDateUtils.parseIso8601DateString("1985-10-26")).contains(LocalDate.of(1985, 10, 26));
    }

    @Test
    @DisplayName("Should return empty Optional for invalid ISO 8601 date string")
    void shouldReturnEmptyOptionalForInvalidIso8601DateString() {
        assertThat(PioneerDateUtils.parseIso8601DateString("xxxx-yy-zz")).isEmpty();
    }
}