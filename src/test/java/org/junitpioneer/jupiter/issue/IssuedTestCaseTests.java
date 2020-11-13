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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.IssuedTestCase;

public final class IssuedTestCaseTests {

	@Test
	void testToString() {
		String expected = "IssuedTestCase{uniqueName='myName', issueId='REQ-123', result='SUCCESSFUL'}";

		IssuedTestCase sut = new IssuedTestCase("myName", "REQ-123", "SUCCESSFUL");

		String result = sut.toString();

		assertThat(result).isEqualTo(expected);
	}

}
