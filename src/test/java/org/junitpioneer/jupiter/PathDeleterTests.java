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

import java.io.IOException;
import java.nio.file.FileSystem;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import org.junit.jupiter.api.Test;

class PathDeleterTests {

	@Test
	void deletingNonExistentFileProducesNoIOException() throws IOException {
		try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
			// Expected not to throw an exception
			PathDeleter.INSTANCE.visitFile(fileSystem.getPath("some", "arbitrary", "file.txt"), null);
		}
	}

	@Test
	void deletingNonExistentDirectoryProducesNoIOException() throws IOException {
		try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
			// Expected not to throw an exception
			PathDeleter.INSTANCE.postVisitDirectory(fileSystem.getPath("some", "arbitrary", "directory"), null);
		}
	}

}
