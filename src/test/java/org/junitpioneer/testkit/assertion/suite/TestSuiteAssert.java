package org.junitpioneer.testkit.assertion.suite;

/**
 * Assertions for asserting multiple tests as part of a suite.
 */
public interface TestSuiteAssert {

    /**
     * Asserts that there were exactly {@code expected} number of started tests.
     * @param expected the expected number of started tests
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfStartedTests(int expected);

    /**
     * Asserts that there were exactly {@code expected} number of failed tests.
     * @param expected the expected number of failed tests
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfFailedTests(int expected);

    /**
     * Asserts that there were exactly {@code expected} number of aborted tests.
     * @param expected the expected number of aborted tests
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfAbortedTests(int expected);

    /**
     * Asserts that there were exactly {@code expected} number of succeeded tests.
     * @param expected the expected number of succeeded tests
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfSucceededTests(int expected);

    /**
     * Asserts that there were exactly {@code expected} number of skipped tests.
     * @param expected the expected number of skipped tests
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfSkippedTests(int expected);

    /**
     * Asserts that there were exactly {@code expected} number of dynamically registered tests.
     * @param expected the expected number of dynamically registered tests
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfDynamicallyRegisteredTests(int expected);

    /**
     * Asserts that there were exactly {@code expected} number of started containers.
     * @param expected the expected number of started containers
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfStartedContainers(int expected);

    /**
     * Asserts that there were exactly {@code expected} number of failed containers.
     * @param expected the expected number of failed containers
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfFailedContainers(int expected);

    /**
     * Asserts that there were exactly {@code expected} number of aborted containers.
     * @param expected the expected number of aborted containers
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfAbortedContainers(int expected);

    /**
     * Asserts that there were exactly {@code expected} number of succeeded containers.
     * @param expected the expected number of succeeded containers
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfSucceededContainers(int expected);

    /**
     * Asserts that there were exactly {@code expected} number of skipped containers.
     * @param expected the expected number of skipped containers
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfSkippedContainers(int expected);

    /**
     * Asserts that there were exactly {@code expected} number of dynamically registered containers.
     * @param expected the expected number of dynamically registered containers
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteAssert hasNumberOfDynamicallyRegisteredContainers(int expected);

    /**
     * Asserts that there was at least one failing test.
     * Use this to assert exceptions thrown by the failing tests in the suit.
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteFailureAssert hasFailingTests();

    /**
     * Asserts that there were exactly {@code expected} number of failed containers.
     * @return a {@code TestSuiteAssert} for further assertions.
     */
    TestSuiteFailureAssert hasFailingContainers();
    
}
