package org.junitpioneer.jupiter.displaynamegenerator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.TestDescriptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

class ReplaceCamelCaseAndUnderscoreAndNumberTests extends ReplaceCamelCaseAndUnderscoreAndNumberTestEngine {

    @Test
    void replaceCamelCaseAndUnderscoreAndNumberGenerator() {
        check(ReplaceCamelCaseAndUnderscoreAndNumberStyleTestCase.class, //
                "CONTAINER: ReplaceCamelCaseAndUnderscoreAndNumberTests$ReplaceCamelCaseAndUnderscoreAndNumberStyleTestCase", //
                "TEST: @DisplayName prevails", //
                "TEST: Should return error when maxResults is negative", //
                "TEST: Should create limit with range (String)", //
                "TEST: Should return 5 errors (int)", //
                "TEST: Should return 5errors", //
                "TEST: Should return 23 errors", //
                "TEST: Should return the value of maxResults", //
                "TEST: Should return the number of errors as numberOfErrors inferior or equal to 5 (String)", //
                "TEST: Should return the number of errors as numberOfErrors inferior or equal to 15" //
        );

    }

    private void check(Class<?> testClass, String... expectedDisplayNames) {
        var request = request().selectors(selectClass(testClass)).build();
        var descriptors = discoverTests(request).getDescendants();
        assertThat(descriptors).map(this::describe).containsExactlyInAnyOrder(expectedDisplayNames);
    }

    private String describe(TestDescriptor descriptor) {
        return descriptor.getType() + ": " + descriptor.getDisplayName();
    }

    @DisplayNameGeneration(ReplaceCamelCaseAndUnderscoreAndNumber.class)
    static class ReplaceCamelCaseAndUnderscoreAndNumberStyleTestCase {
        @Test
        void shouldReturnErrorWhen_maxResults_IsNegative() {}

        @ParameterizedTest
        @ValueSource(strings = {"", "  "})
        void shouldCreateLimitWithRange(String input) {}

        @ParameterizedTest
        @ValueSource(ints = {5, 23})
        void shouldReturn5Errors(int input) {}

        @Test
        void shouldReturn5errors() {}

        @Test
        void shouldReturn23Errors() {}

        @Test
        void shouldReturnTheValueOf_maxResults() {}

        @ParameterizedTest
        @ValueSource(strings = {"", "  "})
        void shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo5(String input) {}

        @Test
        void shouldReturnTheNumberOfErrorsAs_numberOfErrors_InferiorOrEqualTo15() {}

        @DisplayName("@DisplayName prevails")
        @Test
        void testDisplayNamePrevails() {}

    }
}
