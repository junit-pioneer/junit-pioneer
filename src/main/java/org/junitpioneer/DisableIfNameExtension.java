/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer;

import static java.util.Objects.isNull;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junitpioneer.jupiter.DisableIfName;

public class DisableIfNameExtension implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		DisableIfName meta = context.getElement().map(a -> a.getAnnotation(DisableIfName.class)).orElse(null);

		if (isNull(meta)) {
			return ConditionEvaluationResult.enabled("No instructions to disable");
		} else if (!context.getTestMethod().isPresent()) {
			return ConditionEvaluationResult.enabled("Only disable at method level");
		}

		boolean disable = meta.regex() ? context.getDisplayName().matches(meta.value())
				: context.getDisplayName().contains(meta.value());
		return disable ? ConditionEvaluationResult.disabled(context.getDisplayName() + " matches " + meta.value())
				: ConditionEvaluationResult.enabled(context.getDisplayName() + " doesn't match " + meta.value());
	}

}
