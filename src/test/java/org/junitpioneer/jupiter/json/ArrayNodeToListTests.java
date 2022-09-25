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

import static org.junitpioneer.testkit.assertion.PioneerAssert.assertThat;

import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junitpioneer.jupiter.ReportEntry;
import org.junitpioneer.testkit.ExecutionResults;
import org.junitpioneer.testkit.PioneerTestKit;

@DisplayName("Converting JSON to lists")
public class ArrayNodeToListTests {

	private static final String COMPOSERS = "org/junitpioneer/jupiter/json/composers.json";
	private static final String POETS = "org/junitpioneer/jupiter/json/poets.json";
	private static final String BAD_POEMS = "org/junitpioneer/jupiter/json/bad_poems.json";

	@Test
	@DisplayName("can convert classpath source arrays to List")
	void classpathTest() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(ArrayNodeToListTests.class, "convertFromClasspath", String.class,
					List.class);

		assertThat(results).hasNumberOfSucceededTests(2);
		assertThat(results)
				.hasNumberOfReportEntries(2)
				.withValues("[Spartacus, Piano Concerto in D-flat major]",
					"[The Isle of the Dead, Morceaux de fantaisie]");
	}

	@Test
	@DisplayName("can convert annotation source arrays to list")
	void annotationTest() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(ArrayNodeToListTests.class, "convertFromAnnotation", List.class);

		assertThat(results).hasNumberOfSucceededTests(2);
		assertThat(results).hasNumberOfReportEntries(2).withValues("[1, 4, 7]", "[2, 4, 9]");
	}

	@Test
	@DisplayName("can convert using a specific List implementation")
	void specificImplementation() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(ArrayNodeToListTests.class, "convertWithExplicitListType",
					LinkedList.class);

		assertThat(results).hasNumberOfSucceededTests(2);
		assertThat(results).hasNumberOfReportEntries(2).withValues("[false, true, false]", "[true, false, true]");
	}

	@Test
	@DisplayName("can convert classpath source arrays to List with more complex objects")
	void classpathTestWithComplexObject() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(ArrayNodeToListTests.class, "convertToComplexObject", String.class,
					List.class);

		assertThat(results).hasNumberOfSucceededTests(2);
		assertThat(results)
				.hasNumberOfReportEntries(2)
				.withValues(
					"Edgar Allan Poe: [The Black Cat (1843), The Cask of Amontillado (1846), The Pit and the Pendulum (1842)]",
					"T. S. Eliot: [The Hollow Men (1925), Ash Wednesday (1930)]");
	}

	@Test
	@DisplayName("throws a ParameterResolutionException if it can not convert complex objects")
	void throwsForMalformedComplexObjects() {
		ExecutionResults results = PioneerTestKit
				.executeTestMethodWithParameterTypes(ArrayNodeToListTests.BadConfigurationTestCase.class,
					"conversionException", List.class);

		assertThat(results)
				.hasSingleFailedContainer()
				.withExceptionInstanceOf(UncheckedIOException.class)
				.hasMessageContaining("Failed to convert to type");
	}

	@ParameterizedTest
	@JsonClasspathSource(COMPOSERS)
	@ReportEntry("{1}")
	void convertFromClasspath(@Property("name") String name, @Property("music") List<String> works) {
		Assertions.assertThat(name).isNotEmpty();
		Assertions.assertThat(works).hasSize(2);
	}

	@ParameterizedTest
	@JsonSource({ "{ 'single': [1, 4, 7] }", "{ 'single': [2, 4, 9] }" })
	@ReportEntry("{0}")
	void convertFromAnnotation(@Property("single") List<Integer> numbers) {
		Assertions.assertThat(numbers).hasSize(3);
	}

	@ParameterizedTest
	@JsonSource({ "{ 'statements': [true, false, true] }", "{ 'statements': [false, true, false] }" })
	@ReportEntry("{0}")
	void convertWithExplicitListType(@Property("statements") LinkedList<Boolean> numbers) {
		Assertions.assertThat(numbers).hasSize(3);
	}

	@ParameterizedTest
	@JsonClasspathSource(POETS)
	@ReportEntry("{0}: {1}")
	void convertToComplexObject(@Property("name") String name, @Property("works") List<Poem> poems) {
		Predicate<Integer> predicate = name.equals("T. S. Eliot") ? release -> release < 1900
				: release -> release > 1900;
		Assertions.assertThat(poems).extracting(Poem::getRelease).noneMatch(predicate);
	}

	static class BadConfigurationTestCase {

		@ParameterizedTest
		@JsonClasspathSource(BAD_POEMS)
		void conversionException(@Property("poems") List<Poem> poems) {
		}

	}

	public static class Poem {

		private String title;
		private int release;

		public Poem() {
		}

		public int getRelease() {
			return release;
		}

		public void setRelease(int release) {
			this.release = release;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		@Override
		public String toString() {
			return title + " (" + release + ")";
		}

	}

}
