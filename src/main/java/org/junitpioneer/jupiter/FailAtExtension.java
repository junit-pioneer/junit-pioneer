/*
 * Copyright 2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.lang.String.format;
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
import org.opentest4j.AssertionFailedError;

/**
 * This class implements the functionality for the {@code @FailAt} annotation.
 *
 * @see FailAt
 */
class FailAtExtension implements ExecutionCondition {

	private static final DateTimeFormatter ISO_8601 = DateTimeFormatter.ISO_DATE;

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return getFailAtDateFromAnnotation(context)
				.map(failAtDate -> evaluateFailAtDate(context, failAtDate))
				.orElse(enabled("No @FailAt annotation found on element."));
	}

	private Optional<LocalDate> getFailAtDateFromAnnotation(ExtensionContext context) {
		return findClosestEnclosingAnnotation(context, FailAt.class).map(FailAt::date).map(this::parseDate);
	}

	private LocalDate parseDate(String dateString) {
		try {
			return LocalDate.parse(dateString, ISO_8601);
		}
		catch (DateTimeParseException ex) {
			throw new ExtensionConfigurationException(
				"The `failAtDate` string '" + dateString + "' is not a valid ISO 8601 string.", ex);
		}
	}

	private ConditionEvaluationResult evaluateFailAtDate(ExtensionContext context, LocalDate failAtDate) {
		LocalDate today = LocalDate.now();
		boolean isBefore = today.isBefore(failAtDate);

		String failAtDateString = failAtDate.format(ISO_8601);
		String todayDateString = today.format(ISO_8601);

		if (isBefore) {
			String reportEntry = format(
				"The `date` %s is after the current date %s, so `@FailAt` did not fail the test \"%s\". It will do so when the date is reached.",
				failAtDateString, todayDateString, context.getUniqueId());
			context.publishReportEntry("FailAt", reportEntry);
			return enabled(reportEntry);
		} else {
			String reportEntry = format(
				"The current date %s is after or on the `date` %s, so `@FailAt` fails the test \"%s\". Please remove the annotation.",
				failAtDateString, todayDateString, context.getUniqueId());
			context.publishReportEntry(FailAtExtension.class.getSimpleName(), reportEntry);

			String message = format("The current date %s is after or on the `date` %s", todayDateString,
				failAtDateString);

			throw new AssertionFailedError(message);
		}
	}

}
