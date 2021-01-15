/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junitpioneer.internal.PioneerAnnotationUtils;

public class DisableIfNameExtension implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		/* We need to make sure not to accidentally disable the @ParameterizedTest method itself.
		 * Since the Jupiter API offers no way to identify that case directly, we use a hack that relies
		 * on the fact that the invocations' unique IDs end with a "test-template-invocation section."
		 * The @ParameterizedTest-annotated method's own unique ID does not contain that string.
		 */
		if (!context.getUniqueId().contains("test-template-invocation"))
			return enabled("Never disable parameterized test method itself");
		return PioneerAnnotationUtils
				.findClosestEnclosingAnnotation(context, DisableIfDisplayName.class)
				.map(annotation -> disable(context, annotation))
				.orElseGet(() -> enabled("No instructions to disable"));
	}

	private ConditionEvaluationResult disable(ExtensionContext context, DisableIfDisplayName disableInstruction) {
		String[] substrings = disableInstruction.contains();
		String[] regExps = disableInstruction.matches();
		boolean checkSubstrings = substrings.length > 0;
		boolean checkRegExps = regExps.length > 0;

		if (!checkSubstrings && !checkRegExps)
			throw new ExtensionConfigurationException(
				"@DisableIfDisplayName requires that either `contains` or `matches` is specified, but both are empty.");

		String displayName = context.getDisplayName();
		ConditionEvaluationResult substringResults = disableIfContains(displayName, substrings);
		ConditionEvaluationResult regExpResults = disableIfMatches(displayName, regExps);

		if (checkSubstrings && checkRegExps)
			return checkResults(substringResults, regExpResults);
		if (checkSubstrings)
			return substringResults;
		return regExpResults;
	}

	private ConditionEvaluationResult checkResults(ConditionEvaluationResult substringResults,
			ConditionEvaluationResult regExpResults) {
		boolean disabled = substringResults.isDisabled() || regExpResults.isDisabled();
		String reason = format("%s %s", substringResults.getReason(), regExpResults.getReason());
		return disabled ? disabled(reason) : enabled(reason);
	}

	private ConditionEvaluationResult disableIfMatches(String displayName, String[] regExps) {
		//@formatter:off
		String matches = Stream
				.of(regExps)
				.filter(displayName::matches)
				.collect(Collectors.joining("', '"));
		return matches.isEmpty()
				? enabled(reason(displayName, "doesn't match any regular expression."))
				: disabled(reason(displayName, format("matches '%s'.",matches)));
		//@formatter:on
	}

	private ConditionEvaluationResult disableIfContains(String displayName, String[] substrings) {
		//@formatter:off
		String matches = Stream
				.of(substrings)
				.filter(displayName::contains)
				.collect(Collectors.joining("', '"));
		return matches.isEmpty()
				? enabled(reason(displayName, "doesn't contain any substring."))
				: disabled(reason(displayName, format("contains '%s'.", matches)));
		//@formatter:on
	}

	private static String reason(String displayName, String outcome) {
		return format("Display name '%s' %s", displayName, outcome);
	}

}
