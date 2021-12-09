package org.junitpioneer.jupiter;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

class ResourcesDirTests {

    @DisplayName("when a test class has a test method with a @Dir-annotated parameter")
    @Nested
    class WhenTestClassHasTestMethodWithDirParameterTests {

        @DisplayName("then the parameter is populated with a new readable and writeable temporary directory "
                + "that lasts as long as the test")
        @Test
        void thenParameterIsPopulatedWithNewReadableAndWriteableTempDirThatLastsAsLongAsTheTest() {
            ExecutionResults executionResults = PioneerTestKit
                    .executeTestClass(ResourcesTests.SingleTestMethodWithDirParameterTestCase.class);
            assertThat(executionResults).hasSingleSucceededTest();
            assertThat(ResourcesTests.SingleTestMethodWithDirParameterTestCase.recordedPath).doesNotExist();
        }

    }

}
