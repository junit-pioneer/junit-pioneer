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

import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.AbstractPioneerTestEngineTests;
import org.junitpioneer.vintage.Test;

@ExtendWith(IssueExtension.class)
public class IssueTests extends AbstractPioneerTestEngineTests {

	@Test
	void testIssueAnnotation() {

	}

	static class IssueTestCase {

		@Test
		void testNoAnnotation() {

		}

		@Issue("Req 11")
		@Test
		void testIsAnnotated() {

		}
	}
}
