/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.PreconditionViolationException;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;
import org.junitpioneer.testkit.assertion.PioneerAssert;

@DisplayName("ObjectMapperProvider interface")
public class ObjectMapperProviderTests {

	@Test
	@DisplayName("throws a precondition violation if trying to use a blank value")
	void blank() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(ObjectMapperProviderTests.ObjectMapperProviderTestCases.class,
					"blank", String.class, int.class);

		PioneerAssert
				.assertThat(results)
				.hasSingleFailedContainer()
				.withExceptionInstanceOf(PreconditionViolationException.class)
				.hasMessageContaining("must not have a blank value");
	}

	@Test
	@DisplayName("throws a precondition violation if trying to use an unknown value")
	void unknown() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(ObjectMapperProviderTests.ObjectMapperProviderTestCases.class,
					"unknown", String.class, int.class);

		PioneerAssert
				.assertThat(results)
				.hasSingleFailedContainer()
				.withExceptionInstanceOf(PreconditionViolationException.class)
				.hasMessageContaining("Could not find custom object mapper");
	}

	@Test
	@DisplayName("works with a custom object mapper")
	void custom() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(ObjectMapperProviderTests.ObjectMapperProviderTestCases.class,
					"custom", String.class, int.class);

		PioneerAssert
				.assertThat(results)
				.hasSingleFailedContainer()
				.withExceptionInstanceOf(PreconditionViolationException.class)
				.hasMessageContaining("Could not find custom object mapper");
	}

	static class ObjectMapperProviderTestCases {

		@ParameterizedTest
		@UseObjectMapper("")
		@JsonSource("[ { name: 'Luke', height: 172  }, { name: 'Yoda', height: 66 } ]")
		void blank(@Property("name") String name, @Property("height") int height) {
		}

		@ParameterizedTest
		@UseObjectMapper("unknown")
		@JsonSource("[ { name: 'Luke', height: 172  }, { name: 'Yoda', height: 66 } ]")
		void unknown(@Property("name") String name, @Property("height") int height) {
		}

		@ParameterizedTest
		@UseObjectMapper("dummy")
		@JsonSource("[ { name: 'Luke', height: 172  }, { name: 'Yoda', height: 66 } ]")
		void custom(@Property("name") String name, @Property("height") int height) {
		}

	}

}
