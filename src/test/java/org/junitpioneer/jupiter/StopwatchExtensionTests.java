package org.junitpioneer.jupiter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junitpioneer.AbstractPioneerTestEngineTests;

import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("Stopwatch extension")
public class StopwatchExtensionTests  extends AbstractPioneerTestEngineTests {

    @Test
    void runClassLevelAnnotationTest() {
        ExecutionEventRecorder eventRecorder = executeTests(StopwatchExtensionTests.ClassLevelAnnotationTest.class,
                "stopwatchExtensionShouldBeExecutedWithAnnotationOnClassLevel");

        assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
        // How to access context to check stored values

        // CURRENT: Stopwatch is NOT executed
    }

    @Test
    void runMethodLevelAnnotationTest() {
        ExecutionEventRecorder eventRecorder = executeTests(StopwatchExtensionTests.MethodLevelAnnotationTest.class,
                "stopwatchExtensionShouldBeExecutedOnWithAnnotationOnMethodLevel");

        assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
        // How to access context to check stored values

        // CURRENT: Stopwatch is executed
    }

    @Test
    void runNonLevelAnnotationTest() {
        ExecutionEventRecorder eventRecorder = executeTests(StopwatchExtensionTests.NonAnnotationTest.class,
                "stopwatchExtensionShouldNotBeExecuted");

        assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
        // How to access context to check stored values

        // CURRENT: Stopwatch is NOT executed
    }


    @Stopwatch
    static class ClassLevelAnnotationTest {

        @Test
        void stopwatchExtensionShouldBeExecutedWithAnnotationOnClassLevel() {
        }
    }

    static class MethodLevelAnnotationTest {

        @Stopwatch
        @Test
        void stopwatchExtensionShouldBeExecutedOnWithAnnotationOnMethodLevel() {

        }
    }

    static class NonAnnotationTest {

        @Test
        void stopwatchExtensionShouldNotBeExecuted() {

        }
    }

}
