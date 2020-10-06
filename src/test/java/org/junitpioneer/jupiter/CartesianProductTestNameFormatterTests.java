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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;

@DisplayName("The DisplayName formatter for CartesianProductTest")
public class CartesianProductTestNameFormatterTests {

	@Test
	@DisplayName("throws an exception for not properly closed parameters")
	void propagatesException() {
		CartesianProductTestNameFormatter formatter = new CartesianProductTestNameFormatter("{index", "");

		assertThatThrownBy(() -> formatter.format(1))
				.isInstanceOf(ExtensionConfigurationException.class)
				.hasCauseExactlyInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("The display name pattern defined for the CartesianProductTest is invalid");
	}

	@Test
	@DisplayName("replaces {index} with the invocation index")
	void replacesIndex() {
		CartesianProductTestNameFormatter formatter = new CartesianProductTestNameFormatter("Index is {index}", "");

		assertThat(formatter.format(3)).isEqualTo("Index is 3");
	}

	@Test
	@DisplayName("replaces {displayName} with the given display name")
	void replacesDisplayName() {
		CartesianProductTestNameFormatter formatter = new CartesianProductTestNameFormatter("Name is {displayName}",
			"Bond. James Bond.");

		assertThat(formatter.format(3)).isEqualTo("Name is Bond. James Bond.");
	}

	@Test
	@DisplayName("replaces {arguments} with comma-separated list of arguments")
	void replacesArguments() {
		CartesianProductTestNameFormatter formatter = new CartesianProductTestNameFormatter("Arguments are {arguments}",
			"");

		assertThat(formatter.format(0, Arrays.asList(Boolean.class, new int[] { 1, 2, 3 }, "enigma").toArray()))
				.isEqualTo("Arguments are class java.lang.Boolean, [1, 2, 3], enigma");
	}

	@Test
	@DisplayName("replaces indexed arguments with the corresponding argument")
	void replacesIndexedArguments() {
		CartesianProductTestNameFormatter formatter = new CartesianProductTestNameFormatter(
			"Second {1} and before that {0}", "");

		assertThat(formatter.format(0, Arrays.asList(Boolean.class, new int[] { 1, 2, 3 }, "enigma").toArray()))
				.isEqualTo("Second [1, 2, 3] and before that class java.lang.Boolean");
	}

	@Test
	@DisplayName("does nothing with over-indexed arguments")
	void overIndexedArguments() {
		CartesianProductTestNameFormatter formatter = new CartesianProductTestNameFormatter(
			"Second {6} and before that {0}", "");

		assertThat(formatter.format(0, Arrays.asList(Boolean.class, new int[] { 1, 2, 3 }, "enigma").toArray()))
				.isEqualTo("Second {6} and before that class java.lang.Boolean");
	}

	@Test
	@DisplayName("throws exception for negative indexed arguments")
	void negativeIndexedArguments() {
		CartesianProductTestNameFormatter formatter = new CartesianProductTestNameFormatter(
			"Second {-1} and before that {0}", "");

		assertThatThrownBy(
			() -> formatter.format(0, Arrays.asList(Boolean.class, new int[] { 1, 2, 3 }, "enigma").toArray()))
					.isInstanceOf(ExtensionConfigurationException.class)
					.hasCauseExactlyInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("The display name pattern defined for the CartesianProductTest is invalid.");
	}

}
