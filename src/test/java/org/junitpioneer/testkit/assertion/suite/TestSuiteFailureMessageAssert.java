package org.junitpioneer.testkit.assertion.suite;

public interface TestSuiteFailureMessageAssert {

    void withMessagesContainingAny(String... messageParts);

    void withMessagesContainingAll(String... messageParts);
}
