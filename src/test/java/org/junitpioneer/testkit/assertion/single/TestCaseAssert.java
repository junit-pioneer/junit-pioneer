package org.junitpioneer.testkit.assertion.single;

/**
 * Assertions for asserting the state of single tests/containers.
 */
public interface TestCaseAssert {

    TestCaseStartedAssert hasSingleStartedTest();

    TestCaseFailureAssert hasSingleFailedTest();

    void hasSingleAbortedTest();

    /**
     * Asserts that there was exactly one successful test.
     */
    void hasSingleSucceededTest();

    void hasSingleSkippedTest();

    TestCaseStartedAssert hasSingleDynamicallyRegisteredTest();

    TestCaseStartedAssert hasSingleStartedContainer();

    TestCaseFailureAssert hasSingleFailedContainer();

    void hasSingleAbortedContainer();

    void hasSingleSucceededContainer();

    void hasSingleSkippedContainer();

    TestCaseStartedAssert hasSingleDynamicallyRegisteredContainer();

}
