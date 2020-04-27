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

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junitpioneer.jupiter.PioneerAnnotationUtils.findClosestEnclosingAnnotation;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DisableIfNameExtension implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<Method> testMethod = context.getTestMethod();

		if (!testMethod.isPresent()) {
			return enabled("Only disable at method level so the parameterized tests could be registered");
		}

		Optional<DisableIfDisplayName> disableIf = findClosestEnclosingAnnotation(context, DisableIfDisplayName.class);
		if (!disableIf.isPresent()) {
			return enabled("No instructions to disable");
		}
		DisableIfDisplayName disableInstruction = disableIf.get();
		String displayName = context.getDisplayName();
		StringBuilder reasonToDisable = new StringBuilder();

		//@formatter:off
		for (String value : disableInstruction.value()) {
			boolean toDisable = disableInstruction.isRegEx()
					? displayName.matches(value)
					: context.getDisplayName().contains(value);
			if (toDisable)
				reasonToDisable.append(displayName).append(" matches ").append(disableInstruction);
		}
		//@formatter:on
		String anyReason = reasonToDisable.toString();
		return anyReason.isEmpty() ? enabled("No reason to disable on display name identified") : disabled(anyReason);
	}

}
