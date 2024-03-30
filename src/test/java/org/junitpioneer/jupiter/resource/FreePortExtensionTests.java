/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource;

import static org.junitpioneer.testkit.PioneerTestKit.executeTestClass;
import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.net.ServerSocket;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.ExecutionResults;

@DisplayName("Free port extension")
public class FreePortExtensionTests {

	@Test
	@DisplayName("resolve FreePort parameter successfully")
	void testFreePortParameterResolution() {
		ExecutionResults results = executeTestClass(FreePortTestCaseTests.class);
		assertThat(results).hasSingleSucceededTest();
	}

	static class FreePortTestCaseTests {

		@Test
		void testFreePortParameterResolution(@New(FreePort.class) ServerSocket port) {
			Assertions.assertThat(port).isNotNull();
		}

		@Test
		void testFreePortArgumentResolution(@New(value = FreePort.class, arguments = "1334") ServerSocket port) {
			Assertions.assertThat(port).isNotNull();
			Assertions.assertThat(port.getLocalPort()).isEqualTo(1334);
		}

	}

}
