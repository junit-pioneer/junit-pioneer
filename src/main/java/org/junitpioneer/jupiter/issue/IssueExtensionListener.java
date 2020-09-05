/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.issue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * <p>This listener creates an additional report for the Issue extension and validates it against the "issueextensionreport.xsd".
 * This is done because the default junit test report can't / should not be extended.
 * A dynamic test report which shall be used by all test tools is planed, but not yet realized, see
 * <a href="https://github.com/ota4j-team/opentest4j/issues/9">issue #9 at opentest4j</a> for current progress.</p>
 *
 * <p>The report contains issues, their tests and their result.</p>
 *
 * @see <a href="https://junit.org/junit5/docs/current/api/org.junit.platform.launcher/org/junit/platform/launcher/TestPlan.html">JUnit Docs</a>
 */
public class IssueExtensionListener implements TestExecutionListener {

	// Class variable to access it in tests
	IssueReport issueReport;

	static final String KEY_ISSUE = "Issue";

	// Cache with all tests that belong to an issue <issueId, List<UniqueIdentifier>>
	ConcurrentHashMap<String, List<String>> issueTestsCache = new ConcurrentHashMap<>();

	// Cache with tests results of test cases <UniqueIdentifier, result>
	ConcurrentHashMap<String, String> testStatusCache = new ConcurrentHashMap<>();

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		String testId = testIdentifier.getUniqueId();

		// Check if the report entry is an issue id
		Map<String, String> entryKeyValues = entry.getKeyValuePairs();
		if (entryKeyValues.containsKey(KEY_ISSUE)) {
			String issueId = entryKeyValues.get(KEY_ISSUE);

			// Store that the current test belongs to issue
			issueTestsCache.putIfAbsent(issueId, new ArrayList<>());
			issueTestsCache.get(issueId).add(testId);

		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isTest()) {
			// Store test result in cache
			testStatusCache.put(testIdentifier.getUniqueId(), testExecutionResult.getStatus().toString());
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		issueReport = new IssueReport();

		for (Map.Entry<String, List<String>> entry : issueTestsCache.entrySet()) {
			String issueId = entry.getKey();
			List<String> allTests = entry.getValue();

			Issue issue = new Issue(issueId);

			allTests.forEach((testID) -> {
				String status = testStatusCache.getOrDefault(testID, "UNKNOWN");
				issue.getTestCases().getTestCase().add(new TestCase(testID, status));
			});

			// Create no issue in report
			issueReport.getIssues().getIssue().add(issue);

		}

		writeReport(issueReport);
	}

	/**
	 * Creates the report and validates it against the XSD.
	 *
	 * @param report Report to be created.
	 */
	void writeReport(IssueReport report) {

		try {
			// Create report file (delete first, if already exists)
			// Target directory is "./build/issue/issueextensionreport.xml"
			String currentDir = System.getProperty("user.dir");
			Path xmlFile = Paths.get(currentDir, "build", "reports", "issueextensionreport.xml");
			Files.deleteIfExists(xmlFile);
			Files.createDirectories(xmlFile.getParent());
			Files.createFile(xmlFile);

			// Marshal with options
			JAXBContext jaxbContext = JAXBContext.newInstance(IssueReport.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			jaxbMarshaller.marshal(report, xmlFile.toFile());

			// Validate
			Source xmlFileSource = new StreamSource(xmlFile.toFile());
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			File xsdFile = new File(ClassLoader.getSystemResource("xsd/issueextensionreport.xsd").toURI());
			Schema schema = schemaFactory.newSchema(xsdFile);
			Validator validator = schema.newValidator();
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			validator.validate(xmlFileSource);
		}
		catch (Throwable t) { //NOSONAR
			// Throw IssueExtensionReportException regardless of checked or unchecked exceptions during execution
			throw new IssueExtensionReportException("Error while creating the pioneer report", t); //NOSONAR
		}
	}

}
