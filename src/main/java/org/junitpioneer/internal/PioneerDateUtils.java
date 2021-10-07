/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * Pioneer-internal utility class to handle dates.
 * DO NOT USE THIS CLASS - IT MAY CHANGE SIGNIFICANTLY IN ANY MINOR UPDATE.
 */
public class PioneerDateUtils {

    public static final LocalDate TODAY = LocalDate.now();
    public static final DateTimeFormatter ISO_8601_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private PioneerDateUtils() {
        // private constructor to prevent instantiation of utility class
    }

    /**
     * Returns true if the given {@link LocalDate} is today or in the past, false otherwise.
     */
    public static boolean isTodayOrInThePast(LocalDate dateToCompare) {
        return dateToCompare.isEqual(TODAY) || dateToCompare.isBefore(TODAY);
    }

    /**
     * Returns an {@link Optional} containing a {@link LocalDate} parsed from a given ISO 8601 date string, or an empty
     * {@link Optional} if the string couldn't be parsed.
     */
    public static Optional<LocalDate> parseIso8601DateString(String isoDateString) {
        try {
            return Optional.of(LocalDate.parse(isoDateString, ISO_8601_DATE_FORMATTER));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
