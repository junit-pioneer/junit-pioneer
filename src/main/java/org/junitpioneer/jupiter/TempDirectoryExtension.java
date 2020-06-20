/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.jupiter;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * {@code TempDirectory} is a JUnit Jupiter extension to create and clean up a
 * temporary directory.
 *
 * <p>The temporary directory is only created if a test or lifecycle method or
 * test class constructor has a parameter annotated with
 * {@link TempDir @TempDir}. If the parameter type is not {@link Path} or if the
 * temporary directory could not be created, this extension will throw a
 * {@link ParameterResolutionException}.</p>
 *
 * <p>The scope of the temporary directory depends on where the first
 * {@link TempDir @TempDir} annotation is encountered when executing a test
 * class. The temporary directory will be shared by all tests in a class when
 * the annotation is present on a parameter of a
 * {@link org.junit.jupiter.api.BeforeAll @BeforeAll} method or the test class
 * constructor. Otherwise, e.g. when only used on test or
 * {@link org.junit.jupiter.api.BeforeEach @BeforeEach} or
 * {@link org.junit.jupiter.api.AfterEach @AfterEach} methods, each test will
 * use its own temporary directory.</p>
 *
 * <p>When the end of the scope of a temporary directory is reached, i.e. when
 * the test method or class has finished execution, this extension will attempt
 * to recursively delete all files and directories in the temporary directory
 * and, finally, the temporary directory itself. In case deletion of a file or
 * directory fails, this extension will throw an {@link IOException} that will
 * cause the test to fail.</p>
 *
 * <p>By default, this extension will use the default
 * {@link java.nio.file.FileSystem FileSystem} to create temporary directories
 * in the default location. However, you may instantiate this extension using
 * the {@link TempDirectoryExtension#createInCustomDirectory(ParentDirProvider)}
 * or {@link TempDirectoryExtension#createInCustomDirectory(Callable)}} factory methods
 * and register it via {@link org.junit.jupiter.api.extension.RegisterExtension @RegisterExtension}
 * to pass a custom provider to configure the parent directory for all temporary
 * directories created by this extension. This allows the use of this extension
 * with any third-party {@code FileSystem} implementation, e.g.
 * <a href="https://github.com/google/jimfs">Jimfs</a>.</p>
 *
 * <p>Since JUnit Jupiter 5.4, there's a
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-built-in-extensions-TempDirectory">
 * built-in {@code @TempDir} extension</a>. If you don't need support for
 * arbitrary file systems, you should consider using that instead of this
 * extension.</p>
 *
 * <p>For more details and examples, see
 * <a href="https://junit-pioneer.org/docs/temp-directory/" target="_top">the documentation on <code>TempDirectory</code></a>
 * </p>
 *
 * @since 0.1
 * @see TempDir
 * @see ParentDirProvider
 * @see Files#createTempDirectory
 */
public class TempDirectoryExtension implements ParameterResolver {

	/**
	 * {@code ParentDirProvider} can be used to configure a custom parent
	 * directory for all temporary directories created by the
	 * {@link TempDirectoryExtension} extension this is used with.
	 *
	 * @see org.junit.jupiter.api.extension.RegisterExtension
	 * @see TempDirectoryExtension#createInCustomDirectory(ParentDirProvider)
	 */
	@FunctionalInterface
	public interface ParentDirProvider {

		/**
		 * Get the parent directory for all temporary directories created by the
		 * {@link TempDirectoryExtension} extension this is used with.
		 *
		 * @return the parent directory for all temporary directories
		 */
		// excluded from Sonar as java.util.concurrent.Callable<V> is root of this generic exception
		Path get(ParameterContext parameterContext, ExtensionContext extensionContext) throws Exception; //NOSONAR

	}

	/**
	 * {@code TempDirProvider} is used internally to define how the temporary
	 * directory is created.
	 *
	 * <p>The temporary directory is by default created on the regular
	 * file system, but the user can also provide a custom file system
	 * by using the {@link ParentDirProvider}. An instance of
	 * {@code TempDirProvider} executes these (and possibly other) strategies.</p>
	 *
	 * @see TempDirectoryExtension.ParentDirProvider
	 */
	@FunctionalInterface
	private interface TempDirProvider {

		CloseablePath get(ParameterContext parameterContext, ExtensionContext extensionContext, String dirPrefix);

	}

	private static final Namespace NAMESPACE = Namespace.create(TempDirectoryExtension.class);
	private static final String KEY = "temp.dir";
	private static final String TEMP_DIR_PREFIX = "junit";

	private final TempDirProvider tempDirProvider;

	private TempDirectoryExtension(TempDirProvider tempDirProvider) {
		this.tempDirProvider = requireNonNull(tempDirProvider);
	}

	/**
	 * Create a new {@code TempDirectory} extension that uses the default
	 * {@link java.nio.file.FileSystem FileSystem} and creates temporary
	 * directories in the default location.
	 *
	 * <p>This constructor is used by the JUnit Jupiter Engine when the
	 * extension is registered via
	 * {@link org.junit.jupiter.api.extension.ExtendWith @ExtendWith}.</p>
	 */
	public TempDirectoryExtension() {
		this((__, ___, dirPrefix) -> createDefaultTempDir(dirPrefix));
	}

	/**
	 * Returns a {@code TempDirectory} extension that uses the default
	 * {@link java.nio.file.FileSystem FileSystem} and creates temporary
	 * directories in the default location.
	 *
	 * <p>You may use this factory method when registering this extension via
	 * {@link org.junit.jupiter.api.extension.RegisterExtension @RegisterExtension},
	 * although you might prefer the simpler registration via
	 * {@link org.junit.jupiter.api.extension.ExtendWith @ExtendWith}.</p>
	 *
	 * @return a {@code TempDirectory} extension
	 */
	public static TempDirectoryExtension createInDefaultDirectory() {
		return new TempDirectoryExtension();
	}

	/**
	 * Returns a {@code TempDirectory} extension that uses the supplied
	 * {@link ParentDirProvider} to configure the parent directory for the
	 * temporary directories created by this extension.
	 *
	 * <p>You may use this factory method when registering this extension via
	 * {@link org.junit.jupiter.api.extension.RegisterExtension @RegisterExtension}.</p>
	 *
	 * @param parentDirProvider used to configure the parent directory for the
	 * temporary directories created by this extension
	 */
	public static TempDirectoryExtension createInCustomDirectory(ParentDirProvider parentDirProvider) {
		requireNonNull(parentDirProvider);
		return new TempDirectoryExtension((parameterContext, extensionContext,
				dirPrefix) -> createCustomTempDir(parentDirProvider, parameterContext, extensionContext, dirPrefix));
	}

	/**
	 * Returns a {@code TempDirectory} extension that uses the supplied
	 * {@link Callable} to configure the parent directory for the temporary
	 * directories created by this extension.
	 *
	 * <p>You may use this factory method when registering this extension via
	 * {@link org.junit.jupiter.api.extension.RegisterExtension @RegisterExtension}.</p>
	 *
	 * @param parentDirProvider used to configure the parent directory for the
	 * temporary directories created by this extension
	 */
	public static TempDirectoryExtension createInCustomDirectory(Callable<Path> parentDirProvider) {
		requireNonNull(parentDirProvider);
		return createInCustomDirectory((parameterContext, extensionContext) -> parentDirProvider.call());
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return parameterContext.isAnnotated(TempDir.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter().getType();
		if (parameterType != Path.class) {
			throw new ParameterResolutionException(
				"Can only resolve parameter of type " + Path.class.getName() + " but was: " + parameterType.getName());
		}
		return extensionContext
				.getStore(NAMESPACE) //
				.getOrComputeIfAbsent(KEY,
					key -> tempDirProvider.get(parameterContext, extensionContext, TEMP_DIR_PREFIX),
					CloseablePath.class) //
				.get();
	}

	private static CloseablePath createDefaultTempDir(String dirPrefix) {
		try {
			return new CloseablePath(Files.createTempDirectory(dirPrefix));
		}
		catch (Exception ex) {
			throw new ExtensionConfigurationException("Failed to create default temp directory", ex);
		}
	}

	private static CloseablePath createCustomTempDir(ParentDirProvider parentDirProvider,
			ParameterContext parameterContext, ExtensionContext extensionContext, String dirPrefix) {
		Path parentDir;
		try {
			parentDir = parentDirProvider.get(parameterContext, extensionContext);
			requireNonNull(parentDir);
		}
		catch (Exception ex) {
			throw new ParameterResolutionException("Failed to get parent directory from provider", ex);
		}
		try {
			return new CloseablePath(Files.createTempDirectory(parentDir, dirPrefix));
		}
		catch (Exception ex) {
			throw new ParameterResolutionException("Failed to create custom temp directory", ex);
		}
	}

	private static class CloseablePath implements CloseableResource {

		private final Path dir;

		CloseablePath(Path dir) {
			this.dir = dir;
		}

		Path get() {
			return dir;
		}

		@Override
		public void close() throws IOException {
			SortedMap<Path, IOException> failures = deleteAllFilesAndDirectories();
			if (!failures.isEmpty()) {
				throw createIOExceptionWithAttachedFailures(failures);
			}
		}

		private SortedMap<Path, IOException> deleteAllFilesAndDirectories() throws IOException {
			SortedMap<Path, IOException> failures = new TreeMap<>();
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
					return deleteAndContinue(file);
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					if (exc instanceof NoSuchFileException) {
						return CONTINUE;
					}
					return super.visitFileFailed(file, exc);
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
					return deleteAndContinue(dir);
				}

				private FileVisitResult deleteAndContinue(Path path) {
					try {
						// without races by multiple threads, a call to `Files::delete` would suffice
						// because the tree walker doesn't visit non-existing files; since race
						// conditions can't be ruled out, `Files::deleteIfExists` is the safer approach
						Files.deleteIfExists(path);
					}
					catch (IOException ex) {
						failures.put(path, ex);
					}
					return CONTINUE;
				}

			});
			return failures;
		}

		private IOException createIOExceptionWithAttachedFailures(SortedMap<Path, IOException> failures) {
			String joinedPaths = failures
					.keySet()
					.stream()
					.peek(this::tryToDeleteOnExit)
					.map(this::relativizeSafely)
					.map(String::valueOf)
					.collect(joining(", "));
			IOException exception = new IOException("Failed to delete temp directory " + dir.toAbsolutePath()
					+ ". The following paths could not be deleted (see suppressed exceptions for details): "
					+ joinedPaths);
			failures.values().forEach(exception::addSuppressed);
			return exception;
		}

		private void tryToDeleteOnExit(Path path) {
			try {
				path.toFile().deleteOnExit();
			}
			catch (UnsupportedOperationException ignore) {
				// If the `Path` can't be turned into a `File` (which throws the UOE),
				// it can't be registered to be deleted when the JVM terminates.
				// Because deleting on JVM termination is just a last ditch effort and
				// nicety towards the user, it is entirely optional and shouldn't affect
				// the extension's behavior.
			}
		}

		private Path relativizeSafely(Path path) {
			try {
				return dir.relativize(path);
			}
			catch (IllegalArgumentException e) {
				return path;
			}
		}

	}

}
