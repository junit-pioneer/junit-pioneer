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

import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

public class CartesianProductTestExtensionTests {

	@Nested
	class StandardBehaviouralTests {

		@CartesianProductTest(value = { "0", "1" })
		@DisplayName("Does nothing if there are no parameters")
		void empty() {
		}

		@CartesianProductTest(value = { "0", "1", "2" })
		@DisplayName("Runs for each parameter once for single parameter")
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

		@CartesianProductTest(factory = "supplyValues")
		@DisplayName("Can have an explicit factory method over the implicit one")
		void explicitFactory(String string, TimeUnit unit) {
			Assertions.assertThat(string).isIn("War", "Peace");
			Assertions.assertThat(unit.name()).endsWith("S");
		}

	}

	static CartesianProductTest.Sets nFold() {
		return new CartesianProductTest.Sets()
				.add("Alpha", "Omega")
				.add(Runnable.class, Comparable.class, TestInfo.class)
				.add(TimeUnit.DAYS, TimeUnit.HOURS);
	}

	static CartesianProductTest.Sets supplyValues() {
		//@formatter:off
		return new CartesianProductTest.Sets()
				.add("War", "Peace")
				.add(TimeUnit.SECONDS, TimeUnit.DAYS);
		//@formatter:on
	}

	@Nested
	@DisplayName("CartesianProduct test fails when")
	class BadConfigurationTests {

		@Test
		@DisplayName("there is no factory method")
		void throwsForMissingFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "noFactory", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(AssertionError.class)
					.hasMessageContaining("not found");
		}

		@Test
		@DisplayName("there is implicit factory method but explicit factory name was given")
		void throwsForMissingExplicitFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "hasImplicitFactory", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(AssertionError.class)
					.hasMessageContaining("not found");
		}

		@Test
		@DisplayName("the factory is not static")
		void throwsForNonStaticFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "nonStaticFactory", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(AssertionError.class)
					.hasMessageContaining("must be static");
		}

		@Test
		@DisplayName("the factory does not return Sets")
		void throwsForWrongReturnValueFactoryMethod() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "wrongReturnFactory", int.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(AssertionError.class)
					.hasMessageContaining("must return");
		}

		@Test
		@DisplayName("the factory does not produce enough parameters")
		void throwsForTooFewFactoryMethodParameters() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "incompleteFactory", int.class,
						String.class, TimeUnit.class);

			assertThat(results).hasNumberOfDynamicallyRegisteredTests(3).hasNumberOfFailedTests(3);
		}

		@Test
		@DisplayName("the factory produces too much parameters")
		void throwsForTooManyFactoryMethodParameters() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "bloatedFactory", int.class,
						String.class);

			assertThat(results)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ParameterResolutionException.class)
					.hasMessageContaining("must register values for each parameter");
		}

		@Test
		@DisplayName("the factory produces parameters in the wrong order")
		void throwsForFactoryWithWrongParameterOrder() {
			ExecutionResults results = PioneerTestKit
					.executeTestMethodWithParameterTypes(BadConfigurationTest.class, "wrongOrder", String.class,
						int.class);

			assertThat(results)
					.hasNumberOfDynamicallyRegisteredTests(6)
					.hasNumberOfFailedTests(6)
					.hasSingleFailedContainer()
					.withExceptionInstanceOf(ParameterResolutionException.class);
		}

	}

	static class BadConfigurationTest {

		@CartesianProductTest
		void noFactory(int i) {
		}

		@CartesianProductTest(factory = "nonExistentFactory")
		void hasImplicitFactory(int i) {
		}

		static CartesianProductTest.Sets hasImplicitFactory() {
			return new CartesianProductTest.Sets().add(1, 2, 3);
		}

		@CartesianProductTest
		void nonStaticFactory(int i) {
		}

		CartesianProductTest.Sets nonStaticFactory() {
			return new CartesianProductTest.Sets().add(1, 2, 3);
		}

		@CartesianProductTest
		void wrongReturnFactory(int i) {
		}

		static int wrongReturnFactory() {
			return 0;
		}

		@CartesianProductTest
		void incompleteFactory(int number, String string, TimeUnit unit) {
		}

		static CartesianProductTest.Sets incompleteFactory() {
			return new CartesianProductTest.Sets().add(1, 4, 9);
		}

		@CartesianProductTest
		void bloatedFactory(int number, String string) {
		}

		static CartesianProductTest.Sets bloatedFactory() {
			return new CartesianProductTest.Sets()
					.add(1, 2, 3)
					.add("Baba is you", "Fire is hot")
					.add(TimeUnit.DAYS)
					.add("Mucho Gusto");
		}

		@CartesianProductTest(factory = "getParams")
		void wrongOrder(String string, int i) {

		}

		static CartesianProductTest.Sets getParams() {
			return new CartesianProductTest.Sets().add(1, 2, 4).add("Message #1", "Message #2");
		}

	}

}
