/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult.Status;

public final class IssueTestCaseTests {

	@Test
	void testToString() {
		String expected = "IssueTestCase{uniqueName='myName', result='SUCCESSFUL'}";
		IssueTestCase sut = new IssueTestCase("myName", Status.SUCCESSFUL);

		String result = sut.toString();

		assertThat(result).isEqualTo(expected);
	}

}
