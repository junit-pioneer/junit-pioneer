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
        // TestReporter onl hast methods to publish Data, but not to recieve Data (from the context)

        // CURRENT (Seen by System.out.println in Extension): Stopwatch is executed [OK]
        // Passing an "ExtensionContext"-Parameter does not work as the parameter is not resolved
        // No ParameterResolver registered for parameter [org.junit.jupiter.api.extension.ExtensionContext arg0]
    }

    @Test
    void runMethodLevelAnnotationTest() {
        ExecutionEventRecorder eventRecorder = executeTests(StopwatchExtensionTests.MethodLevelAnnotationTest.class,
                "stopwatchExtensionShouldBeExecutedOnWithAnnotationOnMethodLevel");

        assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
        // How to access context to check stored values

        // CURRENT (Seen by System.out.println in Extension): Stopwatch is executed [OK]
    }

    @Test
    void runNonLevelAnnotationTest() {
        ExecutionEventRecorder eventRecorder = executeTests(StopwatchExtensionTests.NonAnnotationTest.class,
                "stopwatchExtensionShouldNotBeExecuted");

        assertThat(eventRecorder.getTestSuccessfulCount()).isEqualTo(1);
        // How to access context to check stored values

        // CURRENT  (Seen by System.out.println in Extension): Stopwatch is NOT executed [OK]
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
