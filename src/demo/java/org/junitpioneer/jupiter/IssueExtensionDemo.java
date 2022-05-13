package org.junitpioneer.jupiter;

import org.junit.jupiter.api.Test;

import java.util.List;

public class IssueExtensionDemo {

    // tag::issue_simple[]
    @Issue("REQ-123")
    @Test
    void test() {
        // One of the tests for the issue with the id "REQ-123"
    }
    // end::issue_simple[]

    // tag::issue_processor_sample[]
    public class SimpleProcessor implements IssueProcessor {

        @Override
        public void processTestResults(
                List<IssueTestSuite> allResults) {
            for(IssueTestSuite testSuite : allResults) {
                System.out.println(testSuite.issueId());
            }
        }

    }
    // end::issue_processor_sample[]
}
