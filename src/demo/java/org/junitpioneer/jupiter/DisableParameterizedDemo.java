package org.junitpioneer.jupiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.params.DisableIfAllArguments;
import org.junitpioneer.jupiter.params.DisableIfAnyArgument;
import org.junitpioneer.jupiter.params.DisableIfArgument;
import org.junitpioneer.jupiter.params.DisableIfDisplayName;

import static org.junit.jupiter.api.Assertions.fail;

public class DisableParameterizedDemo {

    // tag::disable_parameterized_name_contains[]
    // disable invocations whose display name contains "disable"
    @DisableIfDisplayName(contains = "disable")
    @ParameterizedTest(name = "See if enabled with {0}")
    @ValueSource(
            // Disabled: 1,2,3,4,5
            // Not disabled: 6
            strings = {
                    "disable who", // 1
                    "you, disable you", // 2
                    "why am I disabled", // 3
                    "what has been disabled must stay disabled", // 4
                    "fine disable me all you want", // 5
                    "not those one, though!" // 6
            }
    )
    void testExecutionDisabled(String reason) {
        if (reason.contains("disable"))
            fail("Test should've been disabled " + reason);
    }
    // end::disable_parameterized_name_contains[]

    // tag::disable_parameterized_name_contains_one_or_two[]
    @DisableIfDisplayName(contains = {"1", "2"})
    @ParameterizedTest(name = "See if enabled with {0}")
    @ValueSource(ints = { 1, 2, 3, 4, 5 })
    void testDisplayNameString(int num) {
        if (num == 1 || num == 2)
            fail("Test should've been disabled for " + num);
    }
    // end::disable_parameterized_name_contains_one_or_two[]

    // tag::disable_parameterized_regex[]
    // disable invocations whose display name
    // contains "disable " or "disabled "
    @DisableIfDisplayName(matches = ".*disabled?\\s.*")
    @ParameterizedTest(name = "See if enabled with {0}")
    @ValueSource(
            // Disabled: 1,2,4,5
            // Not disabled: 3,6
            strings = {
                    "disable who", // 1
                    "you, disable you", // 2
                    "why am I disabled", // 3
                    "what has been disabled must stay disabled", // 4
                    "fine disable me all you want", // 5
                    "not those one, though!" // 6
            }
    )
    void single(String reason) {
        // ...
    }
    // end::disable_parameterized_regex[]

    // tag::disable_parameterized_contains_in_all_tokens[]
    @DisableIfAllArguments(contains = "the")
    @ParameterizedTest
    @CsvSource(value = {
            "If the swift moment I entreat:;Tarry a while! You are so fair!",
            "Then forge the shackles to my feet,;Then I will gladly perish there!",
            "Then let them toll the passing-bell,;Then of your servitude be free,",
            "The clock may stop, its hands fall still,;And time be over then for me!"
    }, delimiter = ';')
    void disableAllContains(String line, String line2) {
        // ...
    }
    // end::disable_parameterized_contains_in_all_tokens[]

    // tag::disable_parameterized_contains_in_any_token[]
    @DisableIfAnyArgument(contains = "Then")
    @ParameterizedTest
    @CsvSource(value = {
            "If the swift moment I entreat:;Tarry a while! You are so fair!",
            "Then forge the shackles to my feet,;Then I will gladly perish there!",
            "Then let them toll the passing-bell,;Then of your servitude be free,",
            "The clock may stop, its hands fall still,;And time be over then for me!"
    }, delimiter = ';')
    void disableAnyContains(String line, String line2) {
        // ...
    }
    // end::disable_parameterized_contains_in_any_token[]

    // tag::disable_parameterized_contains_multiple_arguments[]
    @DisableIfAnyArgument(contains = { "Then", "then" })
    @ParameterizedTest
    @CsvSource(value = {
            "If the swift moment I entreat:;Tarry a while! You are so fair!",
            "Then forge the shackles to my feet,;Then I will gladly perish there!",
            "Then let them toll the passing-bell,;Then of your servitude be free,",
            "The clock may stop, its hands fall still,;And time be over then for me!"
    }, delimiter = ';')
    void disableAnyContainsMultipleArguments(String line, String line2) {
        // [...]
    }
    // end::disable_parameterized_contains_multiple_arguments[]

    // tag::disable_parameterized_matches_all_arguments[]
    @DisableIfAllArguments(matches = ".*\\s[a-z]{3}\\s.*")
    @ParameterizedTest
    @CsvSource(value = {
            "If the swift moment I entreat:;Tarry a while! You are so fair!",
            "Then forge the shackles to my feet,;Then I will gladly perish there!",
            "Then let them toll the passing-bell,;Then of your servitude be free,",
            "The clock may stop, its hands fall still,;And time be over then for me!"
    }, delimiter = ';')
    void interceptMatchesAny(String line, String line2) {
        // [...]
    }
    // end::disable_parameterized_matches_all_arguments[]

    // tag::disable_parameterized_named_parameter_contains[]
    @DisableIfArgument(name = "line2", contains = "swift")
    @ParameterizedTest
    @CsvSource({
            "If the swift moment I entreat:;Tarry a while! You are so fair!",
            "Then forge the shackles to my feet,;Then I will gladly perish there!"
    })
    void targetName(String line, String line2) {
        // [...]
    }
    // end::disable_parameterized_named_parameter_contains[]

    // tag::disable_parameterized_indexed_parameter_contains[]
    @DisableIfArgument(index = 1, contains = "swift")
    @ParameterizedTest
    @CsvSource({
            "If the swift moment I entreat:;Tarry a while! You are so fair!",
            "Then forge the shackles to my feet,;Then I will gladly perish there!"
    })
    void targetIndex(String line, String line2) {
        // [...]
    }
    // end::disable_parameterized_indexed_parameter_contains[]

    // tag::disable_parameterized_different_rules_for_different_parameters_contains[]
    @DisableIfArgument(contains = "gibberish")
    @DisableIfArgument(contains = "gladly")
    @ParameterizedTest
    @CsvSource({
            "If the swift moment I entreat:;Tarry a while! You are so fair!",
            "Then forge the shackles to my feet,;Then I will gladly perish there!"
    })
    void targetByOrder(String line, String line2) {
    }
    // end::disable_parameterized_different_rules_for_different_parameters_contains[]

    // tag::disable_parameterized_different_rules_for_different_parameters_matches[]
    // disable invocations whose argument ends with 'knew' or 'grew'
    @DisableIfArgument(matches = { ".*knew", ".*grew" })
    @ParameterizedTest
    @ValueSource(strings = {
            "Lily-like, white as snow,",
            "She hardly knew",
            "She was a woman, so",
            "Sweetly she grew"
    })
    void interceptMatches(String value) {
    }
    // end::disable_parameterized_different_rules_for_different_parameters_matches[]
}
