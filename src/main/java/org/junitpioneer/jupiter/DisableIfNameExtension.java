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

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DisableIfNameExtension implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return findClosestEnclosingAnnotation(context, DisableIfDisplayName.class)
				.map(annotation -> disable(context, annotation))
				.orElseGet(() -> enabled("No instructions to disable"));
	}

	private ConditionEvaluationResult disable(ExtensionContext context, DisableIfDisplayName disableInstruction) {
		//@formatter:off
		String displayName = context.getDisplayName();
		return disableInstruction.isRegEx()
				? disableIfMatches(displayName, disableInstruction.value())
				: disableIfContains(displayName, disableInstruction.value());
		//@formatter:on
	}

	private ConditionEvaluationResult disableIfMatches(String displayName, String[] regExps) {
		//@formatter:off
		String matches = Stream
				.of(regExps)
				.filter(displayName::matches)
				.collect(Collectors.joining("', '"));
		return matches.isEmpty()
				? enabled("Display name '" + displayName + " doesn't match any regular expression")
				: disabled("Display name '" + displayName + "' matches '" + matches + "'");
		//@formatter:on
	}

	private ConditionEvaluationResult disableIfContains(String displayName, String[] substrings) {
		//@formatter:off
		String matches = Stream
				.of(substrings)
				.filter(displayName::contains)
				.collect(Collectors.joining("', '"));
		return matches.isEmpty()
				? enabled("Display name '" + displayName + " doesn't contain any substring")
				: disabled("Display name '" + displayName + "' contains '" + matches + "'");
		//@formatter:on
	}

}
