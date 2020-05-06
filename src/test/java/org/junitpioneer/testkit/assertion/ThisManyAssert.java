package org.junitpioneer.testkit.assertion;

public interface ThisManyAssert {
    RemainderAssert andThisManyFailed(int count);
    RemainderAssert andThisManyAborted(int count);
    RemainderAssert andThisManySucceeded(int count);
}
