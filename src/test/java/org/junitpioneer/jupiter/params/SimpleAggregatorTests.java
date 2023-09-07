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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junitpioneer.testkit.PioneerTestKit.executeTestMethodWithParameterTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.assertion.PioneerAssert;

class SimpleAggregatorTests {

	@ParameterizedTest
	@CsvSource({ "23, M, Bob", "24, F, Jane" })
	void testAggregator(@Aggregate Person person) {
		if (person.gender == Gender.M) {
			assertThat(person.age).isEqualTo(23);
			assertThat(person.name).isEqualTo("Bob");
		} else {
			assertThat(person.age).isEqualTo(24);
			assertThat(person.name).isEqualTo("Jane");
		}
	}

	@ParameterizedTest
	@ValueSource(ints = { 13, 17, 19 })
	void testBoxing(@Aggregate Boxed boxed) {
		assertThat(boxed.value).isLessThan(20);
	}

	@ParameterizedTest
	@CsvSource({ "John, 2023-07-16", "Bob, 1959-02-21", "Jane, 1977-05-03" })
	void testLocalDate(@Aggregate Human human) {
		assertThat(human.name).isNotBlank();
		assertThat(human.birthday).isIn("2023-07-16", "1959-02-21", "1977-05-03");
	}

	@ParameterizedTest
	@CsvSource({ "Speeding, 19:03:12", "Ran red light, 18:34:02", "Rolling stop, 07:12:12" })
	void testLocalTime(@Aggregate Ticket ticket) {
		assertThat(ticket.description).isNotBlank();
		assertThat(ticket.time).isIn("19:03:12", "18:34:02", "07:12:12");
	}

	@ParameterizedTest
	@CsvSource({ "Fuzzy, 2010-10-10T12:20:09", "Unsure, 2022-12-11T07:12:15" })
	void testLocalDateTime(@Aggregate Memory memory) {
		assertThat(memory.description).isIn("Fuzzy", "Unsure");
		assertThat(memory.occurred).isIn("2010-10-10T12:20:09", "2022-12-11T07:12:15");
	}

	@Test
	void noMatchingConstructorSize() {
		ExecutionResults results = executeTestMethodWithParameterTypes(BadConfigurationTestCases.class,
			"noMatchingConstructorSize", Person.class);

		PioneerAssert
				.assertThat(results)
				.hasNumberOfFailedTests(2)
				.andThenCheckExceptions(exceptions -> assertThat(exceptions)
						.allSatisfy(exception -> assertThat(exception)
								.hasCauseInstanceOf(ArgumentsAggregationException.class)
								.hasMessageContaining(
									"Could not aggregate arguments, no public constructor with %d parameters was found",
									1)));
	}

	@Test
	void noMatchingConstructor() {
		ExecutionResults results = executeTestMethodWithParameterTypes(BadConfigurationTestCases.class,
			"noMatchingConstructor", BadConfigurationTestCases.Verse.class);

		PioneerAssert
				.assertThat(results)
				.hasNumberOfFailedTests(2)
				.andThenCheckExceptions(exceptions -> assertThat(exceptions)
						.allSatisfy(exception -> assertThat(exception)
								.hasCauseInstanceOf(ArgumentsAggregationException.class)
								.hasMessageContaining(
									"Could not aggregate arguments, no matching public constructor was found.")));
	}

	@Test
	void tooManyMatchingConstructors() {
		ExecutionResults results = executeTestMethodWithParameterTypes(BadConfigurationTestCases.class,
			"tooManyMatchingConstructors", BadConfigurationTestCases.Verse.class);

		PioneerAssert
				.assertThat(results)
				.hasNumberOfFailedTests(2)
				.andThenCheckExceptions(exceptions -> assertThat(exceptions)
						.allSatisfy(exception -> assertThat(exception)
								.hasCauseInstanceOf(ArgumentsAggregationException.class)
								.hasMessageContaining("Expected only one matching public constructor")));
	}

	static class BadConfigurationTestCases {

		@ParameterizedTest
		@CsvSource({ "What is more gentle than a wind in summer?", "What is more soothing than the pretty hummer" })
		void noMatchingConstructorSize(@Aggregate Person person) {
		}

		@ParameterizedTest
		@CsvSource({ "That stays one moment in an open flower, And buzzes cheerily from bower to bower?",
				"What is more tranquil than a musk-rose blowing, In a green island far from all men's knowing?" })
		void noMatchingConstructor(@Aggregate Verse verse) {
		}

		@ParameterizedTest
		@CsvSource({ "More healthful than the leafiness of dales?, 1", "More secret than a nest of nightingales?, 2" })
		void tooManyMatchingConstructors(@Aggregate Verse verse) {
		}

		static class Verse {

			private final String verse;
			private final int line;

			public Verse(String verse, int line) {
				this.verse = verse;
				this.line = line;
			}

			public Verse(String verse, Integer line) {
				this.verse = verse;
				this.line = line;
			}

		}

	}

	enum Gender {
		M, F, X
	}

	static class Person {

		private final int age;
		private final Gender gender;
		private final String name;

		public Person(int age, Gender gender, String name) {
			this.age = age;
			this.gender = gender;
			this.name = name;
		}

	}

	static class Boxed {

		private final int value;

		public Boxed(int value) {
			this.value = value;
		}

	}

	static class Human {

		private final String name;
		private final LocalDate birthday;

		public Human(String name, LocalDate birthday) {
			this.name = name;
			this.birthday = birthday;
		}

	}

	static class Ticket {

		private final String description;
		private final LocalTime time;

		public Ticket(String description, LocalTime time) {
			this.description = description;
			this.time = time;
		}

	}

	static class Memory {

		private final String description;
		private final LocalDateTime occurred;

		public Memory(String description, LocalDateTime occurred) {
			this.description = description;
			this.occurred = occurred;
		}

	}

}
