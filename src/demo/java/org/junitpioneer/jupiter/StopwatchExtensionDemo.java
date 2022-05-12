package org.junitpioneer.jupiter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

public class StopwatchExtensionDemo {

    // tag::stopwatch_demo[]
    @Stopwatch
    @Test
    void test() {
        // Some test
    }
    // end::stopwatch_demo[]
}
