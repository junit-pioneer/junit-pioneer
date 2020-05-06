package org.junitpioneer.testkit.assertion;

public interface RemainderAssert extends ThisManyAssert {
    FailureAssert theRestFailed();
    void theRestAborted();
    void theRestSucceeded();
}
