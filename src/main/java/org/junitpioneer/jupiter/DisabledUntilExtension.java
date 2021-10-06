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

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

class DisabledUntilExtension implements ExecutionCondition {

    private static final LocalDate TODAY = LocalDate.now();
    public static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        return getUntilDateFromAnnotation(context)
            .map(untilDate -> evaluateUntilDate(context, untilDate))
            .orElse(ConditionEvaluationResult.enabled("No valid @DisabledUntil annotation found on element"));
    }

    private Optional<LocalDate> getUntilDateFromAnnotation(ExtensionContext context) {
        return findAnnotation(context.getElement(), DisabledUntil.class)
            .map(DisabledUntil::untilDate)
            .flatMap(DisabledUntilExtension::parseIso8601DateString);
    }

    private ConditionEvaluationResult evaluateUntilDate(ExtensionContext context, LocalDate untilDate) {
        if (isTodayOrInThePast(untilDate)) {
            final String message = MessageFormat.format("untilDate [{0}] is equal to or before current date: [{1}]", untilDate, TODAY.format(ISO_DATE_FORMATTER));
            context.publishReportEntry(DisabledUntilExtension.class.getSimpleName(), message);
            return ConditionEvaluationResult.enabled(message);
        }
        final String message = MessageFormat.format("untilDate [{0}] is after current date [{1}]", untilDate, TODAY.format(ISO_DATE_FORMATTER));
        return ConditionEvaluationResult.disabled(message);
    }

    protected static boolean isTodayOrInThePast(LocalDate untilDate) {
        return untilDate.isEqual(TODAY) || untilDate.isBefore(TODAY);
    }

    private static Optional<LocalDate> parseIso8601DateString(String isoDateString) {
        try {
            return Optional.of(LocalDate.parse(isoDateString, ISO_DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}