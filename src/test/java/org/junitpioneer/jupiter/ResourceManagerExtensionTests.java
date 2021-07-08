/*
 * Copyright 2016-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayName("ResourceManager extension")
// TODO: Do we need a test that checks a test case with LifeCycle.PER_METHOD? Ask maintainers.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ResourceManagerExtensionTests {

	@DisplayName("when a test class has a test method with a @New(TemporaryDirectory.class)-annotated parameter")
	@ExtendWith(ResourceManagerExtension.class)
	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class NewTemporaryDirectoryInMethodTests {

		Path recordedPath;

		@DisplayName("then the parameter is populated with a new temporary directory")
		@Order(0)
		@Test
		void thenParameterIsPopulatedWithNewTemporaryDirectory(@New(TemporaryDirectory.class) Path tempDir) {
			assertThat(tempDir).startsWith(Paths.get(System.getProperty("java.io.tmpdir")));

			recordedPath = tempDir;
		}

		@DisplayName("then the respective temporary directory is torn down when finished")
		@Order(1)
		@Test
		void thenRespectiveTemporaryDirectoryIsTornDownWhenFinished() {
			assertThat(recordedPath).doesNotExist();
		}

		@DisplayName("then the associated directory is writeable and readable")
		@Order(0)
		@Test
		void thenAssociatedDirectoryIsWriteableAndReadable(@New(TemporaryDirectory.class) Path tempDir)
				throws Exception {
			Files.write(tempDir.resolve("file.txt"), "some random text".getBytes(UTF_8));
			assertThat(tempDir.resolve("file.txt")).usingCharset(UTF_8).hasContent("some random text");
		}

	}

	// ---

	@DisplayName("when a test class has multiple test methods with a @New(TemporaryDirectory.class)-annotated parameter")
	@ExtendWith(ResourceManagerExtension.class)
	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
	class NewTemporaryDirectoryInMultipleMethodsTests {

		List<Path> recordedPaths = new ArrayList<>();

		@DisplayName("then the parameter on the first test method is populated with a new temporary directory")
		@Order(0)
		@Test
		void thenParameterOnFirstTestMethodIsPopulatedWithNewTemporaryDirectory(
				@New(TemporaryDirectory.class) Path tempDir) {
			assertThat(tempDir).startsWith(Paths.get(System.getProperty("java.io.tmpdir")));
			recordedPaths.add(tempDir);
		}

		@DisplayName("then the parameter on the second test method is populated with a new temporary directory")
		@Order(0)
		@Test
		void thenParameterOnSecondTestMethodIsPopulatedWithNewTemporaryDirectory(
				@New(TemporaryDirectory.class) Path tempDir) {
			assertThat(tempDir).startsWith(Paths.get(System.getProperty("java.io.tmpdir")));
			recordedPaths.add(tempDir);
		}

		@DisplayName("then the respective temporary directories are torn down when finished")
		@Order(1)
		@Test
		void thenRespectiveTemporaryDirectoryIsTornDownWhenFinished() {
			assertThat(recordedPaths).hasSize(2).allSatisfy(path -> assertThat(path).doesNotExist());
		}

		@DisplayName("then the respective temporary directories are distinct")
		@Order(1)
		@Test
		void thenRespectiveTemporaryDirectoriesAreDistinct() {
			assertThat(recordedPaths.stream().distinct()).hasSize(recordedPaths.size());
		}

	}

	// ---

	Path recordedPathFromConstructor;

	@DisplayName("when a test class has a constructor with a @New(TemporaryDirectory.class)-annotated parameter")
	@ExtendWith(ResourceManagerExtension.class)
	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class NewTemporaryDirectoryInConstructorTests {

		NewTemporaryDirectoryInConstructorTests(@New(TemporaryDirectory.class) Path tempDir) {
			recordedPathFromConstructor = tempDir;
		}

		@DisplayName("then the parameter is populated with a new temporary directory")
		@Test
		void thenParameterIsPopulatedWithNewTemporaryDirectory() {
			assertThat(recordedPathFromConstructor).startsWith(Paths.get(System.getProperty("java.io.tmpdir")));
		}

	}

	// ---

	@AfterAll
	@DisplayName("Check that the temporary directory created in a test class constructor is torn down")
	void checkThatTemporaryDirectoryCreatedInTestClassConstructorIsTornDown() {
		assertThat(recordedPathFromConstructor).doesNotExist();
	}

	// TODO: Write and test with two custom ResourceFactory implementations: jimfs and OkHttp's MockWebServer
}
