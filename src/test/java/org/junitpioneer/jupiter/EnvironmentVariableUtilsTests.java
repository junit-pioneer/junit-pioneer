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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.AccessControlException;
import java.security.Policy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JUnitPioneer system environment utilities")
@ClearEnvironmentVariable(key = "TEST")
class EnvironmentVariableUtilsTests {


	@Test
	@SetSystemProperty(key = "java.security.policy", value = "file:src/test/resources/default-testing.policy")
	void shouldThrowAccessControlExceptionWithDefaultSecurityManager() {
		executeWithSecurityManager(() -> assertThatThrownBy(() -> EnvironmentVariableUtils.set("TEST", "TEST"))
				.isInstanceOf(AccessControlException.class));
	}

	@Test
	@SetSystemProperty(key = "java.security.policy", value = "file:src/test/resources/reflect-permission-testing.policy")
	void shouldModifyEnvironmentVariableIfPermissionIsGiven() {
		executeWithSecurityManager(() -> {
			assertThatCode(() -> EnvironmentVariableUtils.set("TEST", "TEST")).doesNotThrowAnyException();
			assertThat(System.getenv("TEST")).isEqualTo("TEST");
		});
	}

	/*
	 * This needs to be done during the execution of the test method and cannot be moved to setup/tear down
	 * because junit uses reflection and the default SecurityManager prevents that.
	 */
	private void executeWithSecurityManager(Runnable runnable) {
		Policy.getPolicy().refresh();
		SecurityManager original = System.getSecurityManager();
		System.setSecurityManager(new SecurityManager());
		try {
			runnable.run();
		}
		finally {
			System.setSecurityManager(original);
		}


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
