/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.testkit.assertion;

import org.junitpioneer.testkit.assertion.reportentry.ReportEntryAssert;
import org.junitpioneer.testkit.assertion.single.TestCaseAssert;
import org.junitpioneer.testkit.assertion.suite.TestSuiteAssert;

/**
 * Base interface for all {@link org.junitpioneer.testkit.ExecutionResults} assertions.
 */
public interface ExecutionResultAssert extends TestSuiteAssert, TestCaseAssert, ReportEntryAssert {
}
