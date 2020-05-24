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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents a test container in the Pioneer report.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "properties", "testContainer", "testCase" })
@XmlRootElement(name = "testContainer")
public class TestContainer {

	protected Properties properties;
	protected List<TestContainer> testContainer; //NOSONAR renaming would produce invalid XML
	protected List<TestCase> testCase;
	@XmlAttribute(name = "name")
	protected String name;

	public TestContainer() {
		// Needed for marshalling
	}

	public TestContainer(String name) {
		this.name = name;

		this.testContainer = new ArrayList<>();
		this.testCase = new ArrayList<>();
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties value) {
		this.properties = value;
	}

	public List<TestContainer> getTestContainer() {
		return this.testContainer;
	}

	public List<TestCase> getTestCase() {
		return this.testCase;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

}
