package org.junitpioneer.testkit.assertion.reportentry;

/**
 * Assertions for asserting the content of the published report entries.
 */
public interface ReportEntryValueAssert {

    /**
     * Asserts that the report entry has a specified key and value.
     * Fails if there are multiple report entries.
     * @param key the key of the expected report entry
     * @param value the value of the expected report entry
     */
    void withKeyAndValue(String key, String value);

    /**
     * Asserts that the report entries contain exactly the specified values (in any order).
     * @param expected the expected values of the report entries
     */
    void withValues(String... expected);

    /**
     * Asserts that the report entries contain the specified key-value pairs (in any order).
     * Fails if there are odd number of supplied strings.
     * @param keyAndValuePairs the expected key-value pairs of the report entries
     */
    void withKeyValuePairs(String... keyAndValuePairs);
}
