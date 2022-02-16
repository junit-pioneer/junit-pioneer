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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Abstract entry-based extension")
@WritesEnvironmentVariable
@WritesSystemProperty
class AbstractEntryBasedExtensionTests {

	private static final String CLEAR_ENVVAR_KEY = "clear envvar";
	private static final String SET_ENVVAR_KEY = "set envvar";
	private static final String SET_ENVVAR_ORIGINAL_VALUE = "original envvar value";

	private static final String CLEAR_SYSPROP_KEY = "clear sysprop";
	private static final String SET_SYSPROP_KEY = "set sysprop";
	private static final String SET_SYSPROP_ORIGINAL_VALUE = "original sysprop value";

	@BeforeEach
	void setUp() {
		EnvironmentVariableUtils.clear(CLEAR_ENVVAR_KEY);
		EnvironmentVariableUtils.set(SET_ENVVAR_KEY, SET_ENVVAR_ORIGINAL_VALUE);

		System.clearProperty(CLEAR_SYSPROP_KEY);
		System.setProperty(SET_SYSPROP_KEY, SET_SYSPROP_ORIGINAL_VALUE);
	}

	@AfterEach
	void tearDown() {
		EnvironmentVariableUtils.clear(SET_ENVVAR_KEY);
		System.clearProperty(SET_SYSPROP_KEY);
	}

	@Test
	@Issue("432")
	@DisplayName("should not mix backups of different extensions on clear environment variable and clear system property")
	void shouldNotMixBackupsOfDifferentExtensionsOnClearEnvironmentVariableAndClearSystemProperty() {
		PioneerTestKit.executeTestMethod(MixBackupsTestCases.class, "clearEnvironmentVariableAndClearSystemProperty");

		assertThat(System.getenv(CLEAR_ENVVAR_KEY)).isNull();
		assertThat(System.getProperty(CLEAR_SYSPROP_KEY)).isNull();
	}

	@Test
	@Issue("432")
	@DisplayName("should not mix backups of different extensions on set environment variable and set system property")
	void shouldNotMixBackupsOfDifferentExtensionsOnSetEnvironmentVariableAndSetSystemProperty() {
		PioneerTestKit.executeTestMethod(MixBackupsTestCases.class, "setEnvironmentVariableAndSetSystemProperty");

		assertThat(System.getenv(SET_ENVVAR_KEY)).isEqualTo(SET_ENVVAR_ORIGINAL_VALUE);
		assertThat(System.getProperty(SET_SYSPROP_KEY)).isEqualTo(SET_SYSPROP_ORIGINAL_VALUE);
	}

	@Test
	@Issue("432")
	@DisplayName("should not mix backups of different extensions on clear environment variable and set system property")
	void shouldNotMixBackupsOfDifferentExtensionsOnClearEnvironmentVariableAndSetSystemProperty() {
		PioneerTestKit.executeTestMethod(MixBackupsTestCases.class, "clearEnvironmentVariableAndSetSystemProperty");

		assertThat(System.getenv(CLEAR_ENVVAR_KEY)).isNull();
		assertThat(System.getProperty(SET_SYSPROP_KEY)).isEqualTo(SET_SYSPROP_ORIGINAL_VALUE);
	}

	@Test
	@Issue("432")
	@DisplayName("should not mix backups of different extensions on set environment variable and clear system property")
	void shouldNotMixBackupsOfDifferentExtensionsOnSetEnvironmentVariableAndClearSystemProperty() {
		PioneerTestKit.executeTestMethod(MixBackupsTestCases.class, "setEnvironmentVariableAndClearSystemProperty");

		assertThat(System.getenv(SET_ENVVAR_KEY)).isEqualTo(SET_ENVVAR_ORIGINAL_VALUE);
		assertThat(System.getProperty(CLEAR_SYSPROP_KEY)).isNull();
	}

	static class MixBackupsTestCases {

		@Test
		@DisplayName("clear environment variable and clear system property")
		@ClearEnvironmentVariable(key = CLEAR_ENVVAR_KEY)
		@ClearSystemProperty(key = CLEAR_SYSPROP_KEY)
		void clearEnvironmentVariableAndClearSystemProperty() {
		}

		@Test
		@DisplayName("set environment variable and set system property")
		@SetEnvironmentVariable(key = SET_ENVVAR_KEY, value = "foo")
		@SetSystemProperty(key = SET_SYSPROP_KEY, value = "bar")
		void setEnvironmentVariableAndSetSystemProperty() {
		}

		@Test
		@DisplayName("clear environment variable and set system property")
		@ClearEnvironmentVariable(key = CLEAR_ENVVAR_KEY)
		@SetSystemProperty(key = SET_SYSPROP_KEY, value = "bar")
		void clearEnvironmentVariableAndSetSystemProperty() {
		}

		@Test
		@DisplayName("set environment variable and clear system property")
		@SetEnvironmentVariable(key = SET_ENVVAR_KEY, value = "foo")
		@ClearSystemProperty(key = SET_SYSPROP_KEY)
		void setEnvironmentVariableAndClearSystemProperty() {
		}

	}

}
