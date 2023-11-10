/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter.resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class ResourceExtensionDemo {

	// tag::create_new_resources_demo[]
	@Test
	void test1(@New(TemporaryDirectory.class) Path tempDir) {
		// Test code goes here, e.g.,
		assertTrue(Files.exists(tempDir));
	}

	@Test
	void test2(@New(TemporaryDirectory.class) Path tempDir) {
		// This temporary directory is different to the first one.
	}
	// end::create_new_resources_demo[]

	// tag::create_new_dir_demo[]
	@Test
	void dirTest1(@Dir Path tempDir) {
		// Test code goes here, e.g.,
		assertTrue(Files.exists(tempDir));
	}

	@Test
	void dirTest2(@Dir Path tempDir) {
		// This temporary directory is different to the first one.
	}
	// end::create_new_dir_demo[]

	// @formatter:off
	// tag::create_new_resource_with_arg_demo[]
	@Test
	void testWithArg(
			@New(value = TemporaryDirectory.class, arguments = "customPrefix")
			Path tempDir) {
		// Test code goes here, e.g.,
		Path rootTempDir = Paths.get(System.getProperty("java.io.tmpdir"));
		assertTrue(rootTempDir.relativize(tempDir).toString().startsWith("customPrefix"));
	}
	// end::create_new_resource_with_arg_demo[]
	// @formatter:on

	// @formatter:off
	// tag::create_shared_resource_demo[]
	@Test
	void sharedResourceTest1(
			@Shared(factory = TemporaryDirectory.class, name = "sharedTempDir")
			Path sharedTempDir) {
		// Test code goes here, e.g.,
		assertTrue(Files.exists(sharedTempDir));
	}

	@Test
	void sharedResourceTest2(
			@Shared(factory = TemporaryDirectory.class, name = "sharedTempDir")
			Path sharedTempDir) {
		// "sharedTempDir" is shared with the temporary directory of
		// the same name in test "sharedResourceTest1", so any created
		// subdirectories and files will be shared.
	}
	// end::create_shared_resource_demo[]
	// @formatter:on

	// @formatter:off
	// tag::create_multiple_shared_resources_demo[]
	@Test
	void firstSharedResource1(
			@Shared(factory = TemporaryDirectory.class, name = "first")
			Path first) {
		// Test code working with first shared resource...
	}

	@Test
	void firstSharedResource2(
			@Shared(factory = TemporaryDirectory.class, name = "first")
			Path first) {
		// Test code working with first shared resource...
	}

	@Test
	void secondSharedResource(
			@Shared(factory = TemporaryDirectory.class, name = "second")
			Path second) {
		// This shared resource is different!
	}
	// end::create_multiple_shared_resources_demo[]
	// @formatter:on

}

// @formatter:off
// tag::create_global_shared_resource_demo_first[]
class FirstTest {

	@Test
	void test(
			@Shared(
					factory = TemporaryDirectory.class,
					name = "globalTempDir",
					scope = Shared.Scope.GLOBAL)
			Path tempDir) {
		// Test code using the global shared resource...
	}

}
// end::create_global_shared_resource_demo_first[]
// @formatter:on

// @formatter:off
// tag::create_global_shared_resource_demo_second[]
class SecondTest {

	@Test
	void test(
			@Shared(
					factory = TemporaryDirectory.class,
					name = "globalTempDir",
					scope = Shared.Scope.GLOBAL)
			Path tempDir) {
		// Test code using the global shared resource...
	}

}
// end::create_global_shared_resource_demo_second[]
// @formatter:on
