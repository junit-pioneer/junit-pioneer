/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.TimeZone;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TimeZone extension")
class DefaultTimeZoneTests {

	private static TimeZone TEST_DEFAULT_TIMEZONE;
	private static TimeZone DEFAULT_TIMEZONE_BEFORE_TEST;

	@BeforeAll
	static void globalSetUp() {
		DEFAULT_TIMEZONE_BEFORE_TEST = TimeZone.getDefault();
		final TimeZone utc = TimeZone.getTimeZone("UTC");
		if (!DEFAULT_TIMEZONE_BEFORE_TEST.equals(utc)) {
			TimeZone.setDefault(utc);
		}
		else {
			TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		}
		TEST_DEFAULT_TIMEZONE = TimeZone.getDefault();
	}

	@Test
	@DisplayName("does nothing when annotation is not present")
	void doesNothingWhenAnnotationNotPresent() {
		assertEquals(TEST_DEFAULT_TIMEZONE, TimeZone.getDefault());
	}

	@DefaultTimeZone("CET")
	@Test
	@DisplayName("sets the default time zone using an abbreviation")
	void setsTimeZoneFromAbbreviation() {
		assertEquals(TimeZone.getTimeZone("CET"), TimeZone.getDefault());
	}

	@DefaultTimeZone("America/Los_Angeles")
	@Test
	@DisplayName("sets the default time zone using a full name")
	void setsTimeZoneFromFullName() {
		assertEquals(TimeZone.getTimeZone("America/Los_Angeles"), TimeZone.getDefault());
	}

	@AfterAll
	static void globalTearDown() {
		TimeZone.setDefault(DEFAULT_TIMEZONE_BEFORE_TEST);
	}
}
