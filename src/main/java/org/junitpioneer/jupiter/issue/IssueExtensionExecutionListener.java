/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.issue;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.junit.platform.engine.TestExecutionResult.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junitpioneer.jupiter.IssueProcessor;
import org.junitpioneer.jupiter.IssueTestSuite;

/**
 * <p>This listener collects the names and results of all tests, which are annotated with the {@link org.junitpioneer.jupiter.Issue @Issue} annotation.
 * After all tests are finished the results are provided to an {@link IssueProcessor} for further processing.</p>
 */
public class IssueExtensionExecutionListener implements TestExecutionListener {

	public static final String REPORT_ENTRY_KEY = "IssueExtension";

	/**
	 * This listener will be active as soon as Pioneer is on the class/module path, regardless of whether {@code @Issue} is actually used.
	 * To prevent superfluous computation and memory use, we "deactivate" this listener if it is not needed.
	 * That's the case when we detect no {@code IssueProcessor} - presumably nobody uses this extension then.
	 */
	private final boolean active;
	private final ConcurrentMap<String, IssueTestCaseBuilder> testCases;

	public IssueExtensionExecutionListener() {
		this.active = ServiceLoader.load(IssueProcessor.class).iterator().hasNext();
		this.testCases = new ConcurrentHashMap<>();
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		if (!active)
			return;

		String testId = testIdentifier.getUniqueId();
		Map<String, String> messages = entry.getKeyValuePairs();

		if (messages.containsKey(REPORT_ENTRY_KEY)) {
			String issueId = messages.get(REPORT_ENTRY_KEY);
			testCases.put(testId, new IssueTestCaseBuilder(testId).setIssueId(issueId));
		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (!active)
			return;

		if (testIdentifier.isTest()) {
			String testId = testIdentifier.getUniqueId();
			// this implicitly assumes that report entries are published before test execution finishes,
			// which (a) makes sense and (b) allows us to only gather information on @Issue annotated tests
			if (testCases.containsKey(testId)) {
				Status result = testExecutionResult.getStatus();
				testCases.get(testId).setResult(result);
			}
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		if (!active)
			return;

		List<IssueTestSuite> issueTestSuites = createIssueTestSuites();
		for (IssueProcessor issueProcessor : ServiceLoader.load(IssueProcessor.class)) {
			issueProcessor.processTestResults(issueTestSuites);
		}
	}

	List<IssueTestSuite> createIssueTestSuites() {
		//@formatter:off
		List<IssueTestSuite> suites = testCases
				.values().stream()
				.collect(toMap(IssueTestCaseBuilder::getIssueId, builder -> new ArrayList<>(Arrays.asList(builder)),
						(builders1, builders2) -> {
							builders1.addAll(builders2);
							return builders1;
						}))
				.entrySet().stream()
				.map(issueIdWithTestCases -> new IssueTestSuite(
						issueIdWithTestCases.getKey(),
						issueIdWithTestCases
								.getValue().stream()
								.map(IssueTestCaseBuilder::build)
								.collect(toList())))
				.collect(toList());
		//@formatter:on
		return Collections.unmodifiableList(suites);
	}

}
