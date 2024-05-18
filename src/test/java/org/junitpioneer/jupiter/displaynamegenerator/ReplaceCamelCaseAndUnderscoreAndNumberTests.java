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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * This class explicitly tests {@linkplain ReplaceCamelCaseAndUnderscoreAndNumber } by directly calling the method {@linkplain ReplaceCamelCaseAndUnderscoreAndNumber#generateDisplayNameForMethod(Class, Method)}  }
 * */
class ReplaceCamelCaseAndUnderscoreAndNumberTests {
    private static Stream<Arguments> provideMethodNameAndExpectedDisplayName() {
        return Stream.of(
                Arguments.of("shouldReturnErrorWhen_maxResults_IsNegative", "Should return error when maxResults is negative"),
                Arguments.of("shouldCreateLimitWithRange", "Should create limit with range (String)"),
                Arguments.of("shouldReturn5Errors", "Should return 5 errors (int)"),
                Arguments.of("shouldReturn5errors", "Should return 5errors"),
                Arguments.of("shouldReturn23Errors", "Should return 23 errors"),
                Arguments.of("shouldReturnTheValueOf_maxResults", "Should return the value of maxResults"),
                Arguments.of("shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo5", "Should return the number of errors as numberOfErrors inferior or equal to 5 (String)"),
                Arguments.of("shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo15", "Should return the number of errors as numberOfErrors inferior or equal to 15"),
                Arguments.of("methodNotAnnotatedWithTestOrParameterizedTest", null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideMethodNameAndExpectedDisplayName")
    void shouldReturnMethodDisplayNamesForCamelCaseAndUnderscoreAndNumber(final String methodName, final String expectedDisplayName) {
        List<String> methodsWithParameters = List.of("shouldCreateLimitWithRange", "shouldReturn5Errors", "shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo5");

        try {
            Method method = null;
            if (!methodsWithParameters.contains(methodName)) {
                method = ClassAnnotatedWithReplaceCamelCaseAndUnderscoreAndNumberTests.class.getDeclaredMethod(methodName);
            }
            if (List.of("shouldCreateLimitWithRange", "shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo5").contains(methodName)) {
                method = ClassAnnotatedWithReplaceCamelCaseAndUnderscoreAndNumberTests.class.getDeclaredMethod(methodName, String.class);
            }
            if ("shouldReturn5Errors".equals(methodName)) {
                method = ClassAnnotatedWithReplaceCamelCaseAndUnderscoreAndNumberTests.class.getDeclaredMethod(methodName, int.class);
            }
            assert method != null;

            if (method.isAnnotationPresent(Test.class) || method.isAnnotationPresent(ParameterizedTest.class)) {
                Method finalMethod = method;
                assertSoftly(softly -> {
                    softly.assertThat(ReplaceCamelCaseAndUnderscoreAndNumber.INSTANCE.generateDisplayNameForMethod(ClassAnnotatedWithReplaceCamelCaseAndUnderscoreAndNumberTests.class, finalMethod)).isEqualTo(expectedDisplayName);
                });
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}