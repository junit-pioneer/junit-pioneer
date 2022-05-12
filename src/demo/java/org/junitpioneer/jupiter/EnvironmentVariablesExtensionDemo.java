package org.junitpioneer.jupiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class EnvironmentVariablesExtensionDemo {

    // tag::environment_clear_simple[]
    @Test
    @ClearEnvironmentVariable(key = "some variable")
    void test() {
        assertThat(System.getenv("some variable")).isNull();
    }
    // end::environment_clear_simple[]

    // tag::environment_set_simple[]
    @Test
    @SetEnvironmentVariable(
            key = "some variable",
            value = "new value")
    void test() {
        assertThat(System.getenv("some variable")).
                isEqualTo("new value");
    }
    // end::environment_set_simple[]

    // tag::environment_using_set_and_clear[]
    @Test
    @ClearEnvironmentVariable(key = "1st variable")
    @ClearEnvironmentVariable(key = "2nd variable")
    @SetEnvironmentVariable(
            key = "3rd variable",
            value = "new value")
    void test() {
        assertThat(System.getenv("1st variable")).isNull();
        assertThat(System.getenv("2nd variable")).isNull();
        assertThat(System.getenv("3rd variable"))
                .isEqualTo("new value");
    }
    // end::environment_using_set_and_clear[]

    // tag::environment_using_at_class_level[]
    @ClearEnvironmentVariable(key = "some variable")
    class MyEnvironmentVariableTest {

        @Test
        @SetEnvironmentVariable(key = "some variable",
                value = "new value")
        void test() {
            assertThat(System.getenv("some variable"))
                    .isEqualTo("new value");
        }

    }
    // end::environment_using_at_class_level[]



}
