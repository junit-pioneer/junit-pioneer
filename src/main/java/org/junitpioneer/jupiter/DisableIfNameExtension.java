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
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DisableIfNameExtension implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<DisableIfName> disable = findAnnotation(context.getElement(), DisableIfName.class);

		if (!disable.isPresent()) {
			return enabled("No instructions to disable");
		} else if (!context.getTestMethod().isPresent()) {
			return enabled("Only disable at method level");
		}
		DisableIfName details = disable.get();
		boolean toDisable;
		if (details.regex()) {
			toDisable = context.getDisplayName().matches(details.value());
		} else {
			toDisable = context.getDisplayName().contains(details.value());
		}
		//@formatter:off
		return toDisable
				? disabled(context.getDisplayName() + " matches " + details.value())
				: enabled(context.getDisplayName() + " doesn't match " + details.value());
		//@formatter:on
	}

}
