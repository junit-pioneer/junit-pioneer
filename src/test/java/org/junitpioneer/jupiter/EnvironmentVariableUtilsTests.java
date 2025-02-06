/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JUnitPioneer system environment utilities")
@WritesEnvironmentVariable
class EnvironmentVariableUtilsTests {

	/*
	 * These tests use classes from the `java.security` package that were deprecated in Java 17.
	 * They need to be updated once the classes are removed. Until then, we reference them by
	 * their fully-qualified name, so we can suppress deprecation warnings on specific methods.
	 */

	@AfterEach
	void removeTestEnvVar() {
		EnvironmentVariableUtils.clear("TEST");
	}

	@Test
	void theEnvironmentIsNotCorruptedAfterSet() {
		EnvironmentVariableUtils.set("TEST", "test");

		/* By using this method, the entire environment is read and copied from the field
		   ProcessEnvironment.theEnvironment. If that field is corrupted by a String having been stored
		   as key or value, this copy operation will fail with a ClassCastException. */
		Map<String, String> environmentCopy = Map.copyOf(System.getenv());
		assertThat(environmentCopy.get("TEST")).isEqualTo("test");
	}

}
