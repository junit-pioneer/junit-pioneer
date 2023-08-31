/*
 * Copyright 2016-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.params;

import java.time.LocalDate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SimpleAggregatorDemo {

	// tag::basic_example[]
	@ParameterizedTest
	@CsvSource({ "Jane, Doe, F, 1990-05-20", "John, Doe, M, 1990-10-22" })
	void test(@Aggregate Person person) {
	}
	// end::basic_example[]

	static class Person {

		// tag::person_class[]
		private final String firstName;
		private final String lastName;
		private final Gender gender;
		private final LocalDate birthday;
		// end::person_class[]

		public Person(String firstName, String lastName, Gender gender, LocalDate birthday) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.gender = gender;
			this.birthday = birthday;
		}

	}

	enum Gender {
		F, M, X
	}

}
