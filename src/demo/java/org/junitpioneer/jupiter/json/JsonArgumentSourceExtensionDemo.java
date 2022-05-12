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

		// tag::classpath_source_with_property[]
		@ParameterizedTest
		@JsonClasspathSource("jedis.json")
		void singleJediProperty(@Property("name") String jediName) {
			// YOUR TEST CODE HERE
		}
		// end::classpath_source_with_property[]

		// tag::classpath_source_deconstruct_from_array[]
		@ParameterizedTest
		@JsonClasspathSource("jedis.json")
		void deconstructFromArray(@Property("name") String name, @Property("height") int height) {
			// YOUR TEST CODE HERE
		}
		// end::classpath_source_deconstruct_from_array[]

		// tag::classpath_source_nested_data[]
		@ParameterizedTest
		@JsonClasspathSource(value = "luke.json", data = "vehicles")
		void lukeVehicles(@Property("name") String name, @Property("length") double length) {
			// YOUR TEST CODE HERE
		}
		// end::classpath_source_nested_data[]

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
		void singleJediProperty(@Property("name") String jediName) {
			// YOUR TEST CODE HERE
		}
		// end::inline_source_with_property[]
		// @formatter:on

		// @formatter:off
		// tag::inline_source_deconstruct_from_array[]
		@ParameterizedTest
		@JsonSource({
				"{ name: 'Yoda', height: 66 }",
				"{ name: 'Luke', height: 172 }",
		})
		void deconstructFromArray(@Property("name") String name, @Property("height") int height) {
			// YOUR TEST CODE HERE
		}
		// @formatter:on
		// end::inline_source_deconstruct_from_array[]

	}

}
