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

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

public class CartesianProductTestExtensionTests {

	@CartesianProductTest(value = "0")
	void empty() {
	}

	@CartesianProductTest(value = { "0", "1", "2" })
	void singleParameter(String param) {
		int value = Integer.parseInt(param);
		Assertions.assertThat(value).isBetween(0, 2);
	}

	@CartesianProductTest({ "0", "1" })
	void threeBits(String a, String b, String c) {
		int value = Integer.parseUnsignedInt(a + b + c, 2);
		Assertions.assertThat(value).isBetween(0b000, 0b111);
	}

	@CartesianProductTest
	@DisplayName("S ⨯ T ⨯ U")
	void nFold(String string, Class<?> type, TimeUnit unit, TestInfo info) {
		Assertions.assertThat(string).endsWith("a");
		Assertions.assertThat(type).isInterface();
		Assertions.assertThat(unit.name()).endsWith("S");
		Assertions.assertThat(info.getTags()).isEmpty();
	}

	static CartesianProductTest.Sets nFold() {
		return new CartesianProductTest.Sets()
				.add("Alpha", "Omega")
				.add(Runnable.class, Comparable.class, TestInfo.class)
				.add(TimeUnit.DAYS, TimeUnit.HOURS);
	}

	@Nested
	class BadConfigurationTests {
		@Test
		@DisplayName("Test fails when there is no factory method")
		void factory1() {
			ExecutionResults results = PioneerTestKit.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "noFactory", "int");

			assertThat(results).hasSingleFailedContainer().withExceptionInstanceOf(AssertionError.class).hasMessageContaining("not found");
		}
	}

	static class BadConfigurationTest {
		@CartesianProductTest
		void noFactory(int i) {
		}

		@CartesianProductTest
		void nonStaticFactory(int i) {
		}

		CartesianProductTest.Sets nonStaticFactory() {
			return new CartesianProductTest.Sets()
					.add(1, 2, 3);
		}

		@CartesianProductTest
		void wrongReturnFactory(int i) {
		}

		static int wrongReturnFactory() {
			return 0;
		}

		@CartesianProductTest
		void parameterizedFactory(int i, int j) {
		}

		static CartesianProductTest.Sets parameterizedFactory(int i) {
			return new CartesianProductTest.Sets()
					.add(i, 1, 3, 6)
					.add(i, 2, 4, 8);
		}
	}
}
