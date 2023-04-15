/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JacksonJsonConverterTests {

	@Test
	@DisplayName("throws exception if property is not none, list or all")
	void incorrectProperty() {
		Throwable thrown = catchThrowable(() -> new JacksonJsonConverter(new ObjectMapper(), "incorrect"));

		assertThat(thrown)
				.isInstanceOf(ExtensionConfigurationException.class)
				.hasMessageContaining("is not a valid value");
	}

	@ParameterizedTest
	@DisplayName("does not throw for all, none or list properties")
	@ValueSource(strings = { "all", "none", "list" })
	void property(String property) {
		assertDoesNotThrow(() -> new JacksonJsonConverter(new ObjectMapper(), property));
	}

}
