/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;

class JsonArgumentSourceExtensionDemo {

	@Nested
	class ClasspathDemo {

		// tag::classpath_source[]
		@ParameterizedTest
		@JsonClasspathSource("jedis.json")
		void singleJedi(Jedi jedi) {
			// YOUR TEST CODE HERE
		}
		// end::classpath_source[]

		// @formatter:off
		// tag::classpath_source_with_property[]
		@ParameterizedTest
		@JsonClasspathSource("jedis.json")
		void singleJediProperty(
				@Property("name") String jediName) {
			// YOUR TEST CODE HERE
		}
		// end::classpath_source_with_property[]
		// @formatter:on

		// @formatter:off
		// tag::classpath_source_deconstruct_from_array[]
		@ParameterizedTest
		@JsonClasspathSource("jedis.json")
		void deconstructFromArray(
				@Property("name") String name,
				@Property("height") int height) {
			// YOUR TEST CODE HERE
		}
		// end::classpath_source_deconstruct_from_array[]
		// @formatter:on

		// @formatter:off
		// tag::classpath_source_nested_data[]
		@ParameterizedTest
		@JsonClasspathSource(
				value = "luke.json", data = "vehicles")
		void lukeVehicles(
				@Property("name") String name,
				@Property("length") double length) {
			// YOUR TEST CODE HERE
		}
		// end::classpath_source_nested_data[]
		// @formatter:on

	}

	@Nested
	class InlineDemo {

		// @formatter:off
		// tag::inline_source[]
		@ParameterizedTest
		@JsonSource("["
				+ "  { name: 'Luke', height: 172  },"
				+ "  { name: 'Yoda', height: 66 }"
				+ "]")
		void singleJedi(Jedi jedi) {
			// YOUR TEST CODE HERE
		}
		// end::inline_source[]
		// @formatter:on

		// @formatter:off
		// tag::inline_source_with_property[]
		@ParameterizedTest
		@JsonSource({
				"{ name: 'Luke', height: 172  }",
				"{ name: 'Yoda', height: 66 }"
		})
		void singleJediProperty(
				@Property("name") String jediName) {
			// YOUR TEST CODE HERE
		}
		// end::inline_source_with_property[]
		// @formatter:on

		// @formatter:off
		// tag::inline_source_with_list[]
		@ParameterizedTest
		@JsonSource({
				"{ name: 'Yoda', padawans: ['Dooku', 'Luke']  }",
				"{ name: 'Obi-Wan', padawans: ['Anakin', 'Luke'] }"
		})
		void multipleJedis(
				@Property("padawans") List<String> padawanNames) {
			// YOUR TEST CODE HERE
		}
		// end::inline_source_with_list[]
		// @formatter:on

		// @formatter:off
		// tag::inline_source_deconstruct_from_array[]
		@ParameterizedTest
		@JsonSource({
				"{ name: 'Yoda', height: 66 }",
				"{ name: 'Luke', height: 172 }",
		})
		void deconstructFromArray(
				@Property("name") String name,
				@Property("height") int height) {
			// YOUR TEST CODE HERE
		}
		// @formatter:on
		// end::inline_source_deconstruct_from_array[]

	}

	static class Misc {

		// tag::use_object_mapper_example[]
		@ParameterizedTest
		@UseObjectMapper("custom")
		@JsonClasspathSource("jedis.json")
		void singleJediProperty(@Property("name") String jediName) {
			// YOUR TEST CODE HERE
		}

		// end::use_object_mapper_example[]
	}

	// tag::custom_annotation[]
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	@ParameterizedTest
	@UseObjectMapper("custom")
	public @interface JsonTest {
	}
	// end::custom_annotation[]

}
