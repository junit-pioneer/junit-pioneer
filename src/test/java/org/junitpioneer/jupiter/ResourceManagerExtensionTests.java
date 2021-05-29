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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@DisplayName("Resource manager extension")
@ExtendWith(ResourceManagerExtension.class)
// TODO: Do we need a test that checks a test case with LifeCycle.PER_METHOD? Ask maintainers.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResourceManagerExtensionTests {

	private Path firstRecordedTempDir;
	private final List<Path> recordedTempDirs = new CopyOnWriteArrayList<>();

	// TODO: Consider adding a constructor with a @New(TemporaryDirectory.class) Path

	@DisplayName("should populate a @New(TemporaryDirectory.class)-annotated parameter with a temp dir resource")
	@Order(0)
	@Test
	void shouldPopulateNewAnnotatedParameterWithTempDirResource(@New(TemporaryDirectory.class) Path tempDir) {
		assertThat(tempDir).startsWith(Paths.get(System.getProperty("java.io.tmpdir")));

		firstRecordedTempDir = tempDir;
		recordedTempDirs.add(tempDir);
	}

	@DisplayName("should tear down the new resource at the end")
	@Order(1)
	@Test
	void shouldTearDownNewResourceAtTheEnd() {
		assertThat(firstRecordedTempDir).doesNotExist();
	}

	@DisplayName("should populate a second @New(TemporaryDirectory.class)-annotated parameter with a temp dir resource")
	@Order(2)
	@Test
	void shouldPopulateSecondNewAnnotatedParameterWithTempDirResource(@New(TemporaryDirectory.class) Path tempDir) {
		assertThat(tempDir).startsWith(Paths.get(System.getProperty("java.io.tmpdir")));

		recordedTempDirs.add(tempDir);
	}

	@DisplayName("should populate @New(TemporaryDirectory.class)-annotated parameters with writeable temp dirs")
	@Order(2)
	@Test
	void shouldPopulateNewAnnotatedParametersWithWriteableTempDirs(@New(TemporaryDirectory.class) Path tempDir)
			throws Exception {
		Files.write(tempDir.resolve("file.txt"), "some random text".getBytes(UTF_8));
		assertThat(tempDir.resolve("file.txt")).usingCharset(UTF_8).hasContent("some random text");

		recordedTempDirs.add(tempDir);
	}

	@DisplayName("should have generated new resources each time @New was used")
	@Order(3)
	@Test
	void shouldHaveGeneratedNewResourcesEachTimeNewAnnotationWasUsed() {
		int numberOfNewTempDirsInThisClass = 3;
		assertThat(recordedTempDirs).hasSize(numberOfNewTempDirsInThisClass);
		assertThat(recordedTempDirs.stream().distinct()).hasSize(numberOfNewTempDirsInThisClass);
	}

	// TODO: Write and test with two custom ResourceFactory implementations: jimfs and OkHttp's MockWebServer
}
