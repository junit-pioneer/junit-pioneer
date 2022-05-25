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

	// tag::create_new_resource_demo[]
	void test(@New(TemporaryDirectory.class) Path tempDir) {
		// Test code goes here, e.g.,
		assertTrue(Files.exists(tempDir));
	}
	// end::create_new_resource_demo[]

	// tag::create_new_resource_with_arg_demo[]
	void testWithArg(@New(value = TemporaryDirectory.class, arguments = { "customDirectoryName" }) Path tempDir) {
		// Test code goes here, e.g.,
		assertTrue(tempDir.endsWith("customDirectoryName"));
	}
	// end::create_new_resource_with_arg_demo[]

}
