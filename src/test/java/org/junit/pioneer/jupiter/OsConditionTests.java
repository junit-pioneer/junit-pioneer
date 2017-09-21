/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.pioneer.jupiter;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExtensionContext;

class OsConditionTests {

	// CONTAINER -------------------------------------------------------------------

	@Test
	void evaluateContainer_notAnnotated_enabled() throws Exception {
		ExtensionContext context = createContextReturning(UnconditionalTestCase.class);
		OsCondition condition = new OsCondition();

		ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);

		assertThat(result.isDisabled()).isFalse();
		assertThat(result.getReason()).contains(OsCondition.NO_CONDITION_PRESENT);
	}

	@Test
	void evaluateContainer_disabledOnOtherOs_enabled() throws Exception {
		ExtensionContext context = createContextReturning(DisabledOnLinuxTestCase.class);
		OsCondition conditionOnWindows = new OsCondition(() -> OS.WINDOWS);

		ConditionEvaluationResult result = conditionOnWindows.evaluateExecutionCondition(context);

		assertEnabledOn(result, OS.WINDOWS);
	}

	@Test
	void evaluateContainer_disabledOnRunningOs_disabled() throws Exception {
		ExtensionContext context = createContextReturning(DisabledOnLinuxTestCase.class);
		OsCondition conditionOnWindows = new OsCondition(() -> OS.LINUX);

		ConditionEvaluationResult result = conditionOnWindows.evaluateExecutionCondition(context);

		assertDisabledOn(result, OS.LINUX);
	}

	@Test
	void evaluateContainer_enabledOnOtherOs_disabled() throws Exception {
		ExtensionContext context = createContextReturning(EnabledOnLinuxTestCase.class);
		OsCondition conditionOnWindows = new OsCondition(() -> OS.WINDOWS);

		ConditionEvaluationResult result = conditionOnWindows.evaluateExecutionCondition(context);

		assertDisabledOn(result, OS.WINDOWS);
	}

	@Test
	void evaluateContainer_enabledOnRunningOs_enabled() throws Exception {
		ExtensionContext context = createContextReturning(EnabledOnLinuxTestCase.class);
		OsCondition conditionOnWindows = new OsCondition(() -> OS.LINUX);

		ConditionEvaluationResult result = conditionOnWindows.evaluateExecutionCondition(context);

		assertEnabledOn(result, OS.LINUX);
	}

	// TESTS -------------------------------------------------------------------

	@Test
	void evaluateMethod_notAnnotated_enabled() throws Exception {
		ExtensionContext context = createContextReturning(UnconditionalTestCase.class, "unconditionalTest");
		OsCondition condition = new OsCondition();

		ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);

		assertThat(result.isDisabled()).isFalse();
		assertThat(result.getReason()).contains(OsCondition.NO_CONDITION_PRESENT);
	}

	@Test
	void evaluateMethod_disabledOnOtherOs_enabled() throws Exception {
		ExtensionContext context = createContextReturning(EnabledAndDisabledTestMethods.class, "disabledOnLinuxTest");
		OsCondition condition = new OsCondition(() -> OS.WINDOWS);

		ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);

		assertEnabledOn(result, OS.WINDOWS);
	}

	@Test
	void evaluateMethod_disabledOnRunningOs_disabled() throws Exception {
		ExtensionContext context = createContextReturning(EnabledAndDisabledTestMethods.class, "disabledOnLinuxTest");
		OsCondition condition = new OsCondition(() -> OS.LINUX);

		ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);

		assertDisabledOn(result, OS.LINUX);
	}

	@Test
	void evaluateMethod_enabledOnOtherOs_disabled() throws Exception {
		ExtensionContext context = createContextReturning(EnabledAndDisabledTestMethods.class, "enabledOnLinuxTest");
		OsCondition condition = new OsCondition(() -> OS.WINDOWS);

		ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);

		assertDisabledOn(result, OS.WINDOWS);
	}

	@Test
	void evaluateMethod_enabledOnRunningOs_enabled() throws Exception {
		ExtensionContext context = createContextReturning(EnabledAndDisabledTestMethods.class, "enabledOnLinuxTest");
		OsCondition condition = new OsCondition(() -> OS.LINUX);

		ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);

		assertEnabledOn(result, OS.LINUX);
	}

	// HELPER -------------------------------------------------------------------

	private static ExtensionContext createContextReturning(Class<?> type) throws NoSuchMethodException {
		ExtensionContext context = mock(ExtensionContext.class);
		when(context.getElement()).thenReturn(asElement(type));
		return context;
	}

	private static Optional<AnnotatedElement> asElement(Class<?> type) throws NoSuchMethodException {
		return asElement(type, "");
	}

	private static ExtensionContext createContextReturning(Class<?> type, String methodName)
			throws NoSuchMethodException {
		ExtensionContext context = mock(ExtensionContext.class);
		when(context.getElement()).thenReturn(asElement(type, methodName));
		return context;
	}

	private static Optional<AnnotatedElement> asElement(Class<?> type, String methodName) throws NoSuchMethodException {
		if (methodName.isEmpty())
			return Optional.of(type);
		return Optional.of(type.getDeclaredMethod(methodName, (Class<?>[]) null));
	}

	private static void assertEnabledOn(ConditionEvaluationResult result, OS os) {
		assertThat(result.isDisabled()).isFalse();
		assertThat(result.getReason()).contains(format(OsCondition.TEST_ENABLED, os));
	}

	private static void assertDisabledOn(ConditionEvaluationResult result, OS os) {
		assertThat(result.isDisabled()).isTrue();
		assertThat(result.getReason()).contains(format(OsCondition.TEST_DISABLED, os));
	}

	// TEST CASES -------------------------------------------------------------------

	private static class UnconditionalTestCase {

		void unconditionalTest() {
		}

	}

	@DisabledOnOs(OS.LINUX)
	private static class DisabledOnLinuxTestCase {
	}

	@EnabledOnOs(OS.LINUX)
	private static class EnabledOnLinuxTestCase {
	}

	private static class EnabledAndDisabledTestMethods {

		@DisabledOnOs(OS.LINUX)
		void disabledOnLinuxTest() {
		}

		@EnabledOnOs(OS.LINUX)
		void enabledOnLinuxTest() {
		}

	}

}
