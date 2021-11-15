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

import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Free port extension")
public class FreePortExtensionTests {

	@Test
	@DisplayName("resolve FreePort parameter successfully")
	void testFreePortParameterResolution() {
		ExecutionResults results = PioneerTestKit.executeTestClass(FreePortTestCase.class);
		assertThat(results).hasSingleSucceededTest();
	}

	@ExtendWith(FreePortExtension.class)
	static class FreePortTestCase {

		@Test
		void testFreePortParameterResolution(FreePort port) {
			Assertions.assertThat(port).isNotNull();
			Assertions.assertThat(port.isFreeNow()).isTrue();
		}

	}

}
