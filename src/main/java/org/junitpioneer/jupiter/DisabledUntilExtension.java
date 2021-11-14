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

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junitpioneer.internal.PioneerAnnotationUtils.findClosestEnclosingAnnotation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * This class implements the functionality for the {@code @DisabledUntil} annotation.
 *
 * @see DisabledUntil
 */
class DisabledUntilExtension implements ExecutionCondition {

	private static final DateTimeFormatter ISO_8601 = DateTimeFormatter.ISO_DATE;

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return getUntilDateFromAnnotation(context)
				.map(untilDate -> evaluateUntilDate(context, untilDate))
				.orElse(enabled("No @DisabledUntil annotation found on element"));
	}

	private Optional<LocalDate> getUntilDateFromAnnotation(ExtensionContext context) {
		return findClosestEnclosingAnnotation(context, DisabledUntil.class)
				.map(DisabledUntil::date)
				.map(this::parseDate);
	}

	private LocalDate parseDate(String dateString) {
		try {
			return LocalDate.parse(dateString, ISO_8601);
		}
		catch (DateTimeParseException ex) {
			throw new ExtensionConfigurationException(
				"The `untilDate` string '" + dateString + "' is no valid ISO-8601 string.", ex);
		}
	}

	private ConditionEvaluationResult evaluateUntilDate(ExtensionContext context, LocalDate untilDate) {
		LocalDate today = LocalDate.now();
		boolean disabled = today.isBefore(untilDate);

		if (disabled) {
			String message = format("The `date` %s is after the current date %s", untilDate.format(ISO_8601),
				today.format(ISO_8601));
			return disabled(message);
		} else {
			String message = format(
				"The `date` %s is before or on the current date %s, so `@DisabledUntil` no longer disabled test \"%s\". Please remove the annotation.",
				untilDate.format(ISO_8601), today.format(ISO_8601), context.getUniqueId());
			context.publishReportEntry(DisabledUntilExtension.class.getSimpleName(), message);
			return enabled(message);
		}
	}

}
