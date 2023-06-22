/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class IssueTestSuiteTests {

	@Test
	public void equalsContract() {
		EqualsVerifier
				.forClass(IssueTestSuite.class)
				.withNonnullFields("issueId", "tests")
				// `equals` relies on `issueId` and `tests`, but `hashCode` only uses `issueId`
				.suppress(Warning.STRICT_HASHCODE)
				.verify();
	}

}
