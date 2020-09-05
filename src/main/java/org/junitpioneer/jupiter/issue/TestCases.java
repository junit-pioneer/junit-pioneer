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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Wrapper for a list of properties of a test object.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "testCase" })
@XmlRootElement(name = "testcases")
public class TestCases {

	@XmlElement(required = false)
	protected List<TestCase> testCase;

	public TestCases() {
		this.testCase = new ArrayList<>();
	}

	public List<TestCase> getTestCase() {
		return testCase;
	}

	public void setTestCase(List<TestCase> testCase) {
		this.testCase = testCase;
	}

}
