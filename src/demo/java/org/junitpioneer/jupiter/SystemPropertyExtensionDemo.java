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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SystemPropertyExtensionDemo {

	// tag::systemproperty_clear_simple[]
	@Test
	@ClearSystemProperty(key = "some property")
	void testClearingProperty() {
		assertThat(System.getProperty("some property")).isNull();
	}
	// end::systemproperty_clear_simple[]

	// tag::systemproperty_set_simple[]
	@Test
	@SetSystemProperty(key = "some property", value = "new value")
	void testSettingProperty() {
		assertThat(System.getProperty("some property")).isEqualTo("new value");
	}
	// end::systemproperty_set_simple[]

	// tag::systemproperty_using_set_and_clear[]
	@Test
	@ClearSystemProperty(key = "1st property")
	@ClearSystemProperty(key = "2nd property")
	@SetSystemProperty(key = "3rd property", value = "new value")
	void testClearingAndSettingProperty() {
		assertThat(System.getProperty("1st property")).isNull();
		assertThat(System.getProperty("2nd property")).isNull();
		assertThat(System.getProperty("3rd property")).isEqualTo("new value");
	}
	// end::systemproperty_using_set_and_clear[]

	// tag::systemproperty_using_at_class_level[]
	@ClearSystemProperty(key = "some property")
	class MySystemPropertyTest {

		@Test
		@SetSystemProperty(key = "some property", value = "new value")
		void clearedAtClasslevel() {
			assertThat(System.getProperty("some property")).isEqualTo("new value");
		}

	}
	// end::systemproperty_using_at_class_level[]

	// tag::systemproperty_parameter[]
	@ParameterizedTest
	@ValueSource(strings = { "foo", "bar" })
	@ClearSystemProperty(key = "some property")
	void parameterizedTest(String value) {
		System.setProperty("some property", value);
	}
	// end::systemproperty_parameter[]

}
