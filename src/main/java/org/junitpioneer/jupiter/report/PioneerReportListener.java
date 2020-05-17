/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.report;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
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
import org.xml.sax.SAXException;

/**
 * <p>This listener creates an additional report for pioneer extension and validates it against the "pioneerreport.xsd".
 * This is done because the default junit test report can't / should not be extended.
 * A dynamic test report which shall be used by all test tools is planed, but not yet realized, see
 * <a href="https://github.com/ota4j-team/opentest4j/issues/9">issue #9 at opentest4j</a> for current progress.</p>
 *
 * <p>The report contains test containers and its test methods. For each object properties, based on published
 * report entries are reported. This allows extension to report additional values in the report.
 * for test cases the result of the test is (of course) reported.</p>
 *
 * @see <a href="https://junit.org/junit5/docs/current/api/org.junit.platform.launcher/org/junit/platform/launcher/TestPlan.html">JUnit Docs</a>
 */
public class PioneerReportListener implements TestExecutionListener {

	TestPlan storedTestPlan;
	PioneerReport pioneerReport;

	ConcurrentHashMap<String, List<Property>> propertyCache = new ConcurrentHashMap<>();
	ConcurrentHashMap<String, String> testStatusCache = new ConcurrentHashMap<>();

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		String id = testIdentifier.getUniqueId();

		// Store published entries in cache
		propertyCache.putIfAbsent(id, new ArrayList<>());
		entry.getKeyValuePairs().forEach((key, value) -> propertyCache.get(id).add(new Property(key, value)));
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isTest()) {
			// Store result in cache
			testStatusCache.put(testIdentifier.getUniqueId(), testExecutionResult.getStatus().toString());
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		// Store plan to access it from methods
		this.storedTestPlan = testPlan;
		pioneerReport = new PioneerReport();

		Set<TestIdentifier> allRoots = testPlan.getRoots();

		for (TestIdentifier identifier : allRoots) {
			if (identifier.isContainer()) {
				pioneerReport.getTestContainer().add(processContainer(identifier));
			} else if (identifier.isTest()) {
				pioneerReport.getTestCase().add(processTest(identifier));
			}
		}
		writeReport(pioneerReport);
	}

	/**
	 * Converts an {@code TestIdentifier} object into a {@code Testcontainer} for the report.
	 *
	 * @param container Object to be converted into {@code Testcontainer}
	 * @return Created object
	 */
	TestContainer processContainer(TestIdentifier container) {
		TestContainer testcontainer = new TestContainer(container.getDisplayName());

		// Add properties, if there are any
		String uniqueId = container.getUniqueId();
		Properties allProps = getProperties(uniqueId);

		if (!allProps.getProperty().isEmpty()) {
			testcontainer.properties = allProps;
		}

		Set<TestIdentifier> allChildren = storedTestPlan.getChildren(uniqueId);

		for (TestIdentifier identifier : allChildren) {
			if (identifier.isContainer()) {
				testcontainer.getTestContainer().add(processContainer(identifier));
			} else if (identifier.isTest()) {
				testcontainer.getTestCase().add(processTest(identifier));
			}
		}

		return testcontainer;
	}

	/**
	 * Converts an {@code TestIdentifier} object into a {@code Testcase} for the report.
	 *
	 * @param test Object to be converted into {@code Testcase} element
	 * @return Created object
	 */
	TestCase processTest(TestIdentifier test) {
		String status = testStatusCache.getOrDefault(test.getUniqueId(), "UNKNOWN");
		TestCase testcase = new TestCase(test.getDisplayName(), status);

		// Add properties, if there are any
		Properties allProps = getProperties(test.getUniqueId());

		if (!allProps.getProperty().isEmpty()) {
			testcase.setProperties(allProps);
		}

		return testcase;
	}

	/**
	 * Retrieves all cached properties for an {@code TestIdentifier} by its unique id.
	 *
	 * @param testIdentifierUniqueId Unique id the properties should be loaded for.
	 * @return Wrapper which contains all cached properties or an empty list.
	 */
	Properties getProperties(String testIdentifierUniqueId) {
		Properties allProperties = new Properties();

		if (propertyCache.containsKey(testIdentifierUniqueId)) {
			allProperties.getProperty().addAll(propertyCache.get(testIdentifierUniqueId));
		}

		return allProperties;
	}

	/**
	 * Creates the report and validates it against the XSD.
	 *
	 * @param report Report to be created.
	 */
	void writeReport(PioneerReport report) {

		try {
			// Create report file (delete first, if already exists)
			// Target directory is "./build/reports/pioneerreport.xml"
			String currentDir = System.getProperty("user.dir");
			Path xmlFile = Paths.get(currentDir, "build", "reports", "pioneerreport.xml");
			Files.deleteIfExists(xmlFile);
			Files.createDirectories(xmlFile.getParent());
			Files.createFile(xmlFile);

			// Marshal with options
			JAXBContext jaxbContext = JAXBContext.newInstance(PioneerReport.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			jaxbMarshaller.marshal(report, xmlFile.toFile());

			// Validate
			Source xmlFileSource = new StreamSource(xmlFile.toFile());
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			File xsdFile = new File(ClassLoader.getSystemResource("xsd/pioneerreport.xsd").toURI());
			Schema schema = schemaFactory.newSchema(xsdFile);
			Validator validator = schema.newValidator();
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			validator.validate(xmlFileSource);
		}
		catch (JAXBException | IOException | SAXException | URISyntaxException e) {
			// Throw RuntimeException to break execution regardless of caller
			throw new RuntimeException("Error while creating the pioneer report", e); //NOSONAR
		}
	}

}
