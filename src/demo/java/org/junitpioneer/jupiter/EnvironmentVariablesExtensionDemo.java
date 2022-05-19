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

import org.junit.jupiter.api.Test;

public class EnvironmentVariablesExtensionDemo {

	// tag::environment_clear_simple[]
	@Test
	@ClearEnvironmentVariable(key = "some variable")
	void testClear() {
		assertThat(System.getenv("some variable")).isNull();
	}
	// end::environment_clear_simple[]

	// tag::environment_set_simple[]
	@Test
	@SetEnvironmentVariable(key = "some variable", value = "new value")
	void testSet() {
		assertThat(System.getenv("some variable")).isEqualTo("new value");
	}
	// end::environment_set_simple[]

	// tag::environment_using_set_and_clear[]
	@Test
	@ClearEnvironmentVariable(key = "1st variable")
	@ClearEnvironmentVariable(key = "2nd variable")
	@SetEnvironmentVariable(key = "3rd variable", value = "new value")
	void testClearAndSet() {
		assertThat(System.getenv("1st variable")).isNull();
		assertThat(System.getenv("2nd variable")).isNull();
		assertThat(System.getenv("3rd variable")).isEqualTo("new value");
	}
	// end::environment_using_set_and_clear[]

	// tag::environment_using_at_class_level[]
	@ClearEnvironmentVariable(key = "some variable")
	class MyEnvironmentVariableTest {

		@Test
		@SetEnvironmentVariable(key = "some variable", value = "new value")
		void clearedAtClasslevel() {
			assertThat(System.getenv("some variable")).isEqualTo("new value");
		}

	}
	// end::environment_using_at_class_level[]

}
