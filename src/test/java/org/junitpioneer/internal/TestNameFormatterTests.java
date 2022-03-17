/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;

@DisplayName("The generic internal-only DisplayName formatter")
public class TestNameFormatterTests {

	@Test
	@DisplayName("throws an exception for not properly closed parameters")
	void propagatesException() {
		TestNameFormatter formatter = new TestNameFormatter("{index", "", TestNameFormatter.class);

		assertThatThrownBy(() -> formatter.format(1))
				.isInstanceOf(ExtensionConfigurationException.class)
				.hasCauseExactlyInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(
					"The display name pattern defined for the " + TestNameFormatter.class.getName() + " is invalid");
	}

	@Test
	@DisplayName("replaces {index} with the invocation index")
	void replacesIndex() {
		TestNameFormatter formatter = new TestNameFormatter("Index is {index}", "", TestNameFormatter.class);

		assertThat(formatter.format(3)).isEqualTo("Index is 3");
	}

	@Test
	@DisplayName("replaces {displayName} with the given display name")
	void replacesDisplayName() {
		TestNameFormatter formatter = new TestNameFormatter("Name is {displayName}", "Bond. James Bond.",
			TestNameFormatter.class);

		assertThat(formatter.format(3)).isEqualTo("Name is Bond. James Bond.");
	}

	@Test
	@DisplayName("replaces {arguments} with comma-separated list of arguments")
	void replacesArguments() {
		TestNameFormatter formatter = new TestNameFormatter("Arguments are {arguments}", "", TestNameFormatter.class);

		assertThat(formatter.format(0, Arrays.asList(Boolean.class, new int[] { 1, 2, 3 }, "enigma").toArray()))
				.isEqualTo("Arguments are class java.lang.Boolean, [1, 2, 3], enigma");
	}

	@Test
	@DisplayName("replaces indexed arguments with the corresponding argument")
	void replacesIndexedArguments() {
		TestNameFormatter formatter = new TestNameFormatter("Second {1} and before that {0}", "",
			TestNameFormatter.class);

		assertThat(formatter.format(0, Arrays.asList(Boolean.class, new int[] { 1, 2, 3 }, "enigma").toArray()))
				.isEqualTo("Second [1, 2, 3] and before that class java.lang.Boolean");
	}

	@Test
	@DisplayName("does nothing with over-indexed arguments")
	void overIndexedArguments() {
		TestNameFormatter formatter = new TestNameFormatter("Second {6} and before that {0}", "",
			TestNameFormatter.class);

		assertThat(formatter.format(0, Arrays.asList(Boolean.class, new int[] { 1, 2, 3 }, "enigma").toArray()))
				.isEqualTo("Second {6} and before that class java.lang.Boolean");
	}

	@Test
	@DisplayName("throws exception for negative indexed arguments")
	void negativeIndexedArguments() {
		TestNameFormatter formatter = new TestNameFormatter("Second {-1} and before that {0}", "",
			TestNameFormatter.class);

		assertThatThrownBy(
			() -> formatter.format(0, Arrays.asList(Boolean.class, new int[] { 1, 2, 3 }, "enigma").toArray()))
					.isInstanceOf(ExtensionConfigurationException.class)
					.hasCauseExactlyInstanceOf(IllegalArgumentException.class)
					.hasMessageContaining("The display name pattern defined for the "
							+ TestNameFormatter.class.getName() + " is invalid.");
	}

}
