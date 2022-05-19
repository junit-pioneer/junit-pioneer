/*
 * Copyright 2016-2022 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

@EnabledForJreRange(max = JRE.JAVA_16, disabledReason = "See: https://github.com/junit-pioneer/junit-pioneer/issues/509")
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
		Map<String, String> environmentCopy = new HashMap<>(System.getenv());
		assertThat(environmentCopy.get("TEST")).isEqualTo("test");
	}

	/*
	 * The documentation mentions that without proper permissions an enabled security manager will not
	 * give access to the internals we need to change environment variables. These tests confirm that.
	 */
	@Nested
	// classes related to `SecurityManager` will eventually be removed and so will these tests be
	@SuppressWarnings("removal")
	class With_SecurityManager {

		@Test
		@SetSystemProperty(key = "java.security.policy", value = "file:src/test/resources/default-testing.policy")
		@SuppressWarnings("deprecated")
		void shouldThrowAccessControlExceptionWithDefaultSecurityManager() {
			//@formatter:off
			executeWithSecurityManager(
					() -> assertThatThrownBy(() -> EnvironmentVariableUtils.set("TEST", "test"))
							.isInstanceOf(java.security.AccessControlException.class));
			//@formatter:on
		}

		@Test
		@SetSystemProperty(key = "java.security.policy", value = "file:src/test/resources/reflect-permission-testing.policy")
		void shouldModifyEnvironmentVariableIfPermissionIsGiven() {
			executeWithSecurityManager(() -> {
				assertThatCode(() -> EnvironmentVariableUtils.set("TEST", "test")).doesNotThrowAnyException();
				assertThat(System.getenv("TEST")).isEqualTo("test");
			});
		}

		/*
		 * This needs to be done during the execution of the test method and cannot be moved to setup/tear down
		 * because junit uses reflection and the default SecurityManager prevents that.
		 */
		@SuppressWarnings("deprecated")
		private void executeWithSecurityManager(Runnable runnable) {
			java.security.Policy.getPolicy().refresh();
			SecurityManager original = System.getSecurityManager();
			System.setSecurityManager(new SecurityManager());
			try {
				runnable.run();
			}
			finally {
				System.setSecurityManager(original);
			}
		}

	}

}
