/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;

@DisplayName("The Pioneer property utils")
class PioneerPropertyUtilsTests {

	@Test
	@DisplayName("enforces non-blank and non-null property names for single properties")
	void throwsSingle() {
		Throwable forNull = catchThrowable(() -> PioneerPropertyUtils.property(null));
		Throwable forEmpty = catchThrowable(() -> PioneerPropertyUtils.property(""));
		Throwable forBlank = catchThrowable(() -> PioneerPropertyUtils.property("    "));

		assertThat(forNull)
				.isInstanceOf(PreconditionViolationException.class)
				.hasMessage("key must not be null or blank");
		assertThat(forEmpty)
				.isInstanceOf(PreconditionViolationException.class)
				.hasMessage("key must not be null or blank");
		assertThat(forBlank)
				.isInstanceOf(PreconditionViolationException.class)
				.hasMessage("key must not be null or blank");
	}

	@Test
	@DisplayName("enforces non-blank and non-null property names for list properties")
	void throwsList() {
		Throwable forNull = catchThrowable(() -> PioneerPropertyUtils.list(null));
		Throwable forEmpty = catchThrowable(() -> PioneerPropertyUtils.list(""));
		Throwable forBlank = catchThrowable(() -> PioneerPropertyUtils.list("    "));

		assertThat(forNull)
				.isInstanceOf(PreconditionViolationException.class)
				.hasMessage("key must not be null or blank");
		assertThat(forEmpty)
				.isInstanceOf(PreconditionViolationException.class)
				.hasMessage("key must not be null or blank");
		assertThat(forBlank)
				.isInstanceOf(PreconditionViolationException.class)
				.hasMessage("key must not be null or blank");
	}

	@Test
	@DisplayName("returns an empty optional for not present single properties")
	void getPropertiesEmpty() {
		Optional<String> property = PioneerPropertyUtils.property("empty");

		assertThat(property).isEmpty();
	}

	@Test
	@DisplayName("returns an empty list for not present list properties")
	void getPropertyListEmpty() {
		List<String> propertyList = PioneerPropertyUtils.list("empty");

		assertThat(propertyList).isEmpty();
	}

	@Test
	@DisplayName("gets a single property")
	void getSingleProperty() {
		Optional<String> property = PioneerPropertyUtils.property("single");

		assertThat(property).isNotEmpty().contains("All that is gold does not glitter");
	}

	@Test
	@DisplayName("gets a list property")
	void getListProperty() {
		List<String> propertyList = PioneerPropertyUtils.list("list");

		assertThat(propertyList)
				.isNotEmpty()
				.containsExactly("Not all those who wander are lost;", "The old that is strong does not wither",
					"Deep roots are not reached by the frost.");
	}

}
