package org.junitpioneer.testkit.assertion.suite;

public interface TestSuiteFailureAssert extends TestSuiteAssert {

    TestSuiteFailureMessageAssert withExceptionInstancesOf(Class<? extends Throwable>... exceptionTypes);

    TestSuiteFailureMessageAssert withExceptions();

}
