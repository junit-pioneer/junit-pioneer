package org.junitpioneer.jupiter;

import org.junit.jupiter.api.Test;

public class DisableUntilExtensionDemo {

    // tag::disable_until_simple[]
    @DisabledUntil(date = "2022-01-01")
    @Test
    void test() {
        // Test will be skipped if it's 2021-12-31 or earlier
    }
    // end::disable_until_simple[]

    // tag::disable_until_with_reason[]
    @DisabledUntil(reason = "The remote server won't be ready until next year", date = "2022-01-01")
    @Test
    void testWithReason() {
        // Test will be skipped if it's 2021-12-31 or earlier
    }
    // end::disable_until_with_reason[]

    // tag::disable_until_at_class_level[]
    @DisabledUntil(date = "2022-01-01")
    class TestClass {
        @Test
        void test() {
            // Test will be skipped if it's 2021-12-31 or earlier
        }
    }
    // end::disable_until_at_class_level[]

}
