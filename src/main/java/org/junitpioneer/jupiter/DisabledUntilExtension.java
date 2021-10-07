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

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junitpioneer.internal.PioneerAnnotationUtils.findClosestEnclosingAnnotation;
import static org.junitpioneer.internal.PioneerDateUtils.ISO_8601_DATE_FORMATTER;
import static org.junitpioneer.internal.PioneerDateUtils.TODAY;
import static org.junitpioneer.internal.PioneerDateUtils.isTodayOrInThePast;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junitpioneer.internal.PioneerDateUtils;

/**
 * This class implements the functionality for the {@code @DisabledUntil} annotation.
 *
 * @see DisabledUntil
 */
class DisabledUntilExtension implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return getUntilDateFromAnnotation(context)
				.map(untilDate -> evaluateUntilDate(context, untilDate))
				.orElse(enabled("No valid @DisabledUntil annotation found on element"));
	}

	private Optional<LocalDate> getUntilDateFromAnnotation(ExtensionContext context) {
		return findClosestEnclosingAnnotation(context, DisabledUntil.class)
				.map(DisabledUntil::untilDate)
				.flatMap(PioneerDateUtils::parseIso8601DateString);
	}

	private ConditionEvaluationResult evaluateUntilDate(ExtensionContext context, LocalDate untilDate) {
		if (isTodayOrInThePast(untilDate)) {
			final String message = format("untilDate [{0}] is equal to or before current date: [{1}]", untilDate,
				TODAY.format(ISO_8601_DATE_FORMATTER));
			context.publishReportEntry(DisabledUntilExtension.class.getSimpleName(), message);
			return enabled(message);
		}
		final String message = format("untilDate [{0}] is after current date [{1}]", untilDate,
			TODAY.format(ISO_8601_DATE_FORMATTER));
		return disabled(message);
	}

}
