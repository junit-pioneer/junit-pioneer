package org.junitpioneer.testkit.assertion.suite;

public interface TestSuiteFailureAssert {

    TestSuiteFailureMessageAssert withExceptionInstancesOf(Class<? extends Throwable>... exceptionTypes);

    TestSuiteFailureMessageAssert withExceptions();

}
