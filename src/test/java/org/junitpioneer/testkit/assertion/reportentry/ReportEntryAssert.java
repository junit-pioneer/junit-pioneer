package org.junitpioneer.testkit.assertion.reportentry;

/**
 * Assertions for asserting how many report entries were published.
 */
public interface ReportEntryAssert {

    /**
     * Asserts that the expected number of report entries were published across all executed tests.
     * @param expected the number of report entries expected to be published
     * @return a {@link ReportEntryValueAssert} for further assertions.
     */
    ReportEntryValueAssert hasNumberOfReportEntries(int expected);

    /**
     * Asserts that exactly one report entry was published across all executed tests or containers.
     * @return a {@link ReportEntryValueAssert} for further assertions.
     */
    ReportEntryValueAssert hasSingleReportEntry();

    /**
     * Asserts that no report entries were published across all executed tests or containers.
     */
    void hasNoReportEntries();

}
