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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Abstract entry-based extension")
class AbstractEntryBasedExtensionTest {

	private static final String ENVIRONMENT_VARIABLE_KEY = "environment variable A";
	private static final String ENVIRONMENT_VARIABLE_VALUE = "old A";
	private static final String SYSTEM_PROPERTY_KEY = "system property B";
	private static final String SYSTEM_PROPERTY_VALUE = "old B";

	@BeforeAll
	static void globalSetUp() {
		EnvironmentVariableUtils.set(ENVIRONMENT_VARIABLE_KEY, ENVIRONMENT_VARIABLE_VALUE);
		System.setProperty(SYSTEM_PROPERTY_KEY, SYSTEM_PROPERTY_VALUE);
	}

	@Test
	@DisplayName("should not mix backups of different extensions")
	void shouldNotMixBackupsOfDifferentExtensions() {
		PioneerTestKit.executeTestMethod(MixBackupsTestCases.class, "settingEnvironmentVariableAndSystemProperty");

		assertThat(System.getenv(ENVIRONMENT_VARIABLE_KEY)).isEqualTo(ENVIRONMENT_VARIABLE_VALUE);
		assertThat(System.getProperty(SYSTEM_PROPERTY_KEY)).isEqualTo(SYSTEM_PROPERTY_VALUE);
	}

	static class MixBackupsTestCases {

		@Test
		@DisplayName("setting environment variable and system property")
		@SetEnvironmentVariable(key = ENVIRONMENT_VARIABLE_KEY, value = "new A")
		@SetSystemProperty(key = SYSTEM_PROPERTY_KEY, value = "new B")
		void settingEnvironmentVariableAndSystemProperty() {
		}

	}

}
