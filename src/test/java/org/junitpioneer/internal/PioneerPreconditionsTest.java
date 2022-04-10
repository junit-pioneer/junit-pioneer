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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;

@DisplayName("Pioneer precondition utilities")
class PioneerPreconditionsTest {

	@Nested
	@DisplayName("not blank")
	class NotBlankTests {

		@Test
		@DisplayName("should throw violation exception if string is null")
		void nullInput() {
			assertThatThrownBy(() -> PioneerPreconditions.notBlank(null, "Value must not be null"))
					.isInstanceOf(PreconditionViolationException.class)
					.hasMessage("Value must not be null");
		}

		@Test
		@DisplayName("should throw violation exception if string is empty")
		void emptyInput() {
			assertThatThrownBy(() -> PioneerPreconditions.notBlank("", "Value must be provided"))
					.isInstanceOf(PreconditionViolationException.class)
					.hasMessage("Value must be provided");
		}

		@Test
		@DisplayName("should throw violation exception if string is blank")
		void blankInput() {
			assertThatThrownBy(() -> PioneerPreconditions.notBlank("     ", "Value must be provided"))
					.isInstanceOf(PreconditionViolationException.class)
					.hasMessage("Value must be provided");
		}

		@Test
		@DisplayName("should return string if it is not blank")
		void validInput() {
			assertThat(PioneerPreconditions.notBlank("testValue", "Value must be provided")).isEqualTo("testValue");
		}

	}

	@Nested
	@DisplayName("not null")
	class NotNullTests {

		@Test
		@DisplayName("should throw violation exception if object is null")
		void nullInput() {
			assertThatThrownBy(() -> PioneerPreconditions.notNull(null, "Input must not be null"))
					.isInstanceOf(PreconditionViolationException.class)
					.hasMessage("Input must not be null");
		}

		@Test
		@DisplayName("should return object if it is not null")
		void validInput() {
			assertThat(PioneerPreconditions.notNull("testValue", "Value must be provided")).isEqualTo("testValue");
		}

	}

	@Nested
	@DisplayName("not empty")
	class NotEmptyTests {

		@Test
		@DisplayName("should throw violation exception if collection is null")
		void nullCollectionInput() {
			assertThatThrownBy(() -> PioneerPreconditions.notEmpty(null, "Value must not be null"))
					.isInstanceOf(PreconditionViolationException.class)
					.hasMessage("Value must not be null");
		}

		@Test
		@DisplayName("should throw violation exception if collection is empty")
		void emptyInput() {
			assertThatThrownBy(() -> PioneerPreconditions.notEmpty(Collections.emptySet(), "Value must be provided"))
					.isInstanceOf(PreconditionViolationException.class)
					.hasMessage("Value must be provided");
		}

		@Test
		@DisplayName("should return collection if it is not empty")
		void validInput() {
			List<String> collection = new ArrayList<>();
			collection.add("testValue");
			assertThat(PioneerPreconditions.notEmpty(collection, "Value must be provided")).isSameAs(collection);
		}

	}

	@Nested
	@DisplayName("not empty with message")
	class NotEmptyWithMessgeTests {

		@Test
		@DisplayName("should throw violation exception if collection is null")
		void nullCollectionInput() {
			assertThatThrownBy(() -> PioneerPreconditions.notEmpty(null, "Collection must not be null"))
					.isInstanceOf(PreconditionViolationException.class)
					.hasMessage("Collection must not be null");
		}

		@Test
		@DisplayName("should throw violation exception if collection is empty")
		void emptyInput() {
			assertThatThrownBy(
				() -> PioneerPreconditions.notEmpty(Collections.emptySet(), "Collection must be provided"))
						.isInstanceOf(PreconditionViolationException.class)
						.hasMessage("Collection must be provided");
		}

		@Test
		@DisplayName("should return collection if it is not empty")
		void validInput() {
			List<String> collection = new ArrayList<>();
			collection.add("testValue");
			assertThat(PioneerPreconditions.notEmpty(collection, "Collection must be provided")).isSameAs(collection);
		}

	}

}
