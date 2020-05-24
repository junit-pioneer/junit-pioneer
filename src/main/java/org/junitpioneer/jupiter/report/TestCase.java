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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents a test method in the Pioneer report.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "properties" })
@XmlRootElement(name = "testCase")
public class TestCase {

	protected Properties properties;
	@XmlAttribute(name = "name", required = true)
	protected String name;
	@XmlAttribute(name = "status")
	protected String status;

	public TestCase() {
	}

	public TestCase(String name, String status) {
		this.name = name;
		this.status = status;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties value) {
		this.properties = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String value) {
		this.name = value;
	}

	public String getStatus() {
		return status;
	}

	//	public void setStatus(String value) {
	//		this.status = value;
	//	}

}
