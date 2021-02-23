/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class IssueTestSuiteTests {

	@Test
	@Disabled("Don't use EqualsVerifier for a while - see #324")
	public void equalsContract() {
		EqualsVerifier
				.forClass(IssueTestSuite.class)
				.withNonnullFields("issueId", "tests")
				// `equals` relies on `issueId` and `tests`, but `hashCode` only uses `issueId`
				.suppress(Warning.STRICT_HASHCODE)
				.verify();
	}

}
