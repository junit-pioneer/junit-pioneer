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

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

// tag::in_memory_directory[]

public final class InMemoryDirectory implements ResourceFactory<Path> {

	private static final AtomicInteger DIRECTORY_NAME = new AtomicInteger();

	private final FileSystem fileSystem;

	public InMemoryDirectory() {
		this.fileSystem = Jimfs.newFileSystem(Configuration.unix());
	}

	@Override
	public Resource<Path> create(List<String> arguments) throws Exception {
		String directoryPrefix = (arguments.size() == 1) ? arguments.get(0) : "";

		Path newInMemoryDirectory = this.fileSystem.getPath("/" + directoryPrefix + DIRECTORY_NAME.getAndIncrement());
		Files.createDirectory(newInMemoryDirectory);

		return new Resource<Path>() {

			@Override
			public Path get() throws Exception {
				return newInMemoryDirectory;
			}

			@Override
			public void close() throws Exception {
				Files.walkFileTree(newInMemoryDirectory, new SimpleFileVisitor<Path>() {

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.deleteIfExists(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.deleteIfExists(dir);
						return FileVisitResult.CONTINUE;
					}

				});
			}

		};
	}

	@Override
	public void close() throws Exception {
		this.fileSystem.close();
	}

}

// end::in_memory_directory[]
