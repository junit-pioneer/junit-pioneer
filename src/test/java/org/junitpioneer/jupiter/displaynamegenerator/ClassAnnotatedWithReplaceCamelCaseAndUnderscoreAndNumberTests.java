/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.displaynamegenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * This class implicitly tests {@linkplain ReplaceCamelCaseAndUnderscoreAndNumber } by checking if the generated display name matches the expected one.
 * */
@DisplayNameGeneration(ReplaceCamelCaseAndUnderscoreAndNumber.class)
class ClassAnnotatedWithReplaceCamelCaseAndUnderscoreAndNumberTests {

    private TestInfo testInfo;

    @BeforeEach
    void init(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    @Test
    void shouldReturnErrorWhen_maxResults_IsNegative() {
        assertSoftly(softly -> {
            softly.assertThat(testInfo.getDisplayName()).isEqualTo("Should return error when maxResults is negative");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void shouldCreateLimitWithRange(String input) {
        methodNotAnnotatedWithTestOrParameterizedTest();
        assertSoftly(softly -> {
            softly.assertThat(testInfo.getDisplayName()).isEqualTo("Should create limit with range (String)");
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 23})
    void shouldReturn5Errors(int input) {
        assertSoftly(softly -> {
            softly.assertThat(testInfo.getDisplayName()).isEqualTo("Should return 5 errors (int)");
        });
    }

    @Test
    void shouldReturn5errors() {

        methodNotAnnotatedWithTestOrParameterizedTest();
        assertSoftly(softly -> {
            softly.assertThat(testInfo.getDisplayName()).isEqualTo("Should return 5errors");
        });
    }

    @Test
    void shouldReturn23Errors() {
        methodNotAnnotatedWithTestOrParameterizedTest();
        assertSoftly(softly -> {
            softly.assertThat(testInfo.getDisplayName()).isEqualTo("Should return 23 errors");
        });
    }

    @Test
    void shouldReturnTheValueOf_maxResults() {
        methodNotAnnotatedWithTestOrParameterizedTest();
        assertSoftly(softly -> {
            softly.assertThat(testInfo.getDisplayName()).isEqualTo("Should return the value of maxResults");
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  "})
    void shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo5(String input) {
        assertSoftly(softly -> {
            softly.assertThat(testInfo.getDisplayName()).isEqualTo("Should return the number of errors as numberOfErrors inferior or equal to 5 (String)");
        });
    }

    @Test
    void shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo15() {
        assertSoftly(softly -> {
            softly.assertThat(testInfo.getDisplayName()).isEqualTo("Should return the number of errors as numberOfErrors inferior or equal to 15");
        });
    }

    private void methodNotAnnotatedWithTestOrParameterizedTest() {}
}