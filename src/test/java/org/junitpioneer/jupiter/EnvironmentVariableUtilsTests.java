/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JUnitPioneer system environment utilities")
class EnvironmentVariableUtilsTests {

	@Test
	void theEnvironmentIsNotCorruptedAfterSet() {
		EnvironmentVariableUtils.set("A_VARIABLE", "A value");

		// By using this method, the entire environment is read and copied from the field
		// ProcessEnvironment.theEnvironment. If that field is corrupted by a String having been stored
		// as key or value, this copy operation will fail with a ClassCastException.
		Map<String, String> environmentCopy = new HashMap<>(System.getenv());
		assertEquals("A value", environmentCopy.get("A_VARIABLE"));
	}

}
