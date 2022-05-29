/*
 * Copyright 2016-2022 the original author or authors.
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

public class ResourcesExtensionDemo {

	// tag::create_new_resources_demo[]
	void test1(@New(TemporaryDirectory.class) Path tempDir) {
		// Test code goes here, e.g.,
		assertTrue(Files.exists(tempDir));
	}

	void test2(@New(TemporaryDirectory.class) Path tempDir) {
		// Test code goes here, e.g.,
		assertTrue(Files.exists(tempDir));
	}
	// end::create_new_resources_demo[]

	// tag::create_new_resource_with_arg_demo[]
	void testWithArg(@New(value = TemporaryDirectory.class, arguments = { "customDirectoryName" }) Path tempDir) {
		// Test code goes here, e.g.,
		assertTrue(tempDir.endsWith("customDirectoryName"));
	}
	// end::create_new_resource_with_arg_demo[]

	// tag::create_shared_resource_demo[]
	void sharedResourceTest1(@Shared(factory = TemporaryDirectory.class, name = "sharedTempDir") Path sharedTempDir) {
		// Test code goes here, e.g.,
		assertTrue(Files.exists(sharedTempDir));
	}

	void sharedResourceTest2(@Shared(factory = TemporaryDirectory.class, name = "sharedTempDir") Path sharedTempDir) {
		// Test code goes here, e.g.,
		assertTrue(Files.exists(sharedTempDir));
	}
	// end::create_shared_resource_demo[]

	// tag::create_multiple_shared_resources_demo[]
	void firstSharedResource1(@Shared(factory = TemporaryDirectory.class, name = "first") Path first) {
		// Test code working with first shared resource...
	}

	void firstSharedResource2(@Shared(factory = TemporaryDirectory.class, name = "first") Path first) {
		// Test code working with first shared resource...
	}

	void secondSharedResource(@Shared(factory = TemporaryDirectory.class, name = "second") Path second) {
		// Test code working with second shared resource...
	}
	// end::create_multiple_shared_resources_demo[]

}

// tag::create_global_shared_resource_demo_first[]
class FirstTest {

	void test(@Shared(factory = TemporaryDirectory.class, name = "globalTempDir", scope = Scope.GLOBAL) Path tempDir) {
		// Test code using the global shared resource...
	}

}
// end::create_global_shared_resource_demo_first[]

// tag::create_global_shared_resource_demo_second[]
class SecondTest {

	void test(@Shared(factory = TemporaryDirectory.class, name = "globalTempDir", scope = Scope.GLOBAL) Path tempDir) {
		// Test code using the global shared resource...
	}

}
// end::create_global_shared_resource_demo_second[]
